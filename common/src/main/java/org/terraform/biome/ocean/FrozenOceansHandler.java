package org.terraform.biome.ocean;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class FrozenOceansHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return Biome.FROZEN_OCEAN;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.GRAVEL,
        		Material.GRAVEL,
                GenUtils.randMaterial(rand, Material.STONE, Material.GRAVEL, Material.STONE),
                GenUtils.randMaterial(rand, Material.STONE),
                GenUtils.randMaterial(rand, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, y + 1, z) != getBiome()) continue;
                
                //Set ground near sea level to gravel
                if(y >= TerraformGenerator.seaLevel - 2) {
                	data.setType(x, y, z, Material.GRAVEL);
                }else if(y >= TerraformGenerator.seaLevel - 4) {
                	if(random.nextBoolean())
                    	data.setType(x, y, z, Material.GRAVEL);
                }

                //Full ice-sheets
                if (!data.getType(x, TerraformGenerator.seaLevel, z).isSolid())
                    data.setType(x, TerraformGenerator.seaLevel, z, Material.ICE);

            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}


}
