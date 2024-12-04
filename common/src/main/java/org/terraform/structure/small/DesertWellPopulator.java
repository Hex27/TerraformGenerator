package org.terraform.structure.small;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeClimate;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.V_1_20;
import org.terraform.utils.version.Version;

import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Random;

public class DesertWellPopulator extends MultiMegaChunkStructurePopulator {

    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        if (!isEnabled()) {
            return;
        }

        Random random = this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ());
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        for (int[] coords : getCoordsFromMegaChunk(tw, mc)) {
            int x = coords[0];
            int z = coords[1];
            if (x >> 4 != data.getChunkX() || z >> 4 != data.getChunkZ()) {
                continue;
            }
            int height = GenUtils.getHighestGround(data, x, z);
            spawnDesertWell(tw, random, data, x, height, z, tw.getBiomeBank(x, z) == BiomeBank.BADLANDS);
        }
    }

    public void spawnDesertWell(TerraformWorld tw,
                                @NotNull Random random,
                                @NotNull PopulatorDataAbstract data,
                                int x,
                                int y,
                                int z,
                                boolean badlandsWell)
    {

        SimpleBlock core = new SimpleBlock(data, x, y, z);
        TerraformGeneratorPlugin.logger.info("Spawning Desert Well at " + core.getCoords());
        try {
            TerraSchematic desertWell = TerraSchematic.load("desert_well", core);
            desertWell.parser = new DesertWellSchematicParser(random, badlandsWell, y);
            // swamphut.setFace(face);
            desertWell.apply();

            // Reposition center (Because shit's fucky)
            core = core.getRelative(1, 0, 1);

            // Make sure the well is standing on a stable base
            for (int nx = -3; nx <= 3; nx++) {
                for (int nz = -3; nz <= 3; nz++) {
                    if (!badlandsWell) {
                        new Wall(core.getRelative(nx, -1, nz)).downLPillar(
                                random,
                                10,
                                Material.SANDSTONE,
                                Material.CHISELED_SANDSTONE,
                                Material.CUT_SANDSTONE,
                                Material.SMOOTH_SANDSTONE
                        );
                    }
                    else {
                        new Wall(core.getRelative(nx, -1, nz)).downLPillar(
                                random,
                                10,
                                Material.RED_SANDSTONE,
                                Material.CHISELED_RED_SANDSTONE,
                                Material.CUT_RED_SANDSTONE,
                                Material.SMOOTH_RED_SANDSTONE
                        );
                    }
                }
            }

            // Drill hole down
            int depth = GenUtils.randInt(random, 5, 10);
            if (core.getUp().getType() != Material.WATER) {
                for (int i = 0; i < depth; i++) {
                    if (i < depth - 3) {
                        core.getRelative(0, -i, 0).setType(Material.CAVE_AIR);
                    }
                    else {
                        core.getRelative(0, -i, 0).setType(Material.WATER);
                    }
                }
            }

        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }

    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 8291374),
                (int) (TConfig.c.STRUCTURES_DESERTWELL_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        if (!isEnabled()) {
            return false;
        }

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[][] allCoords = getCoordsFromMegaChunk(tw, mc);
        for (int[] coords : allCoords) {
            if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {
                EnumSet<BiomeBank> biomes = GenUtils.getBiomesInChunk(tw, chunkX, chunkZ);
                for (BiomeBank b : biomes) {
                    if (b.getClimate() != BiomeClimate.HOT_BARREN) {
                        return false;
                    }
                    if (b.getType() != BiomeType.FLAT) {
                        return false;
                    }
                }
                return rollSpawnRatio(tw, chunkX, chunkZ);
            }
        }
        return false;
    }

    @Override
    public int[][] getCoordsFromMegaChunk(@NotNull TerraformWorld tw, @NotNull MegaChunk mc) {
        int num = TConfig.c.STRUCTURES_DESERTWELL_COUNT_PER_MEGACHUNK;
        int[][] coords = new int[num][2];
        for (int i = 0; i < num; i++) {
            coords[i] = mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 819227 * (1 + i)));
        }
        return coords;
    }

    @Override
    public int[] getNearestFeature(@NotNull TerraformWorld tw, int rawX, int rawZ) {
        MegaChunk mc = new MegaChunk(rawX, 0, rawZ);

        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                for (int[] loc : getCoordsFromMegaChunk(tw, mc)) {
                    double distSqr = Math.pow(loc[0] - rawX, 2) + Math.pow(loc[1] - rawZ, 2);
                    if (distSqr < minDistanceSquared) {
                        minDistanceSquared = distSqr;
                        min = loc;
                    }
                }
            }
        }
        return min;
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_DESERTWELL_ENABLED && (
                TConfig.c.BIOME_DESERT_WEIGHT > 0
                || TConfig.c.BIOME_BADLANDS_WEIGHT > 0);
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(189821, chunkX, chunkZ);
    }

    @Override
    public int getChunkBufferDistance() {
        return 1;
    }

    private static class DesertWellSchematicParser extends SchematicParser {
        private final Random rand;
        // private final PopulatorDataAbstract pop;
        private final boolean badlandsWell;
        private final int baseY;

        public DesertWellSchematicParser(Random rand, boolean badlandsWell, int baseY) {
            this.rand = rand;
            this.badlandsWell = badlandsWell;
            this.baseY = baseY;
        }

        @Override
        public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {

            if (this.badlandsWell) {
                data = Bukkit.createBlockData(StringUtils.replace(data.getAsString(), "sandstone", "red_sandstone"));
                if (data.getMaterial() == Material.RED_SANDSTONE && rand.nextInt(5) == 0) {
                    data = Bukkit.createBlockData(Material.CHISELED_RED_SANDSTONE);
                    super.applyData(block, data);
                    return;
                }

                if (data.getMaterial() != Material.RED_SANDSTONE_STAIRS
                    && data.getMaterial() != Material.RED_SANDSTONE_WALL
                    && data.getMaterial().toString().contains("RED_SANDSTONE"))
                {
                    data = Bukkit.createBlockData(StringUtils.replace(data.getAsString(),
                            "red_sandstone",
                            GenUtils.randChoice(rand,
                                    Material.RED_SANDSTONE,
                                    Material.SMOOTH_RED_SANDSTONE,
                                    Material.CUT_RED_SANDSTONE
                            ).name().toLowerCase(Locale.ENGLISH)
                    ));
                    super.applyData(block, data);
                }
                else {
                    super.applyData(block, data);
                }
                return;
            }
            else {
                if (data.getMaterial() == Material.SANDSTONE) {
                    if (rand.nextInt(5) == 0) {
                        data = Bukkit.createBlockData(Material.CHISELED_SANDSTONE);
                        super.applyData(block, data);
                        return;
                    }
                    else if (Version.isAtLeast(20) && block.getY() == baseY && GenUtils.chance(rand, 1, 20)) {
                        data = Bukkit.createBlockData(V_1_20.SUSPICIOUS_SAND);
                        super.applyData(block, data);
                        block.getPopData()
                             .lootTableChest(
                                     block.getX(),
                                     block.getY(),
                                     block.getZ(),
                                     TerraLootTable.DESERT_WELL_ARCHAEOLOGY
                             );
                        return;
                    }
                }

                if (data.getMaterial() != Material.SANDSTONE_STAIRS
                    && data.getMaterial() != Material.SANDSTONE_WALL
                    && data.getMaterial().toString().contains("SANDSTONE"))
                {
                    data = Bukkit.createBlockData(StringUtils.replace(data.getAsString(),
                            "sandstone",
                            GenUtils.randChoice(rand,
                                            Material.SANDSTONE,
                                            Material.SMOOTH_SANDSTONE,
                                            Material.CUT_SANDSTONE
                                    )
                                    .name()
                                    .toLowerCase(Locale.ENGLISH)
                    ));
                    super.applyData(block, data);
                }
                else {
                    super.applyData(block, data);
                }
            }


        }
    }
}
