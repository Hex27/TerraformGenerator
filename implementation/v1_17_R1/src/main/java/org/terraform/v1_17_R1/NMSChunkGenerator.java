package org.terraform.v1_17_R1;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EnumCreatureType;
import net.minecraft.world.level.BlockColumn;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.WorldChunkManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ITileEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.BiomeStorage;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.levelgen.ChunkGeneratorAbstract;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldGenStage;
import net.minecraft.world.level.levelgen.carver.WorldGenCarverAbstract;
import net.minecraft.world.level.levelgen.feature.StructureGenerator;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureManager;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class NMSChunkGenerator extends ChunkGenerator {
	private static Class<?> chunkDataClass = null;
	private static Method getTiles;
	private static Method getTypeId;
	private final ChunkGenerator delegate;
    private final WorldServer world;
    private final TerraformPopulator pop;
    private final TerraformWorld tw;
    
    public NMSChunkGenerator(String worldname, int seed,
    						ChunkGenerator delegate,
                             WorldChunkManager worldchunkmanager,
                             WorldChunkManager worldchunkmanager1,
                             StructureSettings structuresettings, long i) {
        super(new CustomBiomeSource(worldchunkmanager), new CustomBiomeSource(worldchunkmanager1), structuresettings, i);
        
        //Time bomb, explodes in about 1-2 years. Written on 28/8/2021
        if(chunkDataClass == null) {
        	try {
            	chunkDataClass = Class.forName("org.bukkit.craftbukkit.v1_17_R1.generator.OldCraftChunkData");
            	TerraformGeneratorPlugin.logger.stdout("Detected new worldgen API. Adjusting accordingly.");
            }
            catch(ClassNotFoundException e)
            {
            	try {
    				chunkDataClass = Class.forName("org.bukkit.craftbukkit.v1_17_R1.generator.CraftChunkData");
    	        	TerraformGeneratorPlugin.logger.stdout("Detected old worldgen API. Adjusting accordingly.");
    			} catch (ClassNotFoundException e1) {
    				//If this fails again, just throw the exception and crash
    				e1.printStackTrace();
    			}
            }
            try {
    			getTiles = chunkDataClass.getDeclaredMethod("getTiles");
              	getTiles.setAccessible(true);
              	getTypeId = chunkDataClass.getDeclaredMethod("getTypeId", int.class, int.class, int.class);
    		} catch (NoSuchMethodException | SecurityException e1) {
    			e1.printStackTrace();
    		}
        }
        
        tw = TerraformWorld.get(worldname, seed);
        this.delegate = delegate;
        pop = new TerraformPopulator(tw);
        world = ((CraftWorld) Bukkit.getWorld(worldname)).getHandle();
        
        try {
            modifyCaveCarverLists(WorldGenCarverAbstract.a);
            modifyCaveCarverLists(WorldGenCarverAbstract.b);
            modifyCaveCarverLists(WorldGenCarverAbstract.c);
            modifyCaveCarverLists(WorldGenCarverAbstract.d);
            modifyCaveCarverLists(WorldGenCarverAbstract.e);
        } catch (Exception e) {
            TerraformGeneratorPlugin.logger.error("Failed to modify vanilla cave carver lists. You may see floating blocks above caves.");
            e.printStackTrace();
        }
    }

    public TerraformWorld getTerraformWorld() {
        return tw;
    }

    @Override
    public void createBiomes(IRegistry<BiomeBase> iregistry, IChunkAccess ichunkaccess) {
        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
        ((ProtoChunk)ichunkaccess).a(new BiomeStorage(iregistry, ichunkaccess, chunkcoordintpair, this.c));
     }

    @Override
    public BlockPosition findNearestMapFeature(WorldServer worldserver, StructureGenerator<?> structuregenerator, BlockPosition blockposition, int i, boolean flag) {
	//      PILLAGER_OUTPOST -> a
	//      MINESHAFT -> b
	//      MINESHAFT_MESA -> c
	//      WOODLAND_MANSION -> d
	//      JUNGLE_TEMPLE -> e
	//      DESERT_PYRAMID -> f
	//      IGLOO -> g
	//      SHIPWRECK -> h
	//      SHIPWRECH_BEACHED -> i
	//      SWAMP_HUT -> j
	//      STRONGHOLD -> k
	//      OCEAN_MONUMENT -> l
	//      OCEAN_RUIN_COLD -> m
	//      OCEAN_RUIN_WARM -> n
	//      NETHER_BRIDGE -> o
	//      NETHER_FOSSIL -> p
	//      END_CITY -> q
	//      BURIED_TREASURE -> r
	//      BASTION_REMNANT -> s
	//      VILLAGE_PLAINS -> t
	//      VILLAGE_DESERT -> u
	//      VILLAGE_SAVANNA -> v
	//      VILLAGE_SNOWY -> w
	//      VILLAGE_TAIGA -> x
	//      RUINED_PORTAL_STANDARD -> y
	//      RUINED_PORTAL_DESERT -> z
	//      RUINED_PORTAL_JUNGLE -> A
	//      RUINED_PORTAL_SWAMP -> B
	//      RUINED_PORTAL_MOUNTAIN -> C
	//      RUINED_PORTAL_OCEAN -> D
	//      RUINED_PORTAL_NETHER -> E
    	
    	//StructureGenerator<?> structuregenerator = (StructureGenerator) WorldGenerator.ao.get(s.toLowerCase(Locale.ROOT));
        int pX = blockposition.getX();
        int pZ = blockposition.getZ();
        TerraformGeneratorPlugin.logger.info("Vanilla locate for " + structuregenerator.getClass().getName() + " invoked.");

        if (structuregenerator == StructureGenerator.k) { //stronghold
            int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
            return new BlockPosition(coords[0], 20, coords[1]);
        } 
        else if(!TConfigOption.DEVSTUFF_VANILLA_LOCATE_DISABLE.getBoolean())
        {
        	if (structuregenerator == StructureGenerator.l) { //Monument
                
        		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MonumentPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
                return new BlockPosition(coords[0], 50, coords[1]);
            } else if (structuregenerator == StructureGenerator.d) { //Mansion
                    
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

    @Override
    public void addDecorations(RegionLimitedWorldAccess rlwa, StructureManager structuremanager) {
        int chunkX = rlwa.a().b; //x
        int chunkZ = rlwa.a().c; //z
        PopulatorData popDat = new PopulatorData(rlwa, this, chunkX, chunkZ);
        pop.populate(tw, rlwa.getRandom(), popDat);

    }

    @Override
    public void doCarving(long i, BiomeManager biomemanager, IChunkAccess ichunkaccess, WorldGenStage.Features worldgenstage_features) {
    	super.doCarving(i, biomemanager, ichunkaccess, worldgenstage_features);
    }

    /**
     * Used to modify cave carvers in vanilla to carve some other blocks.
     * @param carverAbstract
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @SuppressWarnings({ "deprecation" })
    private void modifyCaveCarverLists(WorldGenCarverAbstract<?> carverAbstract) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Set<net.minecraft.world.level.block.Block> immutableCarverList =
                ImmutableSet.of(
                		//Defaults
                		//Lolmao kms
                		Blocks.b, Blocks.c, Blocks.e, Blocks.g, Blocks.j, Blocks.k, 
                		Blocks.l, Blocks.i, Blocks.hf, Blocks.fS, Blocks.fT, Blocks.fU, Blocks.fV, Blocks.fW, Blocks.fX, Blocks.fY, Blocks.fZ, Blocks.ga, Blocks.gb, Blocks.gc, Blocks.gd, Blocks.ge, Blocks.gf, Blocks.gg, Blocks.gh, Blocks.az, Blocks.hU, Blocks.ec, Blocks.cK, Blocks.hh, Blocks.pN, Blocks.oL, Blocks.c, Blocks.H, Blocks.I, Blocks.qj, Blocks.oU, Blocks.oV, Blocks.qk,
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
        Field field = WorldGenCarverAbstract.class.getDeclaredField("k");
        if (!field.isAccessible())
            field.setAccessible(true);
        field.set(carverAbstract, immutableCarverList);
    }

    @Override
    public int getSeaLevel() {
        return TerraformGenerator.seaLevel;
    }

    @Override
    public void createStructures(IRegistryCustom iregistrycustom, StructureManager structuremanager, IChunkAccess ichunkaccess, DefinedStructureManager definedstructuremanager,
                                 long i) {

    }

    public int getSpawnHeight(LevelHeightAccessor levelheightaccessor) {
        return 64;
     }

    @Override
    public CompletableFuture<IChunkAccess> buildNoise(Executor executor, StructureManager structuremanager, IChunkAccess ichunkaccess) {
        return CompletableFuture.completedFuture(ichunkaccess);
     }

  	@SuppressWarnings("unchecked")
    @Override
    public void buildBase(RegionLimitedWorldAccess regionlimitedworldaccess, IChunkAccess ichunkaccess) {
        try {
        	TerraformGenerator generator = new TerraformGenerator();
            int x = ichunkaccess.getPos().b;
            int z = ichunkaccess.getPos().c;
        	Random random = tw.getRand(3);
        	random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
            
        	CustomBiomeGrid biomegrid = new CustomBiomeGrid(new BiomeStorage(this.world.t().d(IRegistry.aO), regionlimitedworldaccess, ichunkaccess.getPos(), this.getWorldChunkManager()));
            org.bukkit.generator.ChunkGenerator.ChunkData data;
            if (generator.isParallelCapable()) {
               data = generator.generateChunkData(this.world.getWorld(), random, x, z, biomegrid);
            } else {
               synchronized(this) {
                  data = generator.generateChunkData(this.world.getWorld(), random, x, z, biomegrid);
               }
            }

            //Preconditions.checkArgument(data instanceof CraftChunkData, "Plugins must use createChunkData(World) rather than implementing ChunkData: %s", data);
            //Object craftData = data;
            
            //ChunkSection[] sections = craftData.getRawChunkData();
            Method getRawChunkData = chunkDataClass.getDeclaredMethod("getRawChunkData");
            getRawChunkData.setAccessible(true);
            ChunkSection[] sections = (ChunkSection[]) getRawChunkData.invoke(data);
            
            
            ChunkSection[] csect = ichunkaccess.getSections();
            int scnt = Math.min(csect.length, sections.length);

            for(int sec = 0; sec < scnt; ++sec) {
               if (sections[sec] != null) {
                  ChunkSection section = sections[sec];
                  csect[sec] = section;
               }
            }

            //Sets this chunk's biomegrid to that biome.
            ((ProtoChunk)ichunkaccess).a(biomegrid.biome);
          	Set<BlockPosition> tiles = (Set<BlockPosition>) getTiles.invoke(data);
            if (tiles != null) {
               Iterator<BlockPosition> var20 = tiles.iterator();

               while(var20.hasNext()) {
                  BlockPosition pos = (BlockPosition)var20.next();
                  int tx = pos.getX();
                  int ty = pos.getY();
                  int tz = pos.getZ();
                  IBlockData block = (IBlockData) getTypeId.invoke(data, tx, ty, tz);
                  if (block.isTileEntity()) {
                     TileEntity tile = ((ITileEntity)block).createTile(new BlockPosition((x << 4) + tx, ty, (z << 4) + tz), block);
                     ichunkaccess.setTileEntity(tile);
                  }
               }
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public WeightedRandomList getMobsFor(BiomeBase biomebase, StructureManager structuremanager, EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
    	
    	if (structuremanager.a(blockposition, true, StructureGenerator.j).e()) {
	      if (enumcreaturetype == EnumCreatureType.a) { //MobCategory.MONSTER
	          return StructureGenerator.j.c(); //Swamp Hut
	      }
	
	      if (enumcreaturetype == EnumCreatureType.b) { //MobCategory.CREATURE
	          return StructureGenerator.j.h(); //Swamp Hut
	      }
	  }
	
	  if (enumcreaturetype == EnumCreatureType.a) { //MobCategory.MONSTER
	      if (structuremanager.a(blockposition, false, StructureGenerator.b).e()) {
	          return StructureGenerator.b.c(); //pillager outpost
	      }
	
	      if (structuremanager.a(blockposition, false, StructureGenerator.l).e()) {
	          return StructureGenerator.l.c(); //Ocean Monument
	      }
	
	      if (structuremanager.a(blockposition, true, StructureGenerator.n).e()) {
	          return StructureGenerator.n.c(); //Nether Bridge???
	      }
	  }

    	return biomebase.b().a(enumcreaturetype);
     }

    @Override
    public int getBaseHeight(int x, int z, HeightMap.Type var2, LevelHeightAccessor var3) {
        return org.terraform.coregen.HeightMap.getBlockHeight(tw, x, z);
    }

    @Override
    protected Codec<? extends ChunkGenerator> a() {
        return ChunkGeneratorAbstract.d;
    }

    private static boolean debug = true;
    private static Field biomeBaseRegistry = null;
    private class CustomBiomeGrid extends CustomBiomeSupportedBiomeGrid implements BiomeGrid {

        private final BiomeStorage biome;

        public CustomBiomeGrid(BiomeStorage biome) {
            this.biome = biome;
            if(biomeBaseRegistry == null) {
            	try {
					biomeBaseRegistry = BiomeStorage.class.getField("registry");
				} catch (NoSuchFieldException e) {
					try {
						biomeBaseRegistry = BiomeStorage.class.getField("e");
					} catch (Throwable e1) {
						e1.printStackTrace();
					}
				}
            }
        }

        @Override
        public Biome getBiome(int x, int z) {
            return getBiome(x, 0, z);
        }

        @Override
        public void setBiome(int x, int z, Biome bio) {
            for (int y = 0; y < tw.getWorld().getMaxHeight(); y++) {
                setBiome(x, y, z, bio);
            }
        }

        @SuppressWarnings("unchecked")
		@Override
        public Biome getBiome(int x, int y, int z) {
            try {
            	if(debug) {
	            	BiomeBase sad = biome.getBiome(x >> 2, y >> 2, z >> 2);
	            	if(sad.g() == 16711680) {
	            		TerraformGeneratorPlugin.logger.info("GET-biome called! Water color correct.");
	            		debug = false;
	            	}
            	}
				return CraftBlock.biomeBaseToBiome((IRegistry<BiomeBase>) biomeBaseRegistry.get(biome), biome.getBiome(x >> 2, y >> 2, z >> 2));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
        }

        @SuppressWarnings("unchecked")
		@Override
        public void setBiome(int x, int y, int z, Biome bio) {
            try {
				biome.setBiome(x >> 2, y >> 2, z >> 2, CraftBlock.biomeToBiomeBase((IRegistry<BiomeBase>) biomeBaseRegistry.get(biome), bio));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
        }
        
		@Override
		public void setBiome(int x, int y, int z, CustomBiomeType bio, Biome fallback) {
			// TODO Auto-generated method stub
			BiomeBase base = null;
			DedicatedServer dedicatedserver = ((CraftServer) Bukkit.getServer()).getServer();
	        IRegistryWritable<BiomeBase> registrywritable = dedicatedserver.getCustomRegistry().b(IRegistry.aO);
	        
			ResourceKey<BiomeBase> rkey = ResourceKey.a(IRegistry.aO, new MinecraftKey(bio.getKey()));
	        base = registrywritable.a(rkey);
	        if(base == null) {
	        	String[] split = bio.getKey().split(":");
	            ResourceKey<BiomeBase> newrkey = ResourceKey.a(IRegistry.aO, new MinecraftKey(split[0],split[1]));
	            base = registrywritable.a(newrkey);
	        }
			
			if(base != null) {
				biome.setBiome(x >> 2, y >> 2, z >> 2, base);
			}
			else
				setBiome(x,y,z,fallback);
			
//            if(debug) {
//            	BiomeBase sad = biome.getBiome(x >> 2, y >> 2, z >> 2);
//            	TerraformGeneratorPlugin.logger.info("Water Color: " + sad.g());
//            	debug = false;
//            }
		}
    }
    
	@Override
	public BlockColumn getBaseColumn(int var0, int var1, LevelHeightAccessor var2) {
		
		return this.delegate.getBaseColumn(var0, var1, var2);
	}
	
	@Override
	public ChunkGenerator withSeed(long arg0) {
		return new NMSChunkGenerator(this.tw.getName(), (int) arg0, this.delegate, this.getWorldChunkManager(), this.getWorldChunkManager(), this.getSettings(), 0L);
	}
}
