package org.terraform.biome.river;

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

public class RiverHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return Biome.RIVER;
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
        return new Material[]{
        		Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }


    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        boolean growsKelp = random.nextBoolean();

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);

                if (data.getBiome(x, y + 1, z) != getBiome()) continue;

                //Set ground near sea level to sand
                if(y >= TerraformGenerator.seaLevel - 2) {
                	data.setType(x, y, z, Material.SAND);
                }else if(y >= TerraformGenerator.seaLevel - 4) {
                	if(random.nextBoolean())
                    	data.setType(x, y, z, Material.SAND);
                }
                
                if (!BlockUtils.isStoneLike(data.getType(x, y, z))) continue;

                // SEA GRASS/KELP
                if (GenUtils.chance(random, 10, 100)) {
                    data.setType(x, y + 1, z, Material.SEAGRASS);
                    if (random.nextBoolean() && y < TerraformGenerator.seaLevel - 2)
                        BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_SEAGRASS);
                } else if (GenUtils.chance(random, 3, 50) && growsKelp && y + 1 < TerraformGenerator.seaLevel - 10) {
                    generateKelp(x, y + 1, z, data, random);
                }

                // Generate clay
                if (GenUtils.chance(random, TConfigOption.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND.getInt(), 1000)) {
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

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}


}
