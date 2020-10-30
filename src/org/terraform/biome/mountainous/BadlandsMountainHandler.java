package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class BadlandsMountainHandler extends BiomeHandler {
    private static final Material[] terracottas = {
            Material.TERRACOTTA,
            Material.YELLOW_TERRACOTTA,
            Material.ORANGE_TERRACOTTA,
            Material.RED_TERRACOTTA,
    };

    @Override
    public boolean isOcean() {
        return false;
    }
//
//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 8);
//		gen.setScale(0.005);
//		
//		return (int) ((gen.noise(x, z, 0.5, 0.5)*7D+50D)*1.5);
//	}

    @Override
    public Biome getBiome() {
        return Biome.BADLANDS_PLATEAU;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.RED_SAND, 35, Material.SAND, 5),
                GenUtils.weightedRandomMaterial(rand, Material.RED_SAND, 35, Material.SAND, 5),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.RED_SANDSTONE, Material.RED_SAND),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE)};
    }

    @Override
    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int highest = GenUtils.getTrueHighestBlock(data, x, z);

                for (int y = highest; y > 60; y--) {
                    if (data.getBiome(x, y, z) != getBiome()) continue;
                    if (!data.getType(x, y, z).toString().contains("SAND"))
                        continue;
                    if (GenUtils.chance(1, 50)) continue;
                    Material terra = terracottas[(y - 60) % 3];
                    data.setType(x, y, z, terra);
                }
            }
        }
    }
}
