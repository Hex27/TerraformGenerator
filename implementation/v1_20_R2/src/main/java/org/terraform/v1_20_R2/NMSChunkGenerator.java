package org.terraform.v1_20_R2;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.BlockColumn;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.WorldChunkManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenStage;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.WorldGenCarverAbstract;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasureStructure;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBiome;
import org.bukkit.craftbukkit.v1_20_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R2.generator.CraftLimitedRegion;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftMagicNumbers;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.terraform.biome.custombiomes.CustomBiomeSupportedBiomeGrid;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.bukkit.TerraformBukkitBlockPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.StructureLocator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.pillager.mansion.MansionPopulator;
import org.terraform.structure.small.buriedtreasure.BuriedTreasurePopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;
import org.terraform.utils.BlockUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("deprecation")
public class NMSChunkGenerator extends ChunkGenerator {
	private final ChunkGenerator delegate;
    
	private final WorldServer world;
    private final TerraformPopulator pop;
    private final TerraformWorld tw;
    private final MapRenderWorldProviderBiome mapRendererBS;
    private final TerraformWorldProviderBiome twBS;
    
	public NMSChunkGenerator(String worldname, long seed,
    						ChunkGenerator delegate) {
    	super(
                delegate.c(), //WorldChunkManager c() is getBiomeSource()
    			delegate.d); //Idk what generationSettingsGetter is
        tw = TerraformWorld.get(worldname, seed);
        this.delegate = delegate;

        //Set the long term biome handler to this one. The normal behaving one
        //is initiated inside the cave carver
        mapRendererBS = new MapRenderWorldProviderBiome(tw, delegate.c());
        twBS = new TerraformWorldProviderBiome(tw, delegate.c());
        pop = new TerraformPopulator(tw);
        world = ((CraftWorld) Objects.requireNonNull(Bukkit.getWorld(worldname))).getHandle();
        
        try {
            modifyCaveCarverLists(WorldGenCarverAbstract.a);
            modifyCaveCarverLists(WorldGenCarverAbstract.b);
            modifyCaveCarverLists(WorldGenCarverAbstract.c);
        } catch (Exception e) {
            TerraformGeneratorPlugin.logger.error("Failed to modify vanilla cave carver lists. You may see floating blocks above caves.");
            e.printStackTrace();
        }
    }
    

    @Override //getBiomeSource
    public WorldChunkManager c() {
    	return mapRendererBS;
    }
    
    public TerraformWorld getTerraformWorld() {
        return tw;
    }

    @Override //createBiomes
    public CompletableFuture<IChunkAccess> a(Executor executor, RandomState randomstate, Blender blender, StructureManager structuremanager, IChunkAccess ichunkaccess)
    {
    	return CompletableFuture.supplyAsync(SystemUtils.a("init_biomes", () -> {
             return ichunkaccess; //Don't do any calculations here, biomes are set in applyCarvers
         }), SystemUtils.f());
     }
    
