package org.terraform.biome;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;

import org.terraform.data.SimpleLocation;
import org.terraform.data.TWSimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.GenUtils;

import com.google.common.cache.CacheLoader;

public class HeightIndependentBiomeCacheLoader extends CacheLoader<TWSimpleLocation, BiomeBank> {

	@Override
	public BiomeBank load(TWSimpleLocation loc) throws Exception {
		int x = loc.getX();
		int z = loc.getZ();
		TerraformWorld tw = loc.getTerraformWorld();

        double dither = TConfigOption.BIOME_DITHER.getDouble();
    	Random locationBasedRandom  = new Random(Objects.hash(tw.getSeed(),x,z));
    	SimpleLocation target  = new SimpleLocation(x,0,z);
    	BiomeSection homeSection = BiomeBank.getBiomeSection(tw, x,z);
    	
    	Collection<BiomeSection> sections = BiomeSection.getSurroundingSections(tw, x, z);
    	BiomeSection mostDominant = homeSection;
    	
    	for(BiomeSection sect:sections) {
    		float dom = (float) (sect.getDominance(target)+GenUtils.randDouble(locationBasedRandom,-dither,dither));
    		
    		if(dom > mostDominant.getDominance(target)+GenUtils.randDouble(locationBasedRandom,-dither,dither))
    			mostDominant = sect;
    	}
    	
    	return mostDominant.getBiomeBank();
	}

}
