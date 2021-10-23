package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class BlackOceanBeachHandler extends BiomeHandler {

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
        return new Material[]{Material.STONE};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                if (data.getBiome(x, z) != getBiome()) continue;

                int y = GenUtils.getHighestGround(data, x, z);
                
                //Set ground near sea level to gravel
                if(y >= TerraformGenerator.seaLevel - 2) {
                	data.setType(x, y, z, Material.GRAVEL);
                }else if(y >= TerraformGenerator.seaLevel - 4) {
                	if(random.nextBoolean())
                    	data.setType(x, y, z, Material.GRAVEL);
                }
                
                //No kelp above sea level.
                if(y > TerraformGenerator.seaLevel) continue;
                if (!BlockUtils.isStoneLike(data.getType(x, y, z))) continue;
                if (GenUtils.chance(random, 1, 80)) { //SEA GRASS/KELP
                    CoralGenerator.generateKelpGrowth(data, x, y + 1, z);
                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}
}
