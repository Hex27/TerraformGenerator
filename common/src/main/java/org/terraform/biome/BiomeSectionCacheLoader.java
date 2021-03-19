package org.terraform.biome;


import com.google.common.cache.CacheLoader;

public class BiomeSectionCacheLoader extends CacheLoader<BiomeSection, BiomeSection> {

	@Override
	public BiomeSection load(BiomeSection key) throws Exception {
		key.doCalculations();
		return key;
	}

}