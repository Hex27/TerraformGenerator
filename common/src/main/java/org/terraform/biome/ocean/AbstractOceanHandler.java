package org.terraform.biome.ocean;

import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.data.TerraformWorld;

public abstract class AbstractOceanHandler extends BiomeHandler {

	@Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
    	
        double height = HeightMap.CORE.getHeight(tw, x, z) - 35;

        if (height > HeightMap.defaultSeaLevel + 4) {
            height += HeightMap.ATTRITION.getHeight(tw, x, z);
        } else {
            height += HeightMap.ATTRITION.getHeight(tw, x, z) * 0.8;
        }

        //If the height is too low, force it back to 3.
        if (height <= 0) height = 3;
        
        return height;
    }
}
