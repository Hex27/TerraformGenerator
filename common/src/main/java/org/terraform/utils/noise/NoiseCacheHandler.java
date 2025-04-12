package org.terraform.utils.noise;

import org.jetbrains.annotations.NotNull;
import org.terraform.data.TerraformWorld;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * To help handle noise object caching throughout the entire plugin.
 * No more stupid hashmap caches all over the place.
 */
public class NoiseCacheHandler {

    // Removed google's cache from this as it was a bottleneck in rapid reads
    // in the noise generation phase. Since FastNoise is small, and the
    // set of storable things is bounded by this enum, there's
    // no reason to use a complex cache that frees things lazily - just store
    // everything and leave it there until the world is unloaded.
    private static final ConcurrentHashMap<NoiseCacheKey, FastNoise> NOISE_CACHE = new ConcurrentHashMap<>();
    public static void flushNoiseCaches(TerraformWorld tw){
        for(NoiseCacheKey k:NOISE_CACHE.keySet()){
            if(k.tw.equals(tw)) NOISE_CACHE.remove(k);
        }
    }

    public static @NotNull FastNoise getNoise(TerraformWorld world,
                                              NoiseCacheEntry entry,
                                              @NotNull Function<TerraformWorld, FastNoise> noiseFunction)
    {
        NoiseCacheKey key = new NoiseCacheKey(world, entry);
        FastNoise noise = NOISE_CACHE.get(key);
        if (noise == null) {
            noise = noiseFunction.apply(world);
            NOISE_CACHE.put(key, noise);
        }
        return noise;

    }

    public enum NoiseCacheEntry {
        TW_TEMPERATURE,
        TW_MOISTURE,
        TW_OCEANIC,
        TW_MOUNTAINOUS,

        CARVER_STANDARD,

        HEIGHTMAP_CORE,
        HEIGHTMAP_RIVER,
        // HEIGHTMAP_MOUNTAINOUS,
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
        BIOME_SHATTERED_Y_SAVANNANOISE,

        CAVE_FLUID_NOISE,
        CAVE_CHEESE_NOISE,
        CAVE_XRAVINE_NOISE,
        CAVE_XRAVINE_DETAILS,
        CAVE_YBARRIER_NOISE,

        STRUCTURE_LARGECAVE_CARVER,

        STRUCTURE_LARGECAVE_RAISEDGROUNDNOISE,

        STRUCTURE_PYRAMID_BASEELEVATOR,
        STRUCTURE_PYRAMID_BASEFUZZER,

        STRUCTURE_ANCIENTCITY_RUINS,
        STRUCTURE_ANCIENTCITY_HOLE,

        STRUCTURE_ANIMALFARM_FIELDNOISE,
        STRUCTURE_ANIMALFARM_RADIUSNOISE,

        STRUCTURE_RUINEDPORTAL_FISSURES,

        GENUTILS_RANDOMOBJ_NOISE,

        FRACTALTREES_LEAVES_NOISE,
        FRACTALTREES_BASE_NOISE
    }

    public record NoiseCacheKey(TerraformWorld tw, NoiseCacheEntry entry) {}


}
