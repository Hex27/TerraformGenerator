package org.terraform.biome;

import org.terraform.data.TWSimpleLocation;
import org.terraform.data.TerraformWorld;
import com.google.common.cache.CacheLoader;

public class HeightIndependentBiomeCacheLoader extends CacheLoader<TWSimpleLocation, BiomeBank> {

	@Override
	public BiomeBank load(TWSimpleLocation loc) throws Exception {
		int x = loc.getX();
		int z = loc.getZ();
		TerraformWorld tw = loc.getTerraformWorld();
    	BiomeSection mostDominant = BiomeSection.getMostDominantSection(tw,x,z);
    	return mostDominant.getBiomeBank();
	}

}
