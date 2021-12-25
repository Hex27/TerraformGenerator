package org.terraform.v1_18_R1;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;

import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EnumCreatureType;
import net.minecraft.world.level.BlockColumn;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSettingsMobs;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.WorldChunkManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.levelgen.ChunkGeneratorAbstract;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.WorldGenStage;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.WorldGenCarverAbstract;
import net.minecraft.world.level.levelgen.feature.StructureGenerator;
import net.minecraft.world.level.levelgen.feature.WorldGenFeaturePillagerOutpost;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureSwampHut;
import net.minecraft.world.level.levelgen.feature.WorldGenMonument;
import net.minecraft.world.level.levelgen.feature.WorldGenNether;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureManager;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.terraform.biome.custombiomes.CustomBiomeSupportedBiomeGrid;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.StructureLocator;
import org.terraform.structure.buriedtreasure.BuriedTreasurePopulator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.pillager.mansion.MansionPopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public class NMSChunkGenerator extends ChunkGenerator {
	private final ChunkGenerator delegate;
    
	@SuppressWarnings("unused")
	private final WorldServer world;
    private final TerraformPopulator pop;
    private final TerraformWorld tw;
    
	public NMSChunkGenerator(String worldname, long seed,
    						ChunkGenerator delegate) {
    	super(new TerraformWorldProviderBiome(TerraformWorld.get(worldname,seed), delegate.e()), delegate.d());

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
    

    @Override
    public WorldChunkManager e() {
    	if(!(b instanceof TerraformWorldProviderBiome))
        	TerraformGeneratorPlugin.logger.error("b was not an instance of TerraformWorldProviderBiome!");

        return this.b;
    }
    
    public TerraformWorld getTerraformWorld() {
        return tw;
    }

    //createBiomes
    @Override
    public CompletableFuture<IChunkAccess> a(IRegistry<BiomeBase> iregistry, Executor executor, Blender blender, StructureManager structuremanager, IChunkAccess ichunkaccess) {

    	return CompletableFuture.supplyAsync(SystemUtils.a("init_biomes", () -> {
             return ichunkaccess; //Don't do any calculations here, biomes are set in applyCarvers
         }), SystemUtils.f());
     }
    

    @Override //findNearestMapFeature
    public BlockPosition a(WorldServer worldserver, StructureGenerator<?> structuregenerator, BlockPosition blockposition, int i, boolean flag) {
//    	PILLAGER_OUTPOST -> c
//    	MINESHAFT -> d
//    	WOODLAND_MANSION -> e
//    	JUNGLE_TEMPLE -> f
//    	DESERT_PYRAMID -> g
//    	IGLOO -> h
//    	RUINED_PORTAL -> i
//    	SHIPWRECK -> j
//    	SWAMP_HUT -> k
//    	STRONGHOLD -> l
//    	OCEAN_MONUMENT -> m
//    	OCEAN_RUIN -> n
//    	NETHER_BRIDGE -> o
//    	END_CITY -> p
//    	BURIED_TREASURE -> q
//    	VILLAGE -> r
//    	NETHER_FOSSIL -> s
//    	BASTION_REMNANT -> t
    	
    	//StructureGenerator<?> structuregenerator = (StructureGenerator) WorldGenerator.ao.get(s.toLowerCase(Locale.ROOT));
        int pX = blockposition.u(); //getX
        int pZ = blockposition.w(); //getZ
        TerraformGeneratorPlugin.logger.info("Vanilla locate for " + structuregenerator.getClass().getName() + " invoked.");

        if (structuregenerator == StructureGenerator.l) { //stronghold
            int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
            return new BlockPosition(coords[0], 20, coords[1]);
        } 
        else if(!TConfigOption.DEVSTUFF_VANILLA_LOCATE_DISABLE.getBoolean())
        {
        	if (structuregenerator == StructureGenerator.m) { //Monument
                
        		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MonumentPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
                return new BlockPosition(coords[0], 50, coords[1]);
            } else if (structuregenerator == StructureGenerator.e) { //Mansion
                    
        		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MansionPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
                return new BlockPosition(coords[0], 50, coords[1]);
            } else if (structuregenerator.getClass().getName().equals("net.minecraft.world.level.levelgen.feature.WorldGenBuriedTreasure")) { 
            	//Buried Treasure
            	int[] coords = StructureLocator.locateMultiMegaChunkStructure(tw, new MegaChunk(pX, 0, pZ), new BuriedTreasurePopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
            	return new BlockPosition(coords[0], 50, coords[1]);
            }
        }

        return null;
    }

    @Override //applyBiomeDecoration
    public void a(GeneratorAccessSeed generatoraccessseed, IChunkAccess ichunkaccess, StructureManager structuremanager) {
    	int chunkX = ichunkaccess.f().c; //x
        int chunkZ = ichunkaccess.f().d; //z
        PopulatorData popDat = new PopulatorData(generatoraccessseed, ichunkaccess, this, chunkX, chunkZ);
        pop.populate(tw, tw.getHashedRand(8292012, chunkX, chunkZ), popDat);
    }

    @Override //applyCarving.
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess, long var2, BiomeManager var4, StructureManager var5, IChunkAccess ichunkaccess, WorldGenStage.Features var7) {
    	//int chunkX = var1.a().c;
    	//int chunkZ  =var1.a().d;
    	ichunkaccess.a(this.b::getNoiseBiome, this.c());
    	
  		try {
        	TerraformGenerator generator = new TerraformGenerator();
            int chunkX = ichunkaccess.f().c; //x
            int chunkZ = ichunkaccess.f().d; //z
        	Random random = tw.getRand(3);
        	random.setSeed((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
            
        	//CustomBiomeGrid biomegrid = new CustomBiomeGrid(new BiomeStorage(this.world.t().d(IRegistry.aO), regionlimitedworldaccess, ichunkaccess.getPos(), this.getWorldChunkManager()));
        	CustomBiomeGrid biomegrid = new CustomBiomeGrid(ichunkaccess);
        	PopulatorData data = new PopulatorData(regionlimitedworldaccess, ichunkaccess, this, chunkX, chunkZ);
        	data.setRadius(0);
        	generator.addPopulatorData(data);
            ChunkData cd = generator.generateChunkData(tw.getWorld(), random, chunkX, chunkZ, biomegrid);
            
            //Do carving after ground is set.
        	delegate.a(regionlimitedworldaccess, var2, var4, var5, ichunkaccess, var7);
        	
        	//Fill seas after carving.
        	for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int rawX = chunkX * 16 + x;
                    int rawZ = chunkZ * 16 + z;

                    //tw.getBiomeBank(rawX, rawZ);
                    int height = (int) org.terraform.coregen.HeightMap.getBlockHeight(tw, rawX, rawZ);
                    generator.fillSeaAndRivers(cd,x,z,height);
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
    public int g() {
        return 256;
    }

    @Override //createStructures should be empty
    public void a(IRegistryCustom iregistrycustom, StructureManager structuremanager, IChunkAccess ichunkaccess, DefinedStructureManager definedstructuremanager,
                                 long i) {
    	
    }

    @Override //getSpawnHeight
    public int a(LevelHeightAccessor levelheightaccessor) {
        return 64;
     }

    @Override //fillFromNoise
    public CompletableFuture<IChunkAccess> a(Executor executor, Blender blender, StructureManager structuremanager, IChunkAccess ichunkaccess) {
    	return delegate.a(executor, blender, structuremanager, ichunkaccess);
     }
    

//  	@SuppressWarnings("unchecked")
    @Override //buildSurface. Used to be buildBase
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess, StructureManager structuremanager, IChunkAccess ichunkaccess) {

    }
  	
  	@Override //createReferences. Idk what this is
  	public void a(GeneratorAccessSeed gas,StructureManager manager,IChunkAccess ica)
  	{
  		delegate.a(gas, manager, ica);
  	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override //getMobsAt
	//StructureManager is StructureFeatureManager, NOT StructureManager. Spigot remapped it.
    public WeightedRandomList a(BiomeBase biomebase, StructureManager structuremanager, EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
        if (!structuremanager.a(blockposition)) {
            return super.a(biomebase, structuremanager, enumcreaturetype, blockposition);
         } else {
            if (structuremanager.b(blockposition, StructureGenerator.k).b()) { //swamp hut
               if (enumcreaturetype == EnumCreatureType.a) {
                  return WorldGenFeatureSwampHut.a;
               }

               if (enumcreaturetype == EnumCreatureType.b) {
                  return WorldGenFeatureSwampHut.w;
               }
            }

            if (enumcreaturetype == EnumCreatureType.a) {
            	//.b is isValid()
            	//structuremanager.getStructureWithPieceAt(blockpos, structurefeature.X).isValid()
               if (structuremanager.a(blockposition, StructureGenerator.c).b()) {
                  return WorldGenFeaturePillagerOutpost.a;
               }

               if (structuremanager.a(blockposition, StructureGenerator.m).b()) {
                  return WorldGenMonument.a;
               }

               if (structuremanager.b(blockposition, StructureGenerator.o).b()) {
                  return WorldGenNether.a;
               }
            }

            return (enumcreaturetype == EnumCreatureType.e || enumcreaturetype == EnumCreatureType.d) && structuremanager.a(blockposition, StructureGenerator.m).b() ? BiomeSettingsMobs.b : super.a(biomebase, structuremanager, enumcreaturetype, blockposition);
         }
      }


    @Override
    protected Codec<? extends ChunkGenerator> a() {
        return ChunkGeneratorAbstract.d;
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
	public BlockColumn a(int var0, int var1, LevelHeightAccessor var2) {
		
		return this.delegate.a(var0, var1, var2);
	}
	
	@Override //withSeed
	public ChunkGenerator a(long seed) {
		return new NMSChunkGenerator(this.tw.getName(), (int) seed, this.delegate);
	}
	
	//spawnOriginalMobs
	public void a(RegionLimitedWorldAccess regionlimitedworldaccess) {
		this.delegate.a(regionlimitedworldaccess);
	}
	//getGenDepth
	public int f() {
		return this.delegate.f();
	}
	//climateSampler
	public Sampler c() {
		return this.delegate.c();
	}
	
	//getMinY
	@Override
	public int h() {
		return this.delegate.h();
	}
	
	@Override //validBiome
	protected boolean a(IRegistry<BiomeBase> iregistry, Predicate<ResourceKey<BiomeBase>> predicate, BiomeBase biomebase)
	{
		return true;
	}
	
	@Override //getFirstFreeHeight
	public int b(int i, int j, HeightMap.Type heightmap_type, LevelHeightAccessor levelheightaccessor) {
		return this.a(i, j, heightmap_type, levelheightaccessor);
	}
	
	
	@Override //getFirstOccupiedHeight
	public int c(int i, int j, HeightMap.Type heightmap_type, LevelHeightAccessor levelheightaccessor) {
		return this.a(i, j, heightmap_type, levelheightaccessor) - 1;
	}

    @Override //getBaseHeight
    public int a(int x, int z, HeightMap.Type var2, LevelHeightAccessor var3) {
        return delegate.a(x, z, var2, var3);
    	//return 100;
    	//return org.terraform.coregen.HeightMap.getBlockHeight(tw, x, z);
    }
   
    private static boolean biomeDebug = false;
    
    @Override
	public BiomeBase getNoiseBiome(int x, int y, int z) {
    	if(!biomeDebug) {
    		biomeDebug = true;
			TerraformGeneratorPlugin.logger.info("[getNoiseBiome] called for " + x + "," + y + "," + z);
    	}
      return this.b.getNoiseBiome(x, y, z, null);
    }
    
}
