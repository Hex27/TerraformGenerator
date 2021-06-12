package org.terraform.v1_17_R1;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EnumCreatureType;
import net.minecraft.world.level.BlockColumn;
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
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.generator.CraftChunkData;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.StructureLocator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class NMSChunkGenerator extends ChunkGenerator {
    private final WorldServer world;
    private final TerraformPopulator pop;
    private final TerraformWorld tw;

    public NMSChunkGenerator(String worldname, int seed,
                             WorldChunkManager worldchunkmanager,
                             WorldChunkManager worldchunkmanager1,
                             StructureSettings structuresettings, long i) {
        super(worldchunkmanager, worldchunkmanager1, structuresettings, i);
        tw = TerraformWorld.get(worldname, seed);
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
    }

    @Override
    public BlockPosition findNearestMapFeature(WorldServer worldserver, StructureGenerator<?> structuregenerator, BlockPosition blockposition, int i, boolean flag) {
        //StructureGenerator<?> structuregenerator = (StructureGenerator) WorldGenerator.ao.get(s.toLowerCase(Locale.ROOT));
        int pX = blockposition.getX();
        int pZ = blockposition.getZ();

        if (structuregenerator == StructureGenerator.k) { //stronghold
            int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
            return new BlockPosition(coords[0], 20, coords[1]);
        } 
    	else if (structuregenerator == StructureGenerator.l) { //Monument
            if(TConfigOption.DEVSTUFF_VANILLA_LOCATE_DISABLE.getBoolean())
            	return null;
    		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MonumentPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
            return new BlockPosition(coords[0], 50, coords[1]);
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
//        BiomeManager biomemanager1 = biomemanager.a(this.b);
//        SeededRandom seededrandom = new SeededRandom();
//        boolean flag = true;
//        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
//        CarvingContext carvingcontext = new CarvingContext(this);
//        Aquifer aquifer = this.a(ichunkaccess);
//        BitSet bitset = ((ProtoChunk)ichunkaccess).b(worldgenstage_features);
//
//        for(int j = -8; j <= 8; ++j) {
//           for(int k = -8; k <= 8; ++k) {
//              ChunkCoordIntPair chunkcoordintpair1 = new ChunkCoordIntPair(chunkcoordintpair.b + j, chunkcoordintpair.c + k);
//              BiomeSettingsGeneration biomesettingsgeneration = this.b.getBiome(QuartPos.a(chunkcoordintpair1.d()), 0, QuartPos.a(chunkcoordintpair1.e())).e();
//              List list = biomesettingsgeneration.a(worldgenstage_features);
//              ListIterator listiterator = list.listIterator();
//
//              while(listiterator.hasNext()) {
//                 int l = listiterator.nextIndex();
//                 WorldGenCarverWrapper worldgencarverwrapper = (WorldGenCarverWrapper)((Supplier)listiterator.next()).get();
//                 seededrandom.c(i + (long)l, chunkcoordintpair1.b, chunkcoordintpair1.c);
//                 if (worldgencarverwrapper.a((Random)seededrandom)) {
//                    Objects.requireNonNull(biomemanager1);
//                    worldgencarverwrapper.a(carvingcontext, ichunkaccess, biomemanager1(), seededrandom, aquifer, chunkcoordintpair1, bitset);
//                 }
//              }
//           }
//        }
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
    @SuppressWarnings({ "rawtypes", "deprecation" })
    private void modifyCaveCarverLists(WorldGenCarverAbstract carverAbstract) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Set<net.minecraft.world.level.block.Block> immutableCarverList =
                ImmutableSet.of(
                		//Defaults
                		//Lolmao kms
                		Blocks.b, Blocks.c, Blocks.e, Blocks.g, Blocks.j, Blocks.k, 
                		Blocks.l, Blocks.i, Blocks.hf, Blocks.fS, Blocks.fT, Blocks.fU, Blocks.fV, Blocks.fW, Blocks.fX, Blocks.fY, Blocks.fZ, Blocks.ga, Blocks.gb, Blocks.gc, Blocks.gd, Blocks.ge, Blocks.gf, Blocks.gg, Blocks.gh, Blocks.az, Blocks.hU, Blocks.ec, Blocks.cK, Blocks.hh, Blocks.pN, Blocks.oL, Blocks.c, Blocks.H, Blocks.I, Blocks.qj, Blocks.oU, Blocks.oV, Blocks.qk,
                        //Extra blocks
                        Blocks.D, //Red sand
                        Blocks.ij, //COBBLESTONE slab
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

    @SuppressWarnings("rawtypes")
	@Override
    public void buildBase(RegionLimitedWorldAccess regionlimitedworldaccess, IChunkAccess ichunkaccess) {
        try {
//            int x = ichunkaccess.getPos().b;
//            int z = ichunkaccess.getPos().c;
//            TerraformGenerator generator = new TerraformGenerator();
//            Random random = tw.getRand(3);
//            random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
//
//            // Get default biome data for chunk
//            CustomBiomeGrid biomegrid = new CustomBiomeGrid(new BiomeStorage(world.r().b(IRegistry.ay), ichunkaccess.getPos(), this.getWorldChunkManager()));
//
//            ChunkData data;
//            if (generator.isParallelCapable()) {
//                data = generator.generateChunkData(tw.getWorld(), random, x, z, biomegrid);
//            } else {
//                synchronized (this) {
//                    data = generator.generateChunkData(tw.getWorld(), random, x, z, biomegrid);
//                }
//            }
//
//            CraftChunkData craftData = (CraftChunkData) data;
//            Method getRawChunkData = CraftChunkData.class.getDeclaredMethod("getRawChunkData");
//            getRawChunkData.setAccessible(true);
//            ChunkSection[] sections = (ChunkSection[]) getRawChunkData.invoke(craftData);
//
//            ChunkSection[] csect = ichunkaccess.getSections();
//            int scnt = Math.min(csect.length, sections.length);
//
//            // Loop through returned sections
//            for (int sec = 0; sec < scnt; sec++) {
//                if (sections[sec] == null) {
//                    continue;
//                }
//                ChunkSection section = sections[sec];
//
//                csect[sec] = section;
//            }
//
//            // Set biome grid
//            ((ProtoChunk) ichunkaccess).a(biomegrid.biome);
//
//            Method getTiles;
//            getTiles = CraftChunkData.class.getDeclaredMethod("getTiles");
//            getTiles.setAccessible(true);
//            @SuppressWarnings("unchecked")
//            Set<BlockPosition> tiles = (Set<BlockPosition>) getTiles.invoke(craftData);
//            if (tiles != null) {
//                for (BlockPosition pos : tiles) {
//                    int tx = pos.getX();
//                    int ty = pos.getY();
//                    int tz = pos.getZ();
//                    net.minecraft.server.v1_16_R3.Block block = craftData.getTypeId(tx, ty, tz).getBlock();
//
//                    if (block.isTileEntity()) {
//                        TileEntity tile = ((ITileEntity) block).createTile(((CraftWorld) tw.getWorld()).getHandle());
//                        ichunkaccess.setTileEntity(new BlockPosition((x << 4) + tx, ty, (z << 4) + tz), tile);
//                    }
//                }
//            }
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

            Preconditions.checkArgument(data instanceof CraftChunkData, "Plugins must use createChunkData(World) rather than implementing ChunkData: %s", data);
            CraftChunkData craftData = (CraftChunkData)data;
            
            //ChunkSection[] sections = craftData.getRawChunkData();
            Method getRawChunkData = CraftChunkData.class.getDeclaredMethod("getRawChunkData");
            getRawChunkData.setAccessible(true);
            ChunkSection[] sections = (ChunkSection[]) getRawChunkData.invoke(craftData);
            
            
            ChunkSection[] csect = ichunkaccess.getSections();
            int scnt = Math.min(csect.length, sections.length);

            for(int sec = 0; sec < scnt; ++sec) {
               if (sections[sec] != null) {
                  ChunkSection section = sections[sec];
                  csect[sec] = section;
               }
            }

            ((ProtoChunk)ichunkaccess).a(biomegrid.biome);
            Method getTiles;
            getTiles = CraftChunkData.class.getDeclaredMethod("getTiles");
          	getTiles.setAccessible(true);
          	@SuppressWarnings("unchecked")
          	Set<BlockPosition> tiles = (Set<BlockPosition>) getTiles.invoke(craftData);
            if (tiles != null) {
               Iterator var20 = tiles.iterator();

               while(var20.hasNext()) {
                  BlockPosition pos = (BlockPosition)var20.next();
                  int tx = pos.getX();
                  int ty = pos.getY();
                  int tz = pos.getZ();
                  IBlockData block = craftData.getTypeId(tx, ty, tz);
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

//    @Override //getBaseColumn
//    public IBlockAccess a(int i, int j) {
////       IBlockData[] aiblockdata = new IBlockData[this.o * 256];
////
//        //iterateNoiseColumn
////       this.a(i, j, aiblockdata, (Predicate) null);
////       return new BlockColumn(aiblockdata);
//        return null;
//    }

    private static Field biomeBaseRegistry = null;
    private class CustomBiomeGrid implements BiomeGrid {

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
    }
    
	@Override
	public BlockColumn getBaseColumn(int arg0, int arg1, LevelHeightAccessor arg2) {
		return null;
	}

	@Override
	public ChunkGenerator withSeed(long arg0) {
//	    public NMSChunkGenerator(String worldname, int seed,
//                WorldChunkManager worldchunkmanager,
//                WorldChunkManager worldchunkmanager1,
//                StructureSettings structuresettings, long i)
		return new NMSChunkGenerator(this.tw.getName(), (int) arg0, this.getWorldChunkManager(), this.getWorldChunkManager(), this.getSettings(), 0L);
	}
}
