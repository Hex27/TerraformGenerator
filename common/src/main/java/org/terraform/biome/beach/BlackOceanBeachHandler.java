package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataColumn;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneEightBlockHandler;

import java.util.Random;

public class BlackOceanBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return OneOneEightBlockHandler.STONY_SHORE;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{Material.STONE};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        //Set ground near sea level to gravel
        if(surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.GRAVEL);
        }else if(surfaceY >= TerraformGenerator.seaLevel - 4) {
            if(random.nextBoolean())
                data.setType(rawX,surfaceY,rawZ, Material.GRAVEL);
        }

        //No kelp above sea level.
        if(surfaceY > TerraformGenerator.seaLevel) return;
        if (!BlockUtils.isStoneLike(data.getType(rawX,surfaceY,rawZ))) return;
        if (GenUtils.chance(random, 1, 80)) { //SEA GRASS/KELP
            CoralGenerator.generateKelpGrowth(data, rawX,surfaceY+1,rawZ);
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}
}
