package org.terraform.coregen;

import com.google.common.cache.CacheLoader;

public class ChunkCacheLoader extends CacheLoader<ChunkCache, ChunkCache> {

    @Override
    public ChunkCache load(ChunkCache key) throws Exception {
        key.initInternalCache();
        return key;
    }

}
