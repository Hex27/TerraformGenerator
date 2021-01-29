package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.mountainous.BadlandsMountainHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.structure.small.DesertWellPopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class BadlandsHandler extends BiomeHandler {
    static BiomeBlender biomeBlender;

    private static BiomeBlender getBiomeBlender(TerraformWorld tw) {
        // Only one blender needed!
        if (biomeBlender == null) biomeBlender = new BiomeBlender(tw, true, false, false)
                .setBiomeThreshold(0.45);
        return biomeBlender;
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.BADLANDS;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
                Material.RED_SAND,
                Material.RED_SAND,
                GenUtils.randMaterial(rand, Material.RED_SAND, Material.RED_SANDSTONE),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE)};
    }

    @Override
    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int highest = GenUtils.getTrueHighestBlock(data, x, z);

                BiomeBank currentBiome = BiomeBank.calculateBiome(world, x, z, highest);
                if (currentBiome != BiomeBank.BADLANDS &&
                        currentBiome != BiomeBank.BADLANDS_BEACH &&
                        currentBiome != BiomeBank.BADLANDS_MOUNTAINS) continue;

                if (HeightMap.getNoiseGradient(world, x, z, 3) >= 1.5 && GenUtils.chance(random, 49, 50)) {
                    BadlandsMountainHandler.oneUnit(world, random, data, x, z, true);
                    continue;
                }

                Material base = data.getType(x, highest, z);
                if (base == Material.SAND ||
                        base == Material.RED_SAND) {
                    if (GenUtils.chance(random, 1, 200)) {

                        boolean canSpawn = true;
                        for (BlockFace face : BlockUtils.directBlockFaces) {
                            if (data.getType(x + face.getModX(), highest + 1, z + face.getModZ()) != Material.AIR)
                                canSpawn = false;
                        }
                        if (canSpawn)
                            BlockUtils.spawnPillar(random, data, x, highest + 1, z, Material.CACTUS, 2, 5);
                    } else if (GenUtils.chance(random, 1, 80) && highest > TerraformGenerator.seaLevel) {
                        data.setType(x, highest + 1, z, Material.DEAD_BUSH);
                    }
                }

            }
        }
        if (GenUtils.chance(random, TConfigOption.STRUCTURES_DESERTWELL_CHANCE_OUT_OF_TEN_THOUSAND.getInt(), 10000)) {
            new DesertWellPopulator().populate(world, random, data, true);
        }
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public void transformTerrain(TerraformWorld tw, Random random, ChunkGenerator.ChunkData chunk, int chunkX, int chunkZ) {
        BiomeBlender blender = getBiomeBlender(tw);

        FastNoise wallNoise = new FastNoise((int) (tw.getWorld().getSeed() * 2));
        wallNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        wallNoise.SetFrequency(0.07f);
        wallNoise.SetFractalOctaves(2);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
                int height = (int) preciseHeight;
                BiomeBank currentBiome = BiomeBank.calculateBiome(tw, rawX, rawZ, height);

                if (currentBiome == BiomeBank.BADLANDS
                        || currentBiome == BiomeBank.BADLANDS_MOUNTAINS
                        || currentBiome == BiomeBank.BADLANDS_BEACH
//                        && HeightMap.getRiverDepth(tw, rawX, rawZ) > 0
                ) {
                    double riverlessHeight = HeightMap.getRiverlessHeight(tw, rawX, rawZ) - 2;

                    // These are for blending river effect with other biomes
                    double edgeFactor = blender.getEdgeFactor(BiomeBank.BADLANDS, rawX, rawZ);
                    double bottomEdgeFactor = Math.min(2 * edgeFactor, 1);
                    double topEdgeFactor = Math.max(2 * edgeFactor - 1, 0);

                    // Max height difference between sea level and riverlessHeight
                    double maxDiff = riverlessHeight - TerraformGenerator.seaLevel;
                    double heightAboveSea = preciseHeight - 2 - TerraformGenerator.seaLevel;
                    double riverFactor = heightAboveSea / maxDiff; // 0 at river level, 1 at riverlessHeight

                    if (riverFactor > 0 && heightAboveSea > 0) {
                        int buildHeight = (int) Math.round(bottomEdgeFactor *
                                (Math.min(1, 4 * Math.pow(riverFactor, 4)) * maxDiff + wallNoise.GetNoise(rawX, rawZ) * 1.5));

                        for (int i = buildHeight; i >= 0; i--) {
                            int lowerHeight = Math.min(TerraformGenerator.seaLevel + i, (int) Math.round(riverlessHeight));

                            chunk.setBlock(x, lowerHeight, z, BlockUtils.getTerracotta(lowerHeight));
                        }

                        double threshold = 0.4 + (1 - topEdgeFactor) * 0.6;

                        // Curved top edges
                        if (riverFactor > threshold) {
                            int upperBuildHeight = (int) Math.round(
                                    1 *//topEdgeFactor *
                                            (Math.min(1, 50 * Math.pow(riverFactor - threshold, 2.5)) * maxDiff + wallNoise.GetNoise(rawX, rawZ) * 1.5));

                            if (topEdgeFactor == 0) continue;

                            for (int i = 0; i <= upperBuildHeight; i++) {
                                int upperHeight = (int) riverlessHeight - i;

                                chunk.setBlock(x, upperHeight, z, BlockUtils.getTerracotta(upperHeight));
                            }
                        }

                        // Coat with red sand
                        if (riverFactor > threshold + 0.12)
                            chunk.setBlock(x, (int) riverlessHeight + 1, z, Material.RED_SAND);
                    }
                }
            }
        }
    }
}
