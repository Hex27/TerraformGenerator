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
    static BiomeBlender riversBlender;
    static BiomeBlender plateauBlender;

    private static BiomeBlender getRiversBlender(TerraformWorld tw) {
        // Only one blender needed!
        if (riversBlender == null) riversBlender = new BiomeBlender(tw, true, false, false)
                .setBiomeThreshold(0.45);
        return riversBlender;
    }

    private static BiomeBlender getPlateauBlender(TerraformWorld tw) {
        if (plateauBlender == null) plateauBlender = new BiomeBlender(tw, true, true, true)
                .setBiomeThreshold(0.3).setMountainThreshold(6).setRiverThreshold(7);
        return plateauBlender;
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
        generatePlateaus(world, data);

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
                        // Prevent cactus from spawning on plateaus:
                        if (HeightMap.getBlockHeight(world, x, z) + 5 < highest) canSpawn = false;
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
        BiomeBlender blender = getRiversBlender(tw);

        FastNoise wallNoise = new FastNoise((int) (tw.getWorld().getSeed() * 2));
        wallNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        wallNoise.SetFrequency(0.07f);
        wallNoise.SetFractalOctaves(2);

        // Rivers
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

    void generatePlateaus(TerraformWorld tw, PopulatorDataAbstract data) {
//        FastNoise plateauNoise = new FastNoise((int) (tw.getSeed() * 7509));
        FastNoise plateauNoise = new FastNoise();
        plateauNoise.SetNoiseType(FastNoise.NoiseType.CubicFractal);
        plateauNoise.SetFractalOctaves(2);
        plateauNoise.SetFrequency(0.01f);

        FastNoise detailsNoise = new FastNoise((int) (tw.getSeed() * 7509));
        detailsNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        detailsNoise.SetFrequency(0.08f);

        double threshold = 0.2;
        int heightFactor = 15;
        int sandRadius = 7;

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {

                int height = HeightMap.getBlockHeight(tw, x, z);
                double rawValue = Math.max(0, plateauNoise.GetNoise(x, z));
                double noiseValue = rawValue * getPlateauBlender(tw).getEdgeFactor(BiomeBank.BADLANDS, x, z);

                double graduated = (noiseValue / threshold);
                double platformHeight = (int) graduated * heightFactor
                        + (10 * Math.pow(graduated - (int) graduated - 0.5 - 0.1, 7) * heightFactor);

                boolean placeSand = false;
                for (int y = 1; y <= (int) Math.round(platformHeight); y++) {
                    placeSand = true;
                    Material material;
                    if ((int) graduated * heightFactor == y)
                        material = Material.RED_SAND;
                    else if ((int) graduated * heightFactor == y + 1)
                        material = GenUtils.randMaterial(Material.RED_SAND, Material.RED_SAND, BlockUtils.getTerracotta(height + y));
                    else if ((int) graduated * heightFactor == y + 2)
                        material = GenUtils.randMaterial(Material.RED_SAND, BlockUtils.getTerracotta(height + y), BlockUtils.getTerracotta(height + y));
                    else
                        material = BlockUtils.getTerracotta(height + y);

                    data.setType(x, height + y, z, material);

                }

                if (!placeSand) continue;
                // Surround plateaus with sand
//                surroundWithSand(tw, data, x, z, heightFactor, sandRadius, (int) graduated * heightFactor);
                int level = (((int) graduated) - 1) * heightFactor;
                for (int sx = x - sandRadius; sx <= x + sandRadius; sx++) {
                    for (int sz = z - sandRadius; sz <= z + sandRadius; sz++) {
                        double distance = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sz - z, 2));

                        if (distance < sandRadius) {
                            int sandHeight = (int) Math.round(heightFactor * 0.55 * Math.pow(1 - distance / sandRadius, 1.7) + detailsNoise.GetNoise(sx, sz));

                            for (int y = 1 + level; y <= sandHeight + level; y++)
                                if (data.getType(sx, HeightMap.getBlockHeight(tw, sx, sz) + y, sz).isAir()) data.setType(sx, HeightMap.getBlockHeight(tw, sx, sz) + y, sz, Material.RED_SAND);

                        }
                    }
                }
            }
        }
    }

    void surroundWithSand(TerraformWorld tw, PopulatorDataAbstract data, int x, int z, int heightFactor, int sandRadius, int level) {
        FastNoise detailsNoise = new FastNoise((int) (tw.getSeed() * 7509));
        detailsNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        detailsNoise.SetFrequency(0.08f);

        for (int sx = x - sandRadius; sx <= x + sandRadius; sx++) {
            for (int sz = z - sandRadius; sz <= z + sandRadius; sz++) {
                double distance = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sz - z, 2));

                if (distance < sandRadius) {
                    int sandHeight = (int) Math.round(heightFactor * 0.55 * Math.pow(1 - distance / sandRadius, 1.7) + detailsNoise.GetNoise(sx, sz));

                    for (int y = 1; y <= sandHeight; y++)
                        if (data.getType(sx, HeightMap.getBlockHeight(tw, sx, sz) + y, sz).isAir()) data.setType(sx, HeightMap.getBlockHeight(tw, sx, sz) + y, sz, Material.RED_SAND);

                }
            }
        }
    }
}
