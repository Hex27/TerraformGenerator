package org.terraform.coregen;

import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public enum HeightMap {
    /**
     * Current river depth, also returns negative values if on dry ground.
     */
    RIVER {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise noise = computeNoise(tw, world -> {
                FastNoise n = new FastNoise();
                n.SetSeed((int) tw.getSeed());
                n.SetNoiseType(NoiseType.PerlinFractal);
                n.SetFrequency(TConfigOption.HEIGHT_MAP_RIVER_FREQUENCY.getFloat());
                n.SetFractalOctaves(5);
                return n;
            });
            return 15 - 100 * Math.abs(noise.GetNoise(x, z));
        }
    }, OCEANIC {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise cubic = computeNoise(tw, world -> {
                FastNoise n = new FastNoise((int) tw.getSeed() * 12);
                n.SetNoiseType(NoiseType.CubicFractal);
                n.SetFractalOctaves(6);
                n.SetFrequency(TConfigOption.HEIGHT_MAP_OCEANIC_FREQUENCY.getFloat());
                return n;
            });

            double height = cubic.GetNoise(x, z) * 2.5;

            //Only negative height (Downwards)
            if (height > 0) height = 0;
            return height * 50; //Depth
        }
    }, CORE {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise cubic = computeNoise(tw, world -> {
                FastNoise n = new FastNoise((int) tw.getSeed());
                n.SetNoiseType(NoiseType.CubicFractal);
                n.SetFractalOctaves(6);
                n.SetFrequency(TConfigOption.HEIGHT_MAP_CORE_FREQUENCY.getFloat());
                return n;
            });

            double height = cubic.GetNoise(x, z) * 2 * 15 + 13 + defaultSeaLevel;

            //Ensure that height doesn't automatically go upwards sharply
            if (height > defaultSeaLevel + 10) {
                height = (height - defaultSeaLevel - 10) * 0.1 + defaultSeaLevel + 10;
            }

            //Ensure that height doesn't automatically go too deep
            if (height < defaultSeaLevel - 30) {
                height = -(defaultSeaLevel - 30 - height) * 0.1 + defaultSeaLevel - 30;
            }

            return height;
        }
    }, MOUNTAIN {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise cubic = computeNoise(tw, world -> {
                FastNoise n = new FastNoise((int) tw.getSeed() * 7);
                n.SetNoiseType(NoiseType.CubicFractal);
                n.SetFractalOctaves(6);
                n.SetFrequency(TConfigOption.HEIGHT_MAP_MOUNTAIN_FREQUENCY.getFloat());
                return n;
            });

            double height = cubic.GetNoise(x, z) * 5;
            if (height < 0) height = 0;
            return Math.pow(height, 5) * 5;
        }
    }, ATTRITION {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise perlin = computeNoise(tw, world -> {
                FastNoise n = new FastNoise((int) tw.getSeed());
                n.SetNoiseType(NoiseType.PerlinFractal);
                n.SetFractalOctaves(4);
                n.SetFrequency(0.02f);
                return n;
            });

            double height = perlin.GetNoise(x, z) * 2 * 7;
            return height < 0 ? 0 : height;
        }
    };

    private static final int defaultSeaLevel = 62;
    private static final float heightAmplifier = TConfigOption.HEIGHT_MAP_LAND_HEIGHT_AMPLIFIER.getFloat();
    protected final Map<TerraformWorld, FastNoise> noiseCache = Collections.synchronizedMap(new IdentityHashMap<>(TerraformWorld.WORLDS.size()));

    protected FastNoise computeNoise(TerraformWorld world, Function<TerraformWorld, FastNoise> noiseFunction) {
        synchronized(noiseCache) {
            return noiseCache.computeIfAbsent(world, noiseFunction);
        }
    }

    /**
     * Returns the average increase or decrease in height for surrounding blocks compared to the provided height at those coords.
     * 1.5 for a radius of 3 is considered steep.
     */
    public static double getNoiseGradient(TerraformWorld tw, int x, int z, int radius) {
        double totalChangeInGradient = 0;
        int count = 0;
        double centerNoise = getBlockHeight(tw, x, z);
        for (int nx = -radius; nx <= radius; nx++)
            for (int nz = -radius; nz <= radius; nz++) {
                if (nx == 0 && nz == 0) continue;
                //Bukkit.getLogger().info(nx + "," + nz + ":"+(getHeight(tw,x+nx,z+nz)-centerNoise));
                totalChangeInGradient += Math.abs(getBlockHeight(tw, x + nx, z + nz) - centerNoise);
                count++;
            }
        //Bukkit.getLogger().info("Count: " + count);
        //Bukkit.getLogger().info("Total: " + totalChangeInGradient);

        return totalChangeInGradient / count;
    }

    /**
     * Used for calculating biomes and calculating terrain shapes.
     * When used with biomes, output value should be type casted to int.
     */
    public static double getRiverlessHeight(TerraformWorld tw, int x, int z) {
        double height = HeightMap.CORE.getHeight(tw, x, z);

        if (height > defaultSeaLevel + 4) {
            height += HeightMap.ATTRITION.getHeight(tw, x, z);
        } else {
            height += HeightMap.ATTRITION.getHeight(tw, x, z) * 0.8;
        }

        //double oldHeight = height;
        if (height > defaultSeaLevel + 4) {
            height += HeightMap.MOUNTAIN.getHeight(tw, x, z);
        } else {
            float frac = (float) height / (float) (TerraformGenerator.seaLevel + 4);
            height += HeightMap.MOUNTAIN.getHeight(tw, x, z) * (frac);
        }

        if (height > 200) height = 200 + (height - 200) * 0.5;
        if (height > 230) height = 230 + (height - 230) * 0.3;
        if (height > 240) height = 240 + (height - 240) * 0.1;
        if (height > 250) height = 250 + (height - 250) * 0.05;

        return height + HeightMap.OCEANIC.getHeight(tw, x, z);
    }

    public static double getPreciseHeight(TerraformWorld tw, int x, int z) {
        ChunkCache cache = TerraformGenerator.getCache(tw, x, z);

        double cachedValue = cache.getHeight(x, z);
        if (cachedValue != 0) return cachedValue;

        double height = HeightMap.CORE.getHeight(tw, x, z);

        if (height > defaultSeaLevel + 4) {
            height += HeightMap.ATTRITION.getHeight(tw, x, z);
        } else {
            height += HeightMap.ATTRITION.getHeight(tw, x, z) * 0.8;
        }

        if (height > defaultSeaLevel + 4) {
            height += HeightMap.MOUNTAIN.getHeight(tw, x, z);
        } else {
            float frac = (float) height / (float) (TerraformGenerator.seaLevel + 4);
            height += HeightMap.MOUNTAIN.getHeight(tw, x, z) * (frac);
        }

        if (height > 200) height = 200 + (height - 200) * 0.5;
        if (height > 230) height = 230 + (height - 230) * 0.3;
        if (height > 240) height = 240 + (height - 240) * 0.1;
        if (height > 250) height = 250 + (height - 250) * 0.05;

        //Oceans
        height += HeightMap.OCEANIC.getHeight(tw, x, z);

        //River Depth
        double depth = HeightMap.RIVER.getHeight(tw, x, z);
        depth = depth < 0 ? 0 : depth;

        //Normal scenario: Shallow area
        if (height - depth >= TerraformGenerator.seaLevel - 15) {
            height -= depth;

            //Fix for underwater river carving: Don't carve deeply
        } else if (height > TerraformGenerator.seaLevel - 15 && height - depth < TerraformGenerator.seaLevel - 15) {
            height = TerraformGenerator.seaLevel - 15;
        }

        if (heightAmplifier != 1f && height > TerraformGenerator.seaLevel) height += heightAmplifier * (height - TerraformGenerator.seaLevel);

        cache.cacheHeight(x, z, height);
        return height;
    }

    public static int getBlockHeight(TerraformWorld tw, int x, int z) {
        return (int) getPreciseHeight(tw, x, z);
    }

    public abstract double getHeight(TerraformWorld tw, int x, int z);
}
