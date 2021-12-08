package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class DarkForestBeachHandler extends BiomeHandler {

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
        return new Material[]{
        		Material.COARSE_DIRT,
                Material.COARSE_DIRT,
                GenUtils.randMaterial(rand, Material.STONE, Material.COARSE_DIRT, Material.COARSE_DIRT, Material.COARSE_DIRT, Material.COARSE_DIRT, Material.COARSE_DIRT),
                Material.STONE,
                Material.STONE
                };
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
//
//        boolean hasSugarcane = GenUtils.chance(random, 1, 100);
//
//        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
//            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
//                int y = GenUtils.getTrueHighestBlock(data, x, z);
//                if (data.getBiome(x, z) != getBiome()) continue;
//                Material base = data.getType(x, y, z);
//                if (base != Material.SAND && base != Material.GRASS_BLOCK) continue;
//
//                y++;
//
//                //Spawn sugarcane
//                if (hasSugarcane) {
//                    boolean hasWater = false;
//                    if (data.getType(x + 1, y - 1, z) == Material.WATER)
//                        hasWater = true;
//                    if (data.getType(x - 1, y - 1, z) == Material.WATER)
//                        hasWater = true;
//                    if (data.getType(x, y - 1, z + 1) == Material.WATER)
//                        hasWater = true;
//                    if (data.getType(x, y - 1, z - 1) == Material.WATER)
//                        hasWater = true;
//
//                    if (hasWater) BlockUtils.spawnPillar(random, data, x, y, z, Material.SUGAR_CANE, 3, 7);
//                }
//            }
//        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        
	}
}
