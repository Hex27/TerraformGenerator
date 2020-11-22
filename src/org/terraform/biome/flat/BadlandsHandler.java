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
                            BlockUtils.spawnPillar(random, data, x, highest + 1, z, Material.CACTUS, 3, 6);
                    }else if (GenUtils.chance(random, 1, 80)) {
                        data.setType(x, highest + 1, z, Material.DEAD_BUSH);
                    }
                }
            }
        }
    }
}