	@Override //findNearestMapFeature
	public Pair<BlockPosition, Holder<Structure>> a(WorldServer worldserver, HolderSet<Structure> holderset, 
			BlockPosition blockposition, int i, boolean flag) {
    	
    	int pX = blockposition.u(); //getX
        int pZ = blockposition.w(); //getZ
        
        for(Holder<Structure> holder:holderset) {
        	Structure feature = holder.a();
        	//StructureGenerator<?> structuregenerator = feature.;
        	TerraformGeneratorPlugin.logger.info("Vanilla locate for " + feature.getClass().getName() + " invoked.");

            if (holder.a().getClass() == StrongholdStructure.class) { //stronghold
                int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
                return new Pair<>(new BlockPosition(coords[0], 20, coords[1]), holder);
            } 
            else if(!TConfigOption.DEVSTUFF_VANILLA_LOCATE_DISABLE.getBoolean())
            {
            	if (holder.a().getClass() == OceanMonumentStructure.class) { //Monument
                    
            		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MonumentPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());

                    return new Pair<BlockPosition, Holder<Structure>>
                    (new BlockPosition(coords[0], 50, coords[1]), holder);
                } else if (holder.a().getClass() == WoodlandMansionStructure.class) { //Mansion
                        
            		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MansionPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());

                    return new Pair<BlockPosition, Holder<Structure>>
                    (new BlockPosition(coords[0], 50, coords[1]), holder);
                } else if (holder.a().getClass() == BuriedTreasureStructure.class) { 
                	//Buried Treasure
                	int[] coords = StructureLocator.locateMultiMegaChunkStructure(tw, new MegaChunk(pX, 0, pZ), new BuriedTreasurePopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
                    if(coords == null) return null;
                    return new Pair<BlockPosition, Holder<Structure>>
                    (new BlockPosition(coords[0], 50, coords[1]), holder);
                }
            }
        }
        return null;
    }

    @Override //applyBiomeDecoration
    public void a(GeneratorAccessSeed generatoraccessseed, IChunkAccess ichunkaccess, StructureManager structuremanager) {
    	int chunkX = ichunkaccess.f().e; //x
        int chunkZ = ichunkaccess.f().f; //z
        PopulatorData popDat = new PopulatorData(generatoraccessseed, ichunkaccess, this, chunkX, chunkZ);
        pop.populate(tw, tw.getHashedRand(8292012, chunkX, chunkZ), popDat);

        //Spigot API BlockPopulator support
        World world = generatoraccessseed.getMinecraftWorld().getWorld();
        if (!world.getPopulators().isEmpty()) {
            CraftLimitedRegion limitedRegion = new CraftLimitedRegion(generatoraccessseed, ichunkaccess.f());
            int x = ichunkaccess.f().e;
            int z = ichunkaccess.f().f;
            Iterator<BlockPopulator> var10 = world.getPopulators().iterator();

            while(var10.hasNext()) {
                BlockPopulator populator = var10.next();
                if(populator instanceof TerraformBukkitBlockPopulator)
                    continue;
                //A is getSeed
                populator.populate(world, tw.getHashedRand(generatoraccessseed.A(), x, z), x, z, limitedRegion);
            }

            limitedRegion.saveEntities();
            limitedRegion.breakLink();
        }
    }

    @Override //applyCarvers
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess, long seed, 
    		RandomState randomstate, BiomeManager biomemanager, 
    		StructureManager structuremanager, IChunkAccess ichunkaccess, 
    		WorldGenStage.Features worldgenstage_features)
    {
    	//POPULATES BIOMES. IMPORTANT
    	//(net.minecraft.world.level.biome.BiomeResolver,net.minecraft.world.level.biome.Climate$Sampler)
        //Use twBS as it is the biome provider that actually calculates biomes.
        //The other one only returns river/plains
        ichunkaccess.a(this.twBS, null); //This can be null as its passed into twBS

  		try {
        	TerraformGenerator generator = new TerraformGenerator();
            int chunkX = ichunkaccess.f().e; //x
            int chunkZ = ichunkaccess.f().f; //z
        	Random random = tw.getRand(3);
        	random.setSeed((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
            
        	//CustomBiomeGrid biomegrid = new CustomBiomeGrid(new BiomeStorage(this.world.t().d(IRegistry.aO), regionlimitedworldaccess, ichunkaccess.getPos(), this.getWorldChunkManager()));
        	CustomBiomeGrid biomegrid = new CustomBiomeGrid(ichunkaccess);
        	PopulatorDataAbstract data = new PopulatorData(regionlimitedworldaccess, ichunkaccess, this, chunkX, chunkZ);
        	((PopulatorData)data).setRadius(0);
        	data = new PopulatorDataICA(data,tw,this.world,ichunkaccess,chunkX,chunkZ);
        	
        	generator.addPopulatorData(data);
            ChunkData cd = generator.generateChunkData(tw.getWorld(), random, chunkX, chunkZ, biomegrid);
            
            //Do carving after ground is set.
        	delegate.a(regionlimitedworldaccess, seed, 
            		randomstate, biomemanager, 
            		structuremanager, ichunkaccess, 
            		worldgenstage_features);
        	
        	//Fill seas and delete water caves in the air after carving.
        	for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int rawX = chunkX * 16 + x;
                    int rawZ = chunkZ * 16 + z;

                    //tw.getBiomeBank(rawX, rawZ);
                    int height = tw.maxY-1; //(int) org.terraform.coregen.HeightMap.getBlockHeight(tw, rawX, rawZ);
                    while(!CraftBlockData.fromData(ichunkaccess.a_(new BlockPosition(rawX, height, rawZ))).getMaterial().isSolid()
                    	&& height > TerraformGenerator.seaLevel) {
                		ichunkaccess.a(new BlockPosition(rawX, height, rawZ), Blocks.a.n(), false); //Blocks.AIR.defaultBlockState()
                    	height--;
                    }
                    
                    generator.fillSeaAndRivers(cd,x,z,org.terraform.coregen.HeightMap.getBlockHeight(tw, rawX, rawZ));
                }
        	}
        	
        	
            //CarverRegistry.doCarving(tw, data, random);
  		
  		} catch (SecurityException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to modify cave carvers in vanilla to carve some other blocks.
     */
    private void modifyCaveCarverLists(WorldGenCarverAbstract<?> carverAbstract) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        HashSet<Block> temp = new HashSet<>();
        for(Material mat: BlockUtils.caveDecoratorMaterials)
            temp.add(CraftMagicNumbers.getBlock(mat));
        for(Material mat: BlockUtils.badlandsStoneLike)
            temp.add(CraftMagicNumbers.getBlock(mat));
        temp.add(CraftMagicNumbers.getBlock(Material.WATER));
        temp.add(CraftMagicNumbers.getBlock(Material.LAVA));

        Field field = WorldGenCarverAbstract.class.getDeclaredField("h");
        if (!field.isAccessible())
            field.setAccessible(true);
        field.set(carverAbstract, ImmutableSet.copyOf(temp));
    }

    @Override //getSeaLevel
    public int e() {
        return delegate.e();
    }

    @Override //createStructures should be empty
    public void a(IRegistryCustom iregistrycustom, ChunkGeneratorStructureState chunkgeneratorstructurestate, StructureManager structuremanager, IChunkAccess ichunkaccess, StructureTemplateManager structuretemplatemanager) {
    }

    @Override //getSpawnHeight
    public int a(LevelHeightAccessor levelheightaccessor) {
        return 64;
     }

    @Override //fillFromNoise
    public CompletableFuture<IChunkAccess> a(Executor executor, Blender blender, 
    		RandomState randomstate, StructureManager structuremanager, 
    		IChunkAccess ichunkaccess) {
    	return delegate.a(executor, blender, 
        		randomstate, structuremanager, 
        		ichunkaccess);
     }
    

//  	@SuppressWarnings("unchecked")
    @Override //buildSurface. Used to be buildBase
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess, StructureManager structuremanager, RandomState randomstate, IChunkAccess ichunkaccess) {

    }
  	
  	@Override //createReferences. Idk what this is
  	public void a(GeneratorAccessSeed gas,StructureManager manager,IChunkAccess ica)
  	{
  		delegate.a(gas, manager, ica);
  	}

    @Override
    protected Codec<? extends ChunkGenerator> a() {
        return Codec.unit(null);
    }

    //This class is on the verge of getting deleted.
	private static class CustomBiomeGrid extends CustomBiomeSupportedBiomeGrid implements BiomeGrid {

        private final IChunkAccess biome;
        public CustomBiomeGrid(IChunkAccess biome) {
            this.biome = biome;
        }

        @Override
        public Biome getBiome(int x, int z) {
        	return getBiome(x, TerraformGenerator.seaLevel, z);
        }

        @Override
        public void setBiome(int x, int z, Biome bio) {
        }

		@Override
        public Biome getBiome(int x, int y, int z) {
            try {
            	return CraftBiome.minecraftHolderToBukkit(this.biome.getNoiseBiome(x >> 2, y >> 2, z >> 2));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			}
        }
		@Override
        public void setBiome(int x, int y, int z, Biome biome) {
        }
        
		@Override
		public void setBiome(int x, int y, int z, CustomBiomeType bio, Biome fallback) {
		}
    }
    
	@Override //getBaseColumn
	public BlockColumn a(int i, int j, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
		return this.delegate.a(i,j,levelheightaccessor,randomstate);
	}
	
