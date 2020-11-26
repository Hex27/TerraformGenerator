package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SandyBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.BEACH;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.randMaterial(rand, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.GRAVEL),
                GenUtils.randMaterial(rand, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.GRAVEL),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.GRAVEL),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.STONE)};
    }

    @Override
    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {

        boolean hasSugarcane = GenUtils.chance(random, 1, 100);

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;
                Material base = data.getType(x, y, z);
                if (base != Material.SAND && base != Material.GRASS_BLOCK) continue;

                y++;
                //Spawn coconut trees
                if (GenUtils.chance(random, 1, 200)) {
                    TreeDB.spawnCoconutTree(world, data, x, y, z);
                    break;
                }

                //Spawn sugarcane
                if (hasSugarcane) {
                    boolean hasWater = false;
                    if (data.getType(x + 1, y - 1, z) == Material.WATER)
                        hasWater = true;
                    if (data.getType(x - 1, y - 1, z) == Material.WATER)
                        hasWater = true;
                    if (data.getType(x, y - 1, z + 1) == Material.WATER)
                        hasWater = true;
                    if (data.getType(x, y - 1, z - 1) == Material.WATER)
                        hasWater = true;

                    if (hasWater) BlockUtils.spawnPillar(random, data, x, y, z, Material.SUGAR_CANE, 3, 7);
                }
            }
        }
    }
}
