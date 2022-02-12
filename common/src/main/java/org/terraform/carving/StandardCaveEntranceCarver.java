package org.terraform.carving;

import java.util.Random;

import org.bukkit.Material;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

public class StandardCaveEntranceCarver extends NoiseCaveEntranceCarver {

	@Override
	public void carve(PopulatorDataAbstract data, TerraformWorld tw, Random random, int x, int z, int groundHeight) {
        FastNoise carverEntranceStandard = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.CARVER_STANDARD, 
        		world -> {
        	        FastNoise n = new FastNoise((int) (world.getSeed() * 111));
        	        n.SetNoiseType(NoiseType.SimplexFractal);
        	        n.SetFractalOctaves(4);
        	        n.SetFrequency(0.02f);
        	        return n;
        		});
    	
        Material fluid = Material.RED_STAINED_GLASS;
        if(data.getType(x, groundHeight+1, z) == Material.WATER)
        	fluid = Material.BLUE_STAINED_GLASS;
        for(int y = groundHeight; y > groundHeight - 10; y--) {
        	double noise = Math.pow(carverEntranceStandard.GetNoise(x, 0.4f*y, z),2);
        	//noise *= 1 - 0.1*(y-groundHeight);
        	if(noise > 0.4) {
        		if(data.getType(x, y, z) == Material.WATER)
            		data.setType(x, y, z, Material.BLUE_STAINED_GLASS);
        		else
            		data.setType(x, y, z, fluid);
//        		if(fluid == Material.WATER)
//        			PhysicsUpdaterPopulator.pushChange(tw.getName(), new SimpleLocation(x,y,z));
        	}
        }
        
	}

}
