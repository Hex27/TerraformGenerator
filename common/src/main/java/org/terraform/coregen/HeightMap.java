package org.terraform.coregen;

import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.CoordPair;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;
import org.terraform.utils.datastructs.ConcurrentLRUCache;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.HashMap;

public enum HeightMap {
    /**
     * Current river depth, also returns negative values if on dry ground.
     */
    RIVER {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise noise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.HEIGHTMAP_RIVER, world -> {
                FastNoise n = new FastNoise((int) world.getSeed());
                n.SetNoiseType(NoiseType.PerlinFractal);
                n.SetFrequency(TConfig.c.HEIGHT_MAP_RIVER_FREQUENCY);
                n.SetFractalOctaves(5);
                return n;
            });
            return 15 - 200 * Math.abs(noise.GetNoise(x, z));
        }
    }, CORE {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise noise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.HEIGHTMAP_CORE, world -> {
                FastNoise n = new FastNoise((int) world.getSeed());
                n.SetNoiseType(NoiseType.SimplexFractal);
                n.SetFractalOctaves(2); // Poor detail after blurs. Rely on Attrition for detail
                n.SetFrequency(TConfig.c.HEIGHT_MAP_CORE_FREQUENCY);
                return n;
            });

            // 7 blocks elevated from the sea level
            double height = 10 * noise.GetNoise(x, z) + 7 + TerraformGenerator.seaLevel;

            // Plateau-out height to make it flat-ish
            if (height > TerraformGenerator.seaLevel + 10) {
                height = (height - TerraformGenerator.seaLevel - 10) * 0.1 + TerraformGenerator.seaLevel + 10;
            }

            // This is fucking nonsense

            return height;
        }
    }, ATTRITION {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise perlin = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.HEIGHTMAP_ATTRITION, world -> {
                FastNoise n = new FastNoise((int) world.getSeed() + 113);
                n.SetNoiseType(NoiseType.PerlinFractal);
                n.SetFractalOctaves(4);
                n.SetFrequency(0.02f);
                return n;
            });

            double height = perlin.GetNoise(x, z) * 2 * 7;
            return Math.max(0, height);
        }
    };

    public static final int defaultSeaLevel = 62;
    public static final float heightAmplifier = TConfig.c.HEIGHT_MAP_LAND_HEIGHT_AMPLIFIER;
    public static final int MASK_RADIUS = 5;
    public static final int MASK_DIAMETER = (MASK_RADIUS * 2) + 1;
    public static final int MASK_VOLUME = MASK_DIAMETER*MASK_DIAMETER;
    private static final int upscaleSize = 3;
    public static int spawnFlatRadiusSquared = -324534;
    private static final ConcurrentLRUCache<BiomeSection, SectionBlurCache> BLUR_CACHE = new ConcurrentLRUCache<>(
        "BLUR_CACHE",64, (sect)->{
            SectionBlurCache newCache = new SectionBlurCache(
                    sect,
                    new float[BiomeSection.sectionWidth+MASK_DIAMETER][BiomeSection.sectionWidth+MASK_DIAMETER],
                    new float[BiomeSection.sectionWidth+MASK_DIAMETER][BiomeSection.sectionWidth+MASK_DIAMETER]);
            newCache.fillCache();
            return newCache;
        }
    );

    /**
     * Returns the average increase or decrease in height for surrounding blocks compared to the provided height at those coords.
     * 1.5 for a radius of 3 is considered steep.
     * Does noise calculations to find the true core height
     */
    public static double getNoiseGradient(TerraformWorld tw, int x, int z, int radius) {
        double totalChangeInGradient = 0;
        int count = 0;
        double centerNoise = getBlockHeight(tw, x, z);
        for (int nx = -radius; nx <= radius; nx++) {
            for (int nz = -radius; nz <= radius; nz++) {
                if (nx == 0 && nz == 0) {
                    continue;
                }
                // Bukkit.getLogger().info(nx + "," + nz + ":"+(getHeight(tw,x+nx,z+nz)-centerNoise));
                totalChangeInGradient += Math.abs(getBlockHeight(tw, x + nx, z + nz) - centerNoise);
                count++;
            }
        }

        return totalChangeInGradient / count;
    }

    /**
     * Returns the average increase or decrease in height for surrounding blocks compared to the provided height at those coords.
     * 1.5 for a radius of 3 is considered steep.
     * Does GenUtils.getHighestGround to get height values.
     */
    /*TODO: There are several calls to this in Biome Handlers.
     * Write a version that uses transformed height.
     * 10/4/2025: Is this not already transformed height???
     */
    public static double getTrueHeightGradient(PopulatorDataAbstract data, int x, int z, int radius) {
        double totalChangeInGradient = 0;
        int count = 0;
        double centerNoise = GenUtils.getHighestGround(data, x, z); // getBlockHeight(tw, x, z);
        for (int nx = -radius; nx <= radius; nx++) {
            for (int nz = -radius; nz <= radius; nz++) {
                if (nx == 0 && nz == 0) {
                    continue;
                }
                // Bukkit.getLogger().info(nx + "," + nz + ":"+(getHeight(tw,x+nx,z+nz)-centerNoise));
                totalChangeInGradient += Math.abs(GenUtils.getHighestGround(data, x + nx, z + nz) - centerNoise);
                count++;
            }
        }

        return totalChangeInGradient / count;
    }

    public static double getRawRiverDepth(TerraformWorld tw, int x, int z) {
        if (Math.pow(x, 2) + Math.pow(z, 2) < spawnFlatRadiusSquared) {
            return 0;
        }
        double depth = HeightMap.RIVER.getHeight(tw, x, z);
        return Math.max(0, depth);
    }

    public static double getPreciseHeight(TerraformWorld tw, int x, int z) {
        ChunkCache cache = TerraformGenerator.getCache(tw, x>>4, z>>4);

        double cachedValue = cache.getHeightMapHeight(x, z);
        if (cachedValue != ChunkCache.CHUNKCACHE_INVAL) {
            return cachedValue;
        }

        double height = getRiverlessHeight(tw, x, z);

        // River Depth
        double depth = getRawRiverDepth(tw, x, z);

        // Normal scenario: Shallow area
        if (height - depth >= TerraformGenerator.seaLevel - 15) {
            height -= depth;

            // Fix for underwater river carving: Don't carve deeply
        }
        else if (height > TerraformGenerator.seaLevel - 15 && height - depth < TerraformGenerator.seaLevel - 15) {
            height = TerraformGenerator.seaLevel - 15;
        }

        if (heightAmplifier != 1f && height > TerraformGenerator.seaLevel) {
            height += heightAmplifier * (height - TerraformGenerator.seaLevel);
        }

        cache.cacheHeightMap(x, z, height);
        return height;
    }

    /**
     * Do not fucking call this anywhere outside the SectionBlurCache.
     * This method used to cause cache thrashing as it can leave the chunk
     * boundaries. This was fixed by just caching this with a stack
     * variable, as it happened to be called in one place.
     * <br><br>
     * Do not call this anywhere else.
     * @param x raw x coordinate
     * @param z raw z coordinate
     * @param dominantBiomeHeights This is a cache value for local caching.
     * @return The dominant biome's height calculation. Must be blurred to be coherent with other biomes.
     */
    static float getDominantBiomeHeight(TerraformWorld tw, int x, int z, HashMap<CoordPair, Float> dominantBiomeHeights) {
        CoordPair key = new CoordPair(x,z);
        Float h = dominantBiomeHeights.get(key);
        if (h == null) {
            // Upscale the biome
            // This comes from computing each biome height one time per upscaleSize blocks
            if (x % upscaleSize != 0 && z % upscaleSize != 0) {
                h = getDominantBiomeHeight(tw, x - (x % upscaleSize), z - (z % upscaleSize), dominantBiomeHeights);
            }
            else {
                h = (float) BiomeBank.calculateHeightIndependentBiome(tw, x, z).getHandler().calculateHeight(tw, x, z);
                if (Math.pow(x, 2) + Math.pow(z, 2) < spawnFlatRadiusSquared) {
                    h = (float) HeightMap.CORE.getHeight(tw, x, z);
                }
            }
            dominantBiomeHeights.put(key, h);
        }
        return h;
    }

    /**
     * Biome calculations are done here as well.
     * <br>
     * This function is responsible for applying blurring to merge biomes together
     *
     * @return Near-final world height without rivers accounted for
     */
    public static double getRiverlessHeight(TerraformWorld tw, int x, int z) {

        // int maskDiameterSquared = maskDiameter*maskDiameter;
        double coreHeight;

        BiomeSection sect = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);

        // This will calculate a blur height
        coreHeight = BLUR_CACHE.get(sect).getBlurredHeight(x,z);

        coreHeight += HeightMap.ATTRITION.getHeight(tw, x, z);

        return coreHeight;
    }

    public static int getBlockHeight(TerraformWorld tw, int x, int z) {
        return (int) getPreciseHeight(tw, x, z);
    }

    public abstract double getHeight(TerraformWorld tw, int x, int z);
}