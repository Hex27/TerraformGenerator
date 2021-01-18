package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class RockBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.MOUNTAIN_EDGE;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.STONE, 5, Material.GRAVEL, 35, Material.COBBLESTONE, 10),
                GenUtils.weightedRandomMaterial(rand, Material.STONE, 5, Material.GRAVEL, 35, Material.COBBLESTONE, 10),
                GenUtils.randMaterial(rand, Material.STONE, Material.COBBLESTONE, Material.GRAVEL),
                GenUtils.randMaterial(rand, Material.STONE, Material.COBBLESTONE, Material.GRAVEL)};
    }

    @Override
    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;

                if (GenUtils.chance(random, 1, 100))
                    data.setType(x, y + 1, z, Material.COBBLESTONE_SLAB);
            }
        }
    }
}
