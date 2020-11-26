package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class DesertHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.DESERT;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.randMaterial(rand, Material.RED_SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND,
                Material.SAND, Material.SAND, Material.SAND, Material.SAND, Material.SAND),
                Material.SAND,
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.SAND),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.STONE)};
    }

    @Override
    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {

        boolean cactusGathering = GenUtils.chance(random, 1, 100);
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;
                Material base = data.getType(x, y, z);

                if (cactusGathering) {
                    if (GenUtils.chance(random, 5, 100))
                        data.setType(x, y, z, Material.GRASS_PATH);
                }

                if (base == Material.SAND) {
                    if (GenUtils.chance(random, 1, 100) ||
                            (GenUtils.chance(random, 1, 20) && cactusGathering)) {
                        boolean canSpawn = true;
                        for (BlockFace face : BlockUtils.directBlockFaces) {
                            if (data.getType(x + face.getModX(), y + 1, z + face.getModZ()) != Material.AIR)
                                canSpawn = false;
                        }
                        if (canSpawn)
                            BlockUtils.spawnPillar(random, data, x, y + 1, z, Material.CACTUS, 3, 5);
                    }else if (GenUtils.chance(random, 1, 80)) {
                            data.setType(x, y + 1, z, Material.DEAD_BUSH);
                    }
                }

            }
        }
    }
}
