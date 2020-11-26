package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class RockyMountainsHandler extends BiomeHandler {

    private static void dirtStack(PopulatorDataAbstract data, Random rand, int x, int y, int z) {
        data.setType(x, y, z, Material.GRASS_BLOCK);

        if (GenUtils.chance(rand, 1, 10))
            data.setType(x, y + 1, z, Material.GRASS);

        for (int i = 1; i < GenUtils.randInt(rand, 3, 7); i++) {
            data.setType(x, y - i, z, Material.DIRT);
        }
    }

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
        return Biome.MOUNTAINS;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.randMaterial(rand, Material.STONE, Material.STONE, Material.STONE, Material.STONE, Material.COBBLESTONE),
                GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),};
    }

    @Override
    public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);

                //Make patches of dirt that extend on the mountain sides
                if (GenUtils.chance(random, 1, 25)) {
                    dirtStack(data, random, x, y, z);
                    for (int nx = -2; nx <= 2; nx++)
                        for (int nz = -2; nz <= 2; nz++) {
                            if (GenUtils.chance(random, 1, 5)) continue;
                            y = GenUtils.getHighestGround(data, x + nx, z + nz);
                            dirtStack(data, random, x + nx, y, z + nz);
                        }
                }
            }
        }
    }
}
