package org.terraform.biome;


import com.google.common.cache.CacheLoader;
import org.jetbrains.annotations.NotNull;

public class BiomeSectionCacheLoader extends CacheLoader<BiomeSection, BiomeSection> {

	@Override
	public @NotNull BiomeSection load(@NotNull BiomeSection key) {
		key.doCalculations();
		return key;
	}

}