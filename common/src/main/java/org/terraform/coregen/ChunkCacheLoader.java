package org.terraform.coregen;

import com.google.common.cache.CacheLoader;
import org.jetbrains.annotations.NotNull;

public class ChunkCacheLoader extends CacheLoader<ChunkCache, ChunkCache> {

	@Override
	public @NotNull ChunkCache load(@NotNull ChunkCache key) throws Exception {
		key.initInternalCache();
		return key;
	}

}
