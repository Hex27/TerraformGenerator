package org.terraform.utils.noise;

import java.util.function.Function;

import org.terraform.data.TerraformWorld;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * To help handle noise object caching throughout the entire plugin.
 * No more stupid hashmap caches all over the place.
 *
 */
public class NoiseCacheHandler{
	
	public static enum NoiseCacheEntry{
		TW_TEMPERATURE,
		TW_MOISTURE,
		TW_OCEANIC,
		TW_MOUNTAINOUS,
		
		CARVER_STANDARD,
		
		HEIGHTMAP_CORE,
		HEIGHTMAP_RIVER,
		//HEIGHTMAP_MOUNTAINOUS,
		HEIGHTMAP_ATTRITION,
		
		BIOME_BEACH_HEIGHT,
		
		BIOME_MUDDYBOG_HEIGHTMAP,
		
		BIOME_BADLANDS_PLATEAU_DISTORTEDCIRCLE,
		BIOME_BADLANDS_PLATEAUNOISE,
		BIOME_BADLANDS_WALLNOISE,
		BIOME_BADLANDS_PLATEAUDETAILS,

		BIOME_PAINTEDHILLS_NOISE,
		BIOME_PAINTEDHILLS_ROCKS_NOISE,
		
        BIOME_DESERT_LUSH_RIVER,

		BIOME_BAMBOOFOREST_PATHNOISE,

		BIOME_GORGE_CLIFFNOISE,
		BIOME_GORGE_DETAILS,
		
		BIOME_ARCHEDCLIFFS_PLATFORMNOISE,
		BIOME_ARCHEDCLIFFS_PILLARNOISE,

		BIOME_ERODEDPLAINS_CLIFFNOISE,
		BIOME_ERODEDPLAINS_DETAILS,

		BIOME_PETRIFIEDCLIFFS_CLIFFNOISE,
		BIOME_PETRIFIEDCLIFFS_INNERNOISE,
		
		BIOME_MUSHROOMISLAND_CIRCLE,
		BIOME_LAKE_CIRCLE,
		
		BIOME_FOREST_PATHNOISE,

        BIOME_TAIGA_BERRY_BUSHNOISE,

		BIOME_JUNGLE_GROUNDWOOD,
		BIOME_JUNGLE_GROUNDLEAVES,
		BIOME_JUNGLE_LILYPADS,

		BIOME_DESERT_DUNENOISE,
		
		BIOME_BADLANDS_CANYON_NOISE,
		
		BIOME_SWAMP_MUDNOISE,
		
		BIOME_CAVECLUSTER_CIRCLENOISE,
		
		BIOME_JAGGED_PEAKSNOISE,
		
		BIOME_SHATTERED_SAVANNANOISE,

        CAVE_CHEESE_NOISE,
		
		STRUCTURE_LARGECAVE_CARVER,
		
		STRUCTURE_LARGECAVE_RAISEDGROUNDNOISE,
		
		STRUCTURE_PYRAMID_BASEELEVATOR,
		STRUCTURE_PYRAMID_BASEFUZZER,
		
		STRUCTURE_ANCIENTCITY_RUINS,
		
		STRUCTURE_ANIMALFARM_FIELDNOISE,
		STRUCTURE_ANIMALFARM_RADIUSNOISE,
		
		STRUCTURE_RUINEDPORTAL_FISSURES,
		
		GENUTILS_RANDOMOBJ_NOISE,
		
		FRACTALTREES_LEAVES_NOISE,
		FRACTALTREES_BASE_NOISE,
		;
	}
	
    private static final LoadingCache<NoiseCacheHandler.NoiseCacheKey, FastNoise> NOISE_CACHE = 
    		CacheBuilder.newBuilder()
    		.maximumSize(300).build(new NoiseCacheLoader());
	
    public static FastNoise getNoise(TerraformWorld world, NoiseCacheEntry entry, Function<TerraformWorld, FastNoise> noiseFunction) {
        NoiseCacheKey key = new NoiseCacheKey(world,entry);
        FastNoise noise = NOISE_CACHE.getIfPresent(key);
        if(noise == null) {
        	noise = noiseFunction.apply(world);
        	NOISE_CACHE.put(key, noise);
        }
        return noise;
        
    }
    
	public static class NoiseCacheLoader extends CacheLoader<NoiseCacheHandler.NoiseCacheKey, FastNoise> {
		/**
		 * Does not do loading. 
		 * If this is null, the caller is responsible for inserting it.
		 */
		@Override
		public FastNoise load(NoiseCacheKey key) throws Exception {
			return null;
		}
	}
	
	public static class NoiseCacheKey {
		private TerraformWorld tw;
		private NoiseCacheEntry entry;
		
		public NoiseCacheKey(TerraformWorld world, NoiseCacheEntry entry) {
			this.tw = world;
			this.entry = entry;
		}

		@Override
		public int hashCode() {
	        return tw.hashCode() ^ (entry.hashCode() * 31);
	    }
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof NoiseCacheKey) {
				NoiseCacheKey o = (NoiseCacheKey) other;
				if(!o.tw.getName().equals(tw.getName()))
					return false;
				return entry == o.entry;
			}
			return false;
		}
	}
	

}
