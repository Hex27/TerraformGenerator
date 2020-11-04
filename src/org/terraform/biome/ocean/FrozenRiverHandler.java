package org.terraform.biome.ocean;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class FrozenRiverHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return Biome.FROZEN_RIVER;
    }

//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 2);
//		gen.setScale(0.005);
//		
//		return (int) (gen.noise(x, z, 0.5, 0.5)*7D+50D);
//	}

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.randMaterial(rand, Material.DIRT, Material.SAND, Material.SAND, Material.DIRT, Material.DIRT, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.SAND, Material.SAND, Material.DIRT, Material.DIRT, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.DIRT, Material.SAND),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }


    @Override
    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        boolean growsKelp = random.nextBoolean();

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, y, z) != getBiome()) continue;

                //Ice
                if (!data.getType(x, TerraformGenerator.seaLevel, z).isSolid())
                    data.setType(x, TerraformGenerator.seaLevel, z, Material.ICE);

                if (!BlockUtils.isStoneLike(data.getType(x, y, z))) continue;
                if (GenUtils.chance(random, 10, 100)) { //SEA GRASS/KELP
                    data.setType(x, y + 1, z, Material.SEAGRASS);
                    if (random.nextBoolean() && y < TerraformGenerator.seaLevel - 2)
                        BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_SEAGRASS);
                } else if (GenUtils.chance(random, 3, 50) && growsKelp && y + 1 < TerraformGenerator.seaLevel - 10) {
                    generateKelp(x, y + 1, z, data, random);
                }
                if (GenUtils.chance(random, TConfigOption.BIOME_RIVER_CLAY_CHANCE.getInt(), 1000)) {
                    BlockUtils.generateClayDeposit(x, y, z, data, random);
                }
            }
        }
    }

    private void generateKelp(int x, int y, int z, PopulatorDataAbstract data, Random random) {
        for (int ny = y; ny < TerraformGenerator.seaLevel - GenUtils.randInt(5, 15); ny++) {
            data.setType(x, ny, z, Material.KELP_PLANT);
        }
    }


}
