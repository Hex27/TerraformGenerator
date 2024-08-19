package org.terraform.structure.small.buriedtreasure;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;

import java.util.EnumSet;
import java.util.Random;

public class BuriedTreasurePopulator extends MultiMegaChunkStructurePopulator {

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
            height -= GenUtils.randInt(random, 3, 10);

            SimpleBlock chest = new SimpleBlock(data, x, height, z);
            new ChestBuilder(Material.CHEST).setFacing(BlockUtils.getDirectBlockFace(random))
                                            .setLootTable(TerraLootTable.BURIED_TREASURE)
                                            .apply(chest);
        }
    }

    @Override
    public int[][] getCoordsFromMegaChunk(@NotNull TerraformWorld tw, @NotNull MegaChunk mc) {
        int num = TConfig.c.STRUCTURES_BURIEDTREASURE_COUNT_PER_MEGACHUNK;
        int[][] coords = new int[num][2];
        for (int i = 0; i < num; i++) {
            coords[i] = mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 87464 * (1 + i)));
        }
        return coords;
    }

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


    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(
                tw.getHashedRand(chunkX, chunkZ, 12422),
                (int) (TConfig.c.STRUCTURES_BURIEDTREASURE_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        if (!isEnabled()) {
            return false;
        }

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        for (int[] coords : getCoordsFromMegaChunk(tw, mc)) {
            if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {
                EnumSet<BiomeBank> biomes = GenUtils.getBiomesInChunk(tw, chunkX, chunkZ);
                double numBeach = 0;
                for (BiomeBank b : biomes) {
                    if (b.getType() == BiomeType.BEACH) {
                        numBeach++;
                    }
                }

                return (numBeach > 0) && rollSpawnRatio(tw, chunkX, chunkZ);
            }
        }
        return false;
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(82736921, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_BURIEDTREASURE_ENABLED;
    }

    @Override
    public int getChunkBufferDistance() {
        return 0;
    }
}
