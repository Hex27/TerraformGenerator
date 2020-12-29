package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeGrid;
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
        FastNoise plateauNoise = new FastNoise((int) (world.getSeed() * 2));
        plateauNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        plateauNoise.SetFrequency(0.005f);

        FastNoise plateauNoise2 = new FastNoise((int) (world.getSeed() * 2));
        plateauNoise2.SetNoiseType(FastNoise.NoiseType.CubicFractal);
        plateauNoise2.SetFrequency(0.18f);
        plateauNoise2.SetFractalOctaves(2);

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                if (HeightMap.getNoiseGradient(world, x, z, 3) >= 1.5 && GenUtils.chance(random, 49, 50)) {
                    BadlandsMountainHandler.oneUnit(world, random, data, x, z, true);
                    continue;
                }

                int highest = GenUtils.getTrueHighestBlock(data, x, z);

//                double riverDepth = HeightMap.getRiverDepth(world, x, z);
//
//                double normalHeight = highest + Math.max(0, riverDepth);
//
//                int terrainHeight = highest - TerraformGenerator.seaLevel;
//
//                if (normalHeight - riverDepth > TerraformGenerator.seaLevel) {
//                    data.setType(x, (int) Math.round(normalHeight), z, Material.RED_CONCRETE);
//                }

//                double plateauStrength = plateauNoise.GetNoise(x, z);
//
//                if (plateauStrength > 0 && BiomeGrid.getEdgeFactor(world, 0.35, -9, BiomeBank.BADLANDS, x, z) > 0.3) {
//                    double value = Math.abs(plateauNoise2.GetNoise(x, z)) * 1.2 * Math.pow(plateauStrength, 0.6);
//
//                    if (value > 0.23) {
//                        int height = (int) Math.round(Math.pow((value - 0.17) * 22, 1.2));
//                        for (int i = 1; i <= height; i++) data.setType(x, highest + i, z, highest + i % 4 == 0 ? Material.RED_TERRACOTTA : Material.TERRACOTTA);
//
//                        highest += height;
//                    }
//                }

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
                    } else if (GenUtils.chance(random, 1, 80)) {
                        data.setType(x, highest + 1, z, Material.DEAD_BUSH);
                    }
                }

            }
        }
        if (GenUtils.chance(random, TConfigOption.STRUCTURES_DESERTWELL_CHANCE_OUT_OF_TEN_THOUSAND.getInt(), 10000)) {
            new DesertWellPopulator().populate(world, random, data, true);
        }
    }
}
