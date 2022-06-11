package org.terraform.v1_19_R1;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.BlockColumn;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.WorldChunkManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.levelgen.ChunkGeneratorAbstract;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenStage;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.WorldGenCarverAbstract;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionStructure;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasureStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.generator.CraftLimitedRegion;
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
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("deprecation")
public class NMSChunkGenerator extends ChunkGenerator {
	private final ChunkGenerator delegate;
    
	private final WorldServer world;
    private final TerraformPopulator pop;
    private final TerraformWorld tw;
    
	public NMSChunkGenerator(String worldname, long seed,
    						ChunkGenerator delegate) {
    	super(
    			delegate.b,//b is structureSets
    			delegate.d,//d is structureOverrides
    			new TerraformWorldProviderBiome(TerraformWorld.get(worldname,seed), 
    					delegate.d()));  //Last arg is WorldChunkManager (BiomeSource)

        tw = TerraformWorld.get(worldname, seed);
        this.delegate = delegate;
        
        pop = new TerraformPopulator(tw);
        world = ((CraftWorld) Bukkit.getWorld(worldname)).getHandle();
        
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
    public WorldChunkManager d() {
    	//d is runtimeBiomeSource
    	if(!(c instanceof TerraformWorldProviderBiome))
        	TerraformGeneratorPlugin.logger.error("c was not an instance of TerraformWorldProviderBiome!");

        return this.c;
    }
    
    public TerraformWorld getTerraformWorld() {
        return tw;
    }

    //createBiomes
    @Override
    public CompletableFuture<IChunkAccess> a(IRegistry<BiomeBase> iregistry, Executor executor, 
    		RandomState randomstate, Blender blender, 
    		StructureManager structuremanager, IChunkAccess ichunkaccess) {
    	return CompletableFuture.supplyAsync(SystemUtils.a("init_biomes", () -> {
             return ichunkaccess; //Don't do any calculations here, biomes are set in applyCarvers
         }), SystemUtils.f());
     }
    
	@Override //findNearestMapFeature
	public Pair<BlockPosition, Holder<Structure>> a(WorldServer worldserver, HolderSet<Structure> holderset, 
			BlockPosition blockposition, int i, boolean flag) {
//	    net.minecraft.core.Holder PILLAGER_OUTPOST -> a
//	    net.minecraft.core.Holder MINESHAFT -> b
//	    net.minecraft.core.Holder MINESHAFT_MESA -> c
//	    net.minecraft.core.Holder WOODLAND_MANSION -> d
//	    net.minecraft.core.Holder JUNGLE_TEMPLE -> e
//	    net.minecraft.core.Holder DESERT_PYRAMID -> f
//	    net.minecraft.core.Holder IGLOO -> g
//	    net.minecraft.core.Holder SHIPWRECK -> h
//	    net.minecraft.core.Holder SHIPWRECK_BEACHED -> i
//	    net.minecraft.core.Holder SWAMP_HUT -> j
//	    net.minecraft.core.Holder STRONGHOLD -> k
//	    net.minecraft.core.Holder OCEAN_MONUMENT -> l
//	    net.minecraft.core.Holder OCEAN_RUIN_COLD -> m
//	    net.minecraft.core.Holder OCEAN_RUIN_WARM -> n
//	    net.minecraft.core.Holder FORTRESS -> o
//	    net.minecraft.core.Holder NETHER_FOSSIL -> p
//	    net.minecraft.core.Holder END_CITY -> q
//	    net.minecraft.core.Holder BURIED_TREASURE -> r
//	    net.minecraft.core.Holder BASTION_REMNANT -> s
//	    net.minecraft.core.Holder VILLAGE_PLAINS -> t
//	    net.minecraft.core.Holder VILLAGE_DESERT -> u
//	    net.minecraft.core.Holder VILLAGE_SAVANNA -> v
//	    net.minecraft.core.Holder VILLAGE_SNOWY -> w
//	    net.minecraft.core.Holder VILLAGE_TAIGA -> x
//	    net.minecraft.core.Holder RUINED_PORTAL_STANDARD -> y
//	    net.minecraft.core.Holder RUINED_PORTAL_DESERT -> z
//	    net.minecraft.core.Holder RUINED_PORTAL_JUNGLE -> A
//	    net.minecraft.core.Holder RUINED_PORTAL_SWAMP -> B
//	    net.minecraft.core.Holder RUINED_PORTAL_MOUNTAIN -> C
//	    net.minecraft.core.Holder RUINED_PORTAL_OCEAN -> D
//	    net.minecraft.core.Holder RUINED_PORTAL_NETHER -> E
//	    net.minecraft.core.Holder ANCIENT_CITY -> F
    	
    	int pX = blockposition.u(); //getX
        int pZ = blockposition.w(); //getZ
        
        for(Holder<Structure> holder:holderset) {
        	Structure feature = holder.a();
        	//StructureGenerator<?> structuregenerator = feature.;
        	TerraformGeneratorPlugin.logger.info("Vanilla locate for " + feature.getClass().getName() + " invoked.");

            if (holder.a().getClass() == StrongholdStructure.class) { //stronghold
                int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
                return new Pair<BlockPosition, Holder<Structure>>
                (new BlockPosition(coords[0], 20, coords[1]), holder);
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
              BlockPopulator populator = (BlockPopulator)var10.next();
              if(populator instanceof TerraformBukkitBlockPopulator)
            	  continue;
              
              populator.populate(world, tw.getHashedRand(generatoraccessseed.B(), x, z), x, z, limitedRegion);
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
    	//ichunkaccess.fillBiomesFromNoise(this.runtimeBiomeSource::getNoiseBiome, this.climateSampler())
    	ichunkaccess.a(this.d(), randomstate.c());
    	
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
                		ichunkaccess.a(new BlockPosition(rawX, height, rawZ), Blocks.a.m(), false); //AIR
                    	height--;
                    }
                    
                    generator.fillSeaAndRivers(cd,x,z,(int) org.terraform.coregen.HeightMap.getBlockHeight(tw, rawX, rawZ));
                }
        	}
        	
        	
            //CarverRegistry.doCarving(tw, data, random);
  		
  		} catch (SecurityException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to modify cave carvers in vanilla to carve some other blocks.
     * @param carverAbstract
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private void modifyCaveCarverLists(WorldGenCarverAbstract<?> carverAbstract) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Set<net.minecraft.world.level.block.Block> immutableCarverList =
        		ImmutableSet.of(Blocks.A, Blocks.b, Blocks.c, Blocks.e, Blocks.g, Blocks.j, Blocks.k, Blocks.l, Blocks.i, Blocks.hf, Blocks.fS, Blocks.fT, Blocks.fU, Blocks.fV, Blocks.fW, Blocks.fX, Blocks.fY, Blocks.fZ, Blocks.ga, Blocks.gb, Blocks.gc, Blocks.gd, Blocks.ge, Blocks.gf, Blocks.gg, Blocks.gh, Blocks.az, Blocks.hU, Blocks.ec, Blocks.cK, Blocks.hh, Blocks.pN, Blocks.oM, Blocks.C, Blocks.D, Blocks.E, Blocks.oL, Blocks.c, Blocks.H, Blocks.I, Blocks.qj, Blocks.oU, Blocks.oV, Blocks.qk,
        				//Extra blocks
                        Blocks.D, //Red sand
                        Blocks.ij, //COBBLESTONE slab
                        Blocks.ie, //stone slab
                        Blocks.lJ, //Mossy cobble slab
                        Blocks.lO, //Andesite Slab
                        Blocks.m, //COBBLESTONE
                        Blocks.iS, //Dirt Path
                        Blocks.cM, //Snow block
                        Blocks.bP //Mossy cobblestone
                );
        Field field = WorldGenCarverAbstract.class.getDeclaredField("h");
        if (!field.isAccessible())
            field.setAccessible(true);
        field.set(carverAbstract, immutableCarverList);
    }

    @Override //getSeaLevel
    public int f() {
        return 256;
    }

    @Override //createStructures should be empty
    public void a(IRegistryCustom iregistrycustom, RandomState randomstate, StructureManager structuremanager, IChunkAccess ichunkaccess, StructureTemplateManager structuretemplatemanager, long i) {
    	
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
    protected Codec<? extends ChunkGenerator> b() {
        return ChunkGeneratorAbstract.a;
    }

    //This class is on the verge of getting deleted.
	private class CustomBiomeGrid extends CustomBiomeSupportedBiomeGrid implements BiomeGrid {

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
            	return CraftBlock.biomeBaseToBiome(this.biome.biomeRegistry, this.biome.getNoiseBiome(x >> 2, y >> 2, z >> 2));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			}
        }
		@Override
        public void setBiome(int x, int y, int z, Biome bio) {
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
	public int e() {
		return this.delegate.e();
	}

	
	//getMinY
	@Override
	public int g() {
		return this.delegate.g();
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
