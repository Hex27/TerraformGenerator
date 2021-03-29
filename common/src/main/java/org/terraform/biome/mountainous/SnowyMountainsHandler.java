package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SnowyMountainsHandler extends AbstractMountainHandler {
    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.SNOWY_MOUNTAINS;
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
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{Material.STONE};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				
				//Snow on shallow areas
				if(HeightMap.getTrueHeightGradient(data, x, z, 3) < 1.4) {
					int y = GenUtils.getHighestGround(data, x, z);
	                //Don't touch submerged blocks
					if(data.getBiome(x, z) != getBiome())
						continue;
	                if(y < TerraformGenerator.seaLevel)
	                	continue;
	                
					data.setType(x, y, z, Material.SNOW_BLOCK);
				}
			}
		}
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.ICY_BEACH;
	}
}
