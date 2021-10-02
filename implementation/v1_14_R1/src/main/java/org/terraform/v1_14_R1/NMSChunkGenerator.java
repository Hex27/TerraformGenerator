package org.terraform.v1_14_R1;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.v1_14_R1.*;
import net.minecraft.server.v1_14_R1.HeightMap.Type;

import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_14_R1.generator.CraftChunkData;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.StructureLocator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.pillager.mansion.MansionPopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class NMSChunkGenerator extends ChunkGenerator {
    private final TerraformPopulator pop;
    private final TerraformWorld tw;

    @SuppressWarnings("unchecked")
    public NMSChunkGenerator(GeneratorAccess generatoraccess,
                             WorldChunkManager worldchunkmanager, GeneratorSettingsDefault c0) {
        super(generatoraccess, worldchunkmanager, c0);
        tw = TerraformWorld.get(generatoraccess.getWorldData().getName(), generatoraccess.getWorldData().getSeed());
        pop = new TerraformPopulator(tw);
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

    @Override
    public void createBiomes(IChunkAccess ichunkaccess) {
        //Bukkit.getLogger().info("createBiomes");

        BiomeBase[] biomeBases = new BiomeBase[16 * 16];
        try {

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int rawX = x + ichunkaccess.getPos().x * 16;
                    int rawZ = x + ichunkaccess.getPos().z * 16;
                    //int y = HeightMap.getBlockHeight(tw, rawX, rawZ);
                    BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(tw.getBiomeBank(rawX, rawZ).getHandler().getBiome()); //BiomeBank.calculateBiome(tw,tw.getTemperature(x,
                    // z), y).getHandler().getBiome()

                    biomeBases[(x * 16) + z] = biomeBase;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ichunkaccess.a(biomeBases);
        //Bukkit.getLogger().info("createBiomes-finish");

    }

    @Override
    protected BiomeBase getDecoratingBiome(RegionLimitedWorldAccess rlwa, BlockPosition bp) {
        return CraftBlock.biomeToBiomeBase(tw.getBiomeBank(bp.getX(), bp.getZ()).getHandler().getBiome());
    }

    @Override
    public void addDecorations(RegionLimitedWorldAccess rlwa) {
        //Bukkit.getLogger().info("addDecorations");
        int chunkX = rlwa.a();
        int chunkZ = rlwa.b();
        PopulatorData popDat = new PopulatorData(rlwa, this, chunkX, chunkZ);
        pop.populate(tw, rlwa.getRandom(), popDat);
        //Bukkit.getLogger().info("addDecorations-finish");
    }

    @Override
    public int getSeaLevel() {
        return TerraformGenerator.seaLevel;
    }

    @Override
    public int getSpawnHeight() {
        return 50;
    }


    @Override
    public void buildNoise(GeneratorAccess generatoraccess,
                           IChunkAccess ichunkaccess) {
    }

    @Override
    public void buildBase(IChunkAccess ichunkaccess) {
        //Bukkit.getLogger().info("buildBase");
        try {
            int x = ichunkaccess.getPos().x;
            int z = ichunkaccess.getPos().z;
            TerraformGenerator generator = new TerraformGenerator();
            Random random = tw.getRand(3);
            random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);

            // Get default biome data for chunk
            CustomBiomeGrid biomegrid = new CustomBiomeGrid();
            biomegrid.biome = this.getWorldChunkManager().getBiomeBlock(x << 4, z << 4, 16, 16);

            ChunkData data;
            if (generator.isParallelCapable()) {
                data = generator.generateChunkData(this.tw.getWorld(), random, x, z, biomegrid);
            } else {
                synchronized (this) {
                    data = generator.generateChunkData(this.tw.getWorld(), random, x, z, biomegrid);
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
            ichunkaccess.a(biomegrid.biome);

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
                    net.minecraft.server.v1_14_R1.Block block = craftData.getTypeId(tx, ty, tz).getBlock();

                    if (block.isTileEntity()) {
                        TileEntity tile = ((ITileEntity) block).createTile(((CraftWorld) tw.getWorld()).getHandle());
                        ichunkaccess.setTileEntity(new BlockPosition((x << 4) + tx, ty, (z << 4) + tz), tile);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //Bukkit.getLogger().info("buildBase-finish");
    }

    /**
     * Used to modify cave carvers in vanilla to carve some other blocks.
     * @param carverAbstract
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private void modifyCaveCarverLists(WorldGenCarverAbstract carverAbstract) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Set<net.minecraft.server.v1_14_R1.Block> immutableCarverList =
                ImmutableSet.of(
                        //vanilla blocks
                        Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA,
                        Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA,
                        Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA,
                        Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE,
                        Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.PACKED_ICE,
                        //Extra blocks
                        Blocks.RED_SAND,
                        Blocks.COBBLESTONE_SLAB,
                        Blocks.COBBLESTONE,
                        Blocks.GRASS_PATH,
                        Blocks.SNOW_BLOCK,
                        Blocks.MOSSY_COBBLESTONE
                );
        Field field = WorldGenCarverAbstract.class.getDeclaredField("j");
        if (!field.isAccessible())
            field.setAccessible(true);
        field.set(carverAbstract, immutableCarverList);
    }

    @Override
    public int getBaseHeight(int i, int j, Type heightmap_type) {
        return org.terraform.coregen.HeightMap.getBlockHeight(tw, i, j);
    }

    @Override
    public BlockPosition findNearestMapFeature(World world, String s, BlockPosition blockposition, int i, boolean flag) {
        //StructureGenerator<?> structuregenerator = (StructureGenerator) WorldGenerator.ao.get(s.toLowerCase(Locale.ROOT));
        int pX = blockposition.getX();
        int pZ = blockposition.getZ();
        if (s.equalsIgnoreCase("Stronghold")) {
            int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
            return new BlockPosition(coords[0], 20, coords[1]);
        }
        else if(!TConfigOption.DEVSTUFF_VANILLA_LOCATE_DISABLE.getBoolean())
        {
        	if (s.equalsIgnoreCase("Monument")) { //Monument
                
        		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MonumentPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
                return new BlockPosition(coords[0], 50, coords[1]);
            } else if (s.toLowerCase().contains("mansion")) { //Mansion
                    
        		int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MansionPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
                return new BlockPosition(coords[0], 50, coords[1]);
            }
        }

        return null;
    }

    private static class CustomBiomeGrid implements BiomeGrid {
        BiomeBase[] biome;

        @Override
        public Biome getBiome(int x, int z) {
            return CraftBlock.biomeBaseToBiome(biome[(z << 4) | x]);
        }

        @Override
        public void setBiome(int x, int z, Biome bio) {
            biome[(z << 4) | x] = CraftBlock.biomeToBiomeBase(bio);
        }

        public Biome getBiome(int x, int y, int z) {
            return getBiome(x, z);
        }

        public void setBiome(int x, int y, int z, Biome bio) {
            setBiome(x, z, bio);
        }
    }
}
