package org.terraform.biome.beach;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.TreeDB;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

import java.util.Random;

/**
 * This class contains functions for oases, it's not a biome handler.
 */
public class OasisBeach {
    public static final double oasisThreshold = (2 - TConfig.c.BIOME_OASIS_COMMONNESS) * 0.31;
    private static final float oasisFrequency = TConfig.c.BIOME_OASIS_FREQUENCY;

    public static float getOasisNoise(TerraformWorld world, int x, int z) {
        FastNoise lushRiversNoise = NoiseCacheHandler.getNoise(world,
                NoiseCacheHandler.NoiseCacheEntry.BIOME_DESERT_LUSH_RIVER,
                w -> {
                    FastNoise n = new FastNoise((int) (w.getSeed() * 0.4));
                    n.SetNoiseType(FastNoise.NoiseType.Cubic);
                    n.SetFrequency(oasisFrequency);

                    return n;
                }
        );

        return lushRiversNoise.GetNoise(x, z);
    }

    /**
     * @return true if (x, z) is inside oasis
     */
    private static boolean isOasisBeach(TerraformWorld tw, int x, int z, BiomeBank targetBiome) {
        double lushRiverNoiseValue = getOasisNoise(tw, x, z);
        double riverDepth = HeightMap.getRawRiverDepth(tw, x, z);
        BiomeBank biome = BiomeBank.calculateHeightIndependentBiome(tw, x, z);

        return lushRiverNoiseValue > oasisThreshold && riverDepth > 0 && biome == targetBiome;
    }

    /**
     * Generate lush beach for coordinate.
     * Should be ran for every coordinate in BiomeHandler#populateSmallItems
     *
     * @param targetBiome the biome this function is called from.
     *                    Prevents code from running twice when biome overlap
     */
    public static void generateOasisBeach(@NotNull TerraformWorld tw,
                                          @NotNull Random random,
                                          @NotNull PopulatorDataAbstract data,
                                          int x,
                                          int z,
                                          BiomeBank targetBiome)
    {
        if (!isOasisBeach(tw, x, z, targetBiome)) {
            return;
        }

        int y = GenUtils.getHighestGround(data, x, z);
        int aboveSea = y - TerraformGenerator.seaLevel;

        boolean isGrass = false;
        if (1 - aboveSea / 8d > random.nextDouble() && y >= TerraformGenerator.seaLevel) {
            data.setType(x, y, z, Material.GRASS_BLOCK);
            isGrass = true;
        }


        if (y == TerraformGenerator.seaLevel && random.nextInt(3) == 0) {
            PlantBuilder.SUGAR_CANE.build(random, data, x, y + 1, z, 1, 4);
        }
        else if (y >= TerraformGenerator.seaLevel) {
            if (random.nextInt(8) == 0) {
                TreeDB.spawnCoconutTree(tw, data, x, y, z);
            }
            else if (random.nextInt(5) == 0) {
                createBush(random,
                        data,
                        x,
                        y,
                        z,
                        GenUtils.randDouble(random, 1.7, 3),
                        GenUtils.randDouble(random, 2, 2.8),
                        GenUtils.randDouble(random, 1.7, 3),
                        Material.JUNGLE_LEAVES,
                        Material.JUNGLE_LOG,
                        0.7
                );
            }
            else if (isGrass) {
                PlantBuilder.build(
                        random,
                        data,
                        x,
                        y + 1,
                        z,
                        PlantBuilder.GRASS,
                        PlantBuilder.GRASS,
                        PlantBuilder.GRASS,
                        PlantBuilder.FERN
                );
            }
        }
    }

    // TODO: I feel like this is the 10th time I implement this in some form, should all game "objects" like rocks etc. be stored in one class as static functions?
    public static void createBush(@NotNull Random random,
                                  @NotNull PopulatorDataAbstract data,
                                  int x,
                                  int y,
                                  int z,
                                  double xRadius,
                                  double yRadius,
                                  double zRadius,
                                  Material leaves,
                                  Material stem,
                                  double density)
    {
        if (!TConfig.arePlantsEnabled()) {
            return;
        }

        for (int ny = y; ny < y + yRadius / 2; ny++) {
            data.setType(x, ny, z, stem);
        }
        for (int xr = (int) -Math.ceil(xRadius); xr < Math.ceil(xRadius); xr++) {
            for (int yr = (int) -Math.ceil(yRadius); yr < Math.ceil(yRadius); yr++) {
                for (int zr = (int) -Math.ceil(zRadius); zr < Math.ceil(zRadius); zr++) {
                    double distToCenter = Math.sqrt((xr * xr) / (xRadius * xRadius)
                                                    + (yr * yr) / (yRadius * yRadius)
                                                    + (zr * zr) / (zRadius * zRadius));

                    if (distToCenter < 1 && random.nextDouble() < 1 - distToCenter + density) {
                        data.lsetType(x + xr, y + yr, z + zr, leaves);
                    }
                }
            }
        }
    }
}
