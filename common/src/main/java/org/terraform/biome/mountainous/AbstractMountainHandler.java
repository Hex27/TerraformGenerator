package org.terraform.biome.mountainous;

import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.data.TerraformWorld;

public abstract class AbstractMountainHandler extends BiomeHandler {

	@Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
    	
        double height = HeightMap.MOUNTAINOUS.getHeight(tw, x, z); //Added here

        //If the height is too high, just force it to smooth out
        if (height > 200) height = 200 + (height - 200) * 0.5;
        if (height > 230) height = 230 + (height - 230) * 0.3;
        if (height > 240) height = 240 + (height - 240) * 0.1;
        if (height > 250) height = 250 + (height - 250) * 0.05;
        
        return height;
    }
}