//	@Override //withSeed
//	public ChunkGenerator a(long seed) {
//		return new NMSChunkGenerator(this.tw.getName(), (int) seed, this.delegate);
//	}
	
	//spawnOriginalMobs
	public void a(RegionLimitedWorldAccess regionlimitedworldaccess) {
		this.delegate.a(regionlimitedworldaccess);
	}
	//getGenDepth
	public int d() {
		return this.delegate.d();
	}

	
	//getMinY
	@Override
	public int f() {
		return this.delegate.f();
	}
	
	@Override //getFirstFreeHeight
	public int b(int i, int j, HeightMap.Type heightmap_type, 
			LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
		return this.a(i, j, heightmap_type, levelheightaccessor, randomstate);
	}
	
	
	@Override //getFirstOccupiedHeight
	public int c(int i, int j, HeightMap.Type heightmap_type, 
			LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
		return this.a(i, j, heightmap_type, levelheightaccessor, randomstate) - 1;
	}

    @Override //getBaseHeight
    public int a(int i, int j, HeightMap.Type heightmap_type, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        //return delegate.a(x, z, var2, var3);
    	return 100;
    	//return org.terraform.coregen.HeightMap.getBlockHeight(tw, x, z);
    }
   
    //private static boolean biomeDebug = false;
    

	@Override //addDebugScreenInfo
	public void a(List<String> list, RandomState randomstate, BlockPosition blockposition) {
		
	}
    
}
