package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

import java.util.Random;

/**
 * This class only exists to make sure lush desert beaches (oases)
 * generate properly and to make DesertHandler more readable.
 */
public class DesertBeachHandler extends SandyBeachHandler {
    public static double lushThreshold = (2 - TConfigOption.BIOME_DESERT_LUSH_COMMONNESS.getDouble()) * 0.31;

    @Override
    public void transformTerrain(TerraformWorld tw, Random random, ChunkGenerator.ChunkData chunk, ChunkGenerator.BiomeGrid biome, int chunkX, int chunkZ) {
        for (int x = chunkX * 16; x < chunkX * 16 + 16; x++) {
            for (int z = chunkZ * 16; z < chunkZ * 16 + 16; z++) {
                int height = HeightMap.getBlockHeight(tw, x, z);
                if (DesertBeachHandler.isLushBeach(tw, x, z)) {
                    for(int y = height; y < height + 35; y++)
                        biome.setBiome(x, y, z, Biome.JUNGLE);
                }
            }
        }
    }

    public static float getLushNoise(TerraformWorld world, int x, int z) {
        FastNoise lushRiversNoise = NoiseCacheHandler.getNoise(
                world,
                NoiseCacheHandler.NoiseCacheEntry.BIOME_DESERT_LUSH_RIVER,
                w -> {
                    FastNoise n = new FastNoise((int) (w.getSeed() * 0.4));
                    n.SetNoiseType(FastNoise.NoiseType.Cubic);
                    n.SetFrequency(0.012f);

                    return n;
                });

        return lushRiversNoise.GetNoise(x, z);
    }

    /**
     * @return true if (x, z) is inside oasis
     */
    public static boolean isLushBeach(TerraformWorld tw, int x, int z) {
        double lushRiverNoiseValue =  getLushNoise(tw, x, z);
        double riverDepth = HeightMap.getRawRiverDepth(tw, x, z);
        BiomeBank biome = BiomeBank.calculateHeightIndependentBiome(tw, x, z);

        return lushRiverNoiseValue > lushThreshold &&
                riverDepth > 0 &&
                (biome == BiomeBank.DESERT ||
                        biome == BiomeBank.BADLANDS);
    }

    public static void generateLushBeach(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int z, double riverDepth) {
        int y = GenUtils.getHighestGround(data, x, z);
        int aboveSea = y - TerraformGenerator.seaLevel;

        boolean isGrass = false;
        if (1 - aboveSea / 8d > random.nextDouble() && y >= TerraformGenerator.seaLevel) {
            data.setType(x, y, z, Material.GRASS_BLOCK);
            isGrass = true;
        }


        if (y == TerraformGenerator.seaLevel && random.nextInt(3) == 0) {
            BlockUtils.spawnPillar(random, data, x, y + 1, z, Material.SUGAR_CANE, 1, 4);
        } else if (y >= TerraformGenerator.seaLevel) {
            if (random.nextInt(8) == 0) {
                TreeDB.spawnCoconutTree(tw, data, x, y, z);
            } else if (random.nextInt(5) == 0) {
                createBush(random, data, x, y, z,
                        GenUtils.randDouble(random, 1.7, 3),
                        GenUtils.randDouble(random, 2, 2.8),
                        GenUtils.randDouble(random, 1.7, 3),
                        Material.JUNGLE_LEAVES,
                        Material.JUNGLE_LOG, 0.7);
            } else if (isGrass) {
                data.setType(x, y + 1, z, GenUtils.randMaterial(random,
                        Material.GRASS, Material.GRASS, Material.GRASS, Material.FERN));
            }
        }
    }

    // TODO: I feel like this is the 10th time I implement this in some form, should all game "objects" like rocks etc. be stored in one class as static functions?
    public static void createBush(Random random, PopulatorDataAbstract data, int x, int y, int z, double xRadius, double yRadius, double zRadius, Material leaves, Material stem, double density) {
        for (int ay = y; ay < y + yRadius / 2d; ay++)
            data.setType(x, ay, z, stem);
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
