package org.terraform.coregen.v1_16_R1;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.HeightMap.Type;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R1.generator.CraftChunkData;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.farmhouse.FarmhousePopulator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class NMSChunkGenerator extends ChunkGenerator {
    private final TerraformPopulator pop;
    private final TerraformWorld tw;

    public NMSChunkGenerator(String worldname, int seed, WorldChunkManager worldchunkmanager,
                             WorldChunkManager worldchunkmanager1,
                             StructureSettings structuresettings, long i) {
        super(worldchunkmanager, worldchunkmanager1, structuresettings, i);
        tw = TerraformWorld.get(worldname, seed);
        pop = new TerraformPopulator(tw);
        try {
            modifyCaveCarverLists(WorldGenCarverAbstract.a);
            modifyCaveCarverLists(WorldGenCarverAbstract.b);
            modifyCaveCarverLists(WorldGenCarverAbstract.c);
            modifyCaveCarverLists(WorldGenCarverAbstract.d);
            modifyCaveCarverLists(WorldGenCarverAbstract.e);
        }
        catch(Exception e) {
        	TerraformGeneratorPlugin.logger.error("Failed to modify vanilla cave carver lists. You may see floating blocks above caves.");
        	e.printStackTrace();
        }
    }

    public TerraformWorld getTerraformWorld() {
        return tw;
    }

    @Override
    public void createBiomes(IChunkAccess ichunkaccess) {

        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();

        ((ProtoChunk) ichunkaccess).a(new BiomeStorage(chunkcoordintpair, this.c));

        int chunkX = ichunkaccess.getPos().x;
        int chunkZ = ichunkaccess.getPos().z;
        for (int x = chunkX * 16; x < chunkX * 16 + 16; x++) {
            for (int z = chunkZ * 16; z < chunkZ * 16 + 16; z++) {

                int y = org.terraform.coregen.HeightMap.getHeight(tw, x, z);
                BiomeBase b = CraftBlock.biomeToBiomeBase(tw.getBiomeBank(x, y, z).getHandler().getBiome()); //BiomeBank.calculateBiome(tw,tw.getTemperature(x,z), y).getHandler
                // ().getBiome()

                //2D Biomes.
                for (int h = 0; h < 256; h++) {
                    ichunkaccess.getBiomeIndex().setBiome(x, h, z, b);
                }
            }
        }
    }

//	@Override
//	public void createStructures(BiomeManager biomemanager, IChunkAccess ica, ChunkGenerator chunkgenerator, DefinedStructureManager definedstructuremanager) {
//		int chunkX = ica.getPos().x;
//        int chunkZ = ica.getPos().z;
//        
//        PopulatorDataICA popDat = new PopulatorDataICA(tw,this.getWorld().getWorld().getHandle(),ica,this,chunkX,chunkZ);
//        pop.populate(tw, this.getWorld().getWorld().getHandle().getRandom(), popDat);
//	}

    @Override
    public BlockPosition findNearestMapFeature(WorldServer worldserver, StructureGenerator<?> structuregenerator, BlockPosition blockposition, int i, boolean flag) {
        //StructureGenerator<?> structuregenerator = (StructureGenerator) WorldGenerator.ao.get(s.toLowerCase(Locale.ROOT));
        int pX = blockposition.getX();
        int pZ = blockposition.getZ();
        if (structuregenerator == StructureGenerator.STRONGHOLD) {
//			double minDistanceSquared = Integer.MAX_VALUE;
//			int[] min = null;
//			for(int[] loc:StrongholdPopulator.strongholdPositions(tw)){
//				double distSqr = Math.pow(loc[0]-pX,2) + Math.pow(loc[1]-pZ,2);
//				if(distSqr < minDistanceSquared){
//					minDistanceSquared = distSqr;
//					min = loc;
//				}
//			}
            int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
            return new BlockPosition(coords[0], 20, coords[1]);
        } else if (structuregenerator == StructureGenerator.VILLAGE) {
            int[] coords = new FarmhousePopulator().getNearestFeature(tw, pX, pZ);
            return new BlockPosition(coords[0], 100, coords[1]);
        } else if (structuregenerator == StructureGenerator.MONUMENT) {
            int[] coords = new MonumentPopulator().getNearestFeature(tw, pX, pZ);
            return new BlockPosition(coords[0], 100, coords[1]);
        }

        return null;
    }

    @Override
    public void addDecorations(RegionLimitedWorldAccess rlwa, StructureManager structuremanager) {
        int chunkX = rlwa.a();
        int chunkZ = rlwa.b();
        PopulatorData popDat = new PopulatorData(rlwa, this, chunkX, chunkZ);
        pop.populate(tw, rlwa.getRandom(), popDat);
//        for(int relX = 0; relX < 16; relX++){
//        	for(int relZ = 0; relZ < 16; relZ++){
//            	int rawX = chunkX*16+relX;
//            	int rawZ = chunkZ*16+relZ;
//            	int y = 256;
//            	if(rlwa.getRandom().nextInt(5) != 1) continue;
//            	IBlockData type = Blocks.AIR.getBlockData();
//            	BlockPosition highest = null;
//            	while(!type.getMaterial().isSolid()){
//            		y--;
//                	highest = new BlockPosition(rawX,y,rawZ);
//            		type = rlwa.getType(highest);
//            	}
//            	highest = new BlockPosition(rawX,y+1,rawZ);
//            	rlwa.setTypeAndData(highest, Blocks.GRASS.getBlockData(), 0);
//            	
//            	if(rlwa.getRandom().nextInt(9) == 1){
//            		spawnRock(rlwa.getRandom(),rlwa,highest);
//            	}
//            }
//        }
    }

    private BiomeBase getBiome(BiomeManager biomemanager, BlockPosition bp) {
        return CraftBlock.biomeToBiomeBase(tw.getBiomeBank(bp.getX(), bp.getY(), bp.getZ()).getHandler().getBiome());
    }

    @Override
    public void doCarving(long seed, BiomeManager biomemanager, IChunkAccess ichunkaccess, WorldGenStage.Features worldgenstage_features) {
        SeededRandom seededrandom = new SeededRandom();

        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();
        int chunkX = chunkcoordintpair.x;
        int chunkZ = chunkcoordintpair.z;
        BiomeBase biomebase = this.getBiome(biomemanager, chunkcoordintpair.l());
        BitSet bitset = ((ProtoChunk) ichunkaccess).b(worldgenstage_features);

        for (int k = chunkX - 8; k <= chunkX + 8; ++k) {
            for (int l = chunkZ - 8; l <= chunkZ + 8; ++l) {
                List<WorldGenCarverWrapper<?>> list = biomebase.a(worldgenstage_features);
                ListIterator<WorldGenCarverWrapper<?>> listiterator = list.listIterator();

                while (listiterator.hasNext()) {
                    int i1 = listiterator.nextIndex();
                    WorldGenCarverWrapper<?> worldgencarverwrapper = listiterator.next();
                    try {
                        Field field = WorldGenCarverWrapper.class.getDeclaredField("b");
                        if (!field.isAccessible())
                            field.setAccessible(true);
                        String carverType = field.get(worldgencarverwrapper).getClass().getSimpleName();
                        if (carverType.equals("WorldGenCanyonOcean") ||
                                carverType.equals("WorldGenCavesOcean")) {
                            continue;
                        }
                    } catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    seededrandom.c(seed + (long) i1, k, l);
                    if (worldgencarverwrapper.a(seededrandom, k, l)) {
                        worldgencarverwrapper.a(ichunkaccess, (blockposition) -> {
                            return this.getBiome(biomemanager, blockposition);
                        }, seededrandom, this.getSeaLevel(), k, l, chunkX, chunkZ, bitset);
                    }
                }
            }
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
    @SuppressWarnings("rawtypes")
	private void modifyCaveCarverLists(WorldGenCarverAbstract carverAbstract) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	Set<net.minecraft.server.v1_16_R1.Block> immutableCarverList = 
    			ImmutableSet.of(
    					//vanilla blocks
    					Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.PACKED_ICE,
    			        //Extra blocks
    					Blocks.RED_SAND,
    					Blocks.COBBLESTONE_SLAB,
    					Blocks.COBBLESTONE,
    					Blocks.GRASS_PATH,
    					Blocks.SNOW_BLOCK
    					);
    	Field field = WorldGenCarverAbstract.class.getDeclaredField("j");
        if (!field.isAccessible())
            field.setAccessible(true);
        field.set(carverAbstract, immutableCarverList);
    }
    
    @Override
    public int getSeaLevel() {
        return TerraformGenerator.seaLevel;
    }

    @Override
    public void createStructures(StructureManager structuremanager, IChunkAccess ichunkaccess, DefinedStructureManager definedstructuremanager, long i) {

    }

    @Override
    public int getSpawnHeight() {
        return getBaseHeight(0, 0, null);
    }


    @Override
    public void buildNoise(GeneratorAccess generatoraccess, StructureManager structuremanager, IChunkAccess ichunkaccess) {

    }

    @Override
    public void buildBase(RegionLimitedWorldAccess regionlimitedworldaccess, IChunkAccess ichunkaccess) {
        try {
            int x = ichunkaccess.getPos().x;
            int z = ichunkaccess.getPos().z;
            TerraformGenerator generator = new TerraformGenerator();
            Random random = tw.getRand(3);
            random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);

            // Get default biome data for chunk
            CustomBiomeGrid biomegrid = new CustomBiomeGrid(new BiomeStorage(ichunkaccess.getPos(), this.getWorldChunkManager()));

            ChunkData data;
            if (generator.isParallelCapable()) {
                data = generator.generateChunkData(tw.getWorld(), random, x, z, biomegrid);
            } else {
                synchronized (this) {
                    data = generator.generateChunkData(tw.getWorld(), random, x, z, biomegrid);
                }
            }

            CraftChunkData craftData = (CraftChunkData) data;
            Method getRawChunkData = CraftChunkData.class.getDeclaredMethod("getRawChunkData");
            getRawChunkData.setAccessible(true);
            ChunkSection[] sections = (ChunkSection[]) getRawChunkData.invoke(craftData);

            ChunkSection[] csect = ichunkaccess.getSections();
            int scnt = Math.min(csect.length, sections.length);

            // Loop through returned sections
            for (int sec = 0; sec < scnt; sec++) {
                if (sections[sec] == null) {
                    continue;
                }
                ChunkSection section = sections[sec];

                csect[sec] = section;
            }

            // Set biome grid
            ((ProtoChunk) ichunkaccess).a(biomegrid.biome);

            Method getTiles;
            getTiles = CraftChunkData.class.getDeclaredMethod("getTiles");
            getTiles.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<BlockPosition> tiles = (Set<BlockPosition>) getTiles.invoke(craftData);
            if (tiles != null) {
                for (BlockPosition pos : tiles) {
                    int tx = pos.getX();
                    int ty = pos.getY();
                    int tz = pos.getZ();
                    net.minecraft.server.v1_16_R1.Block block = craftData.getTypeId(tx, ty, tz).getBlock();

                    if (block.isTileEntity()) {
                        TileEntity tile = ((ITileEntity) block).createTile(((CraftWorld) tw.getWorld()).getHandle());
                        ichunkaccess.setTileEntity(new BlockPosition((x << 4) + tx, ty, (z << 4) + tz), tile);
                    }
                }
            }

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

//	@Override
//	public void buildNoise(GeneratorAccess generatoraccess, IChunkAccess ichunkaccess) {
//		BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
//        TerraformGenerator gen = new TerraformGenerator();
//        CustomBiomeGrid biomegrid = new CustomBiomeGrid(new BiomeStorage(ichunkaccess.getPos(), this.getWorldChunkManager()));
//        
//        ChunkData cd = gen.generateChunkData(tw.getWorld(), tw.getRand(3), ichunkaccess.getPos().x, ichunkaccess.getPos().z, biomegrid);
//        
//        for(int x = 0; x < 16; x++){
//        	for(int z = 0; z < 16; z++){
//        		for(int y = 0; y < 255; y++){
//    				int rawX = ichunkaccess.getPos().x*16 + x;
//    				int rawZ = ichunkaccess.getPos().z*16 + z;
//                	ichunkaccess.getBiomeIndex().setBiome(x, y, z, CraftBlock.biomeToBiomeBase(biomegrid.getBiome(x,z)));
//                	generatoraccess.setTypeAndData(blockposition_mutableblockposition.d(x,y,z), CraftBlockData.newData(cd.getType(x,y,z), "").getState(), 0);
//                }
//            }
//        }
////		for (int x = 0; x < 16; ++x) {
////            for (int z = 0; z < 16; ++z) {
////				int rawX = ichunkaccess.getPos().x*16 + x;
////				int rawZ = ichunkaccess.getPos().z*16 + z;
////            	//double n = noise.GetNoise(rawX, rawZ)*7 + currentHeight;
////				for(int y = (int) n; y >= 0; y--){
////					ichunkaccess.getBiomeIndex().setBiome(x, y, z, Biomes.PLAINS);
////					//chunk.setBlock(x, y, z, Material.GRASS_BLOCK);
////					IBlockData iblockdata = Blocks.GRASS_BLOCK.getBlockData();//Material.GRASS_BLOCK.createBlockData();
////	                
////					if(y < n-3){
////	                	iblockdata = Blocks.STONE.getBlockData();
////	                }
////					ichunkaccess.setType(blockposition_mutableblockposition.d(x,y,z), iblockdata, false);
////	                
////				}
//////                heightmap.a(j, i, k, iblockdata);
//////                heightmap1.a(j, i, k, iblockdata);
////            }
////        }
//	
//	}

    @Override
    public List<BiomeBase.BiomeMeta> getMobsFor(BiomeBase biomebase, StructureManager structuremanager, EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
        if (structuremanager.a(blockposition, true, StructureGenerator.SWAMP_HUT).e()) {
            if (enumcreaturetype == EnumCreatureType.MONSTER) {
                return StructureGenerator.SWAMP_HUT.c();
            }

            if (enumcreaturetype == EnumCreatureType.CREATURE) {
                return StructureGenerator.SWAMP_HUT.j();
            }
        }

        if (enumcreaturetype == EnumCreatureType.MONSTER) {
            if (structuremanager.a(blockposition, false, StructureGenerator.PILLAGER_OUTPOST).e()) {
                return StructureGenerator.PILLAGER_OUTPOST.c();
            }

            if (structuremanager.a(blockposition, false, StructureGenerator.MONUMENT).e()) {
                return StructureGenerator.MONUMENT.c();
            }

            if (structuremanager.a(blockposition, true, StructureGenerator.FORTRESS).e()) {
                return StructureGenerator.FORTRESS.c();
            }
        }

        return super.getMobsFor(biomebase, structuremanager, enumcreaturetype, blockposition);
    }

    @Override
    public int getBaseHeight(int i, int j, Type heightmap_type) {
        return org.terraform.coregen.HeightMap.getHeight(tw, i, j);
    }

    @Override
    protected Codec<? extends ChunkGenerator> a() {
        return ChunkGeneratorAbstract.d;
    }

    @Override //getBaseColumn
    public IBlockAccess a(int i, int j) {
//       IBlockData[] aiblockdata = new IBlockData[this.o * 256];
//
        //iterateNoiseColumn
//       this.a(i, j, aiblockdata, (Predicate) null);
//       return new BlockColumn(aiblockdata);
        return null;
    }

    private class CustomBiomeGrid implements BiomeGrid {

        private final BiomeStorage biome;

        public CustomBiomeGrid(BiomeStorage biome) {
            this.biome = biome;
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

        @Override
        public Biome getBiome(int x, int y, int z) {
            return CraftBlock.biomeBaseToBiome(biome.getBiome(x, y, z));
        }

        @Override
        public void setBiome(int x, int y, int z, Biome bio) {
            biome.setBiome(x, y, z, CraftBlock.biomeToBiomeBase(bio));
        }
    }
}
