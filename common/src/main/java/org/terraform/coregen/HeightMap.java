package org.terraform.coregen;

import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

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
                n.SetFrequency(TConfigOption.HEIGHT_MAP_RIVER_FREQUENCY.getFloat());
                n.SetFractalOctaves(5);
                return n;
            });
            return 15 - 200 * Math.abs(noise.GetNoise(x, z));
        }
    },
    CORE {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise noise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.HEIGHTMAP_CORE, world -> {
                FastNoise n = new FastNoise((int) world.getSeed());
                n.SetNoiseType(NoiseType.SimplexFractal);
                n.SetFractalOctaves(2); //Poor detail after blurs. Rely on Attrition for detail
                n.SetFrequency(TConfigOption.HEIGHT_MAP_CORE_FREQUENCY.getFloat());
                return n;
            });

            //7 blocks elevated from the sea level
            double height = 10*noise.GetNoise(x, z) + 7 + TerraformGenerator.seaLevel;

            //Plateau-out height to make it flat-ish
            if (height > TerraformGenerator.seaLevel + 10) {
                height = (height - TerraformGenerator.seaLevel - 10) * 0.1 + TerraformGenerator.seaLevel + 10;
            }

            //This is fucking nonsense

            return height;
        }
    },
    ATTRITION {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise perlin = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.HEIGHTMAP_ATTRITION, world -> {
                FastNoise n = new FastNoise((int) world.getSeed()+113);
                n.SetNoiseType(NoiseType.PerlinFractal);
                n.SetFractalOctaves(4);
                n.SetFrequency(0.02f);
                return n;
            });

            double height = perlin.GetNoise(x, z) * 2 * 7;
            return height < 0 ? 0 : height;
        }
    };

    public static final int defaultSeaLevel = 62;
    public static final float heightAmplifier = TConfigOption.HEIGHT_MAP_LAND_HEIGHT_AMPLIFIER.getFloat();

    /**
     * Returns the average increase or decrease in height for surrounding blocks compared to the provided height at those coords.
     * 1.5 for a radius of 3 is considered steep.
     * Does noise calculations to find the true core height
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

        return totalChangeInGradient / count;
    }

    /**
     * Returns the average increase or decrease in height for surrounding blocks compared to the provided height at those coords.
     * 1.5 for a radius of 3 is considered steep.
     * Does GenUtils.getHighestGround to get height values.
     */
    /*TODO: There are several calls to this in Biome Handlers.
     * Write a version that uses transformed height.
     */
    public static double getTrueHeightGradient(PopulatorDataAbstract data, int x, int z, int radius) {
        double totalChangeInGradient = 0;
        int count = 0;
        double centerNoise = GenUtils.getHighestGround(data, x, z); //getBlockHeight(tw, x, z);
        for (int nx = -radius; nx <= radius; nx++)
            for (int nz = -radius; nz <= radius; nz++) {
                if (nx == 0 && nz == 0) continue;
                //Bukkit.getLogger().info(nx + "," + nz + ":"+(getHeight(tw,x+nx,z+nz)-centerNoise));
                totalChangeInGradient += Math.abs(GenUtils.getHighestGround(data, x+nx, z+nz) - centerNoise);
                count++;
            }

        return totalChangeInGradient / count;
    }

    public static double getRawRiverDepth(TerraformWorld tw, int x, int z) {
        if(Math.pow(x,2) + Math.pow(z,2) < spawnFlatRadiusSquared)
            return 0;
        double depth = HeightMap.RIVER.getHeight(tw, x, z);
        depth = depth < 0 ? 0 : depth;
        return depth;
    }

    public static int spawnFlatRadiusSquared = -324534;
    public static double getPreciseHeight(TerraformWorld tw, int x, int z) {
        ChunkCache cache = TerraformGenerator.getCache(tw, x, z);

        double cachedValue = cache.getHeightMapHeight(x, z);
        if (cachedValue != TerraformGeneratorPlugin.injector.getMinY()-1) return cachedValue;

        double height = getRiverlessHeight(tw,x,z);

        //River Depth
        double depth = getRawRiverDepth(tw,x,z);

        //Normal scenario: Shallow area
        if (height - depth >= TerraformGenerator.seaLevel - 15) {
            height -= depth;

            //Fix for underwater river carving: Don't carve deeply
        } else if (height > TerraformGenerator.seaLevel - 15
                && height - depth < TerraformGenerator.seaLevel - 15) {
            height = TerraformGenerator.seaLevel - 15;
        }

        if (heightAmplifier != 1f && height > TerraformGenerator.seaLevel)
            height += heightAmplifier * (height - TerraformGenerator.seaLevel);

        cache.cacheHeightMap(x, z, height);
        return height;
    }

    private static final int upscaleSize = 3;
    private static float getDominantBiomeHeight(TerraformWorld tw, int x, int z) {
        ChunkCache cache = TerraformGenerator.getCache(tw, x, z);
        float h = cache.getDominantBiomeHeight(x, z);
        if(h == TerraformGeneratorPlugin.injector.getMinY()-1) {
            //Upscale the biome
            if(x % upscaleSize != 0 && z % upscaleSize != 0)
                h = getDominantBiomeHeight(tw, x-(x%upscaleSize),z-(z%upscaleSize));
            else
            {
                h = (float) BiomeBank.calculateHeightIndependentBiome(tw, x, z)
                        .getHandler().calculateHeight(tw, x, z);
                if(Math.pow(x,2) + Math.pow(z,2) < spawnFlatRadiusSquared)
                    h = (float) HeightMap.CORE.getHeight(tw,x,z);
            }
        }
        cache.cacheDominantBiomeHeight(x, z, h);
        return h;
    }

    /**
     * Biome calculations are done here as well.
     * <br>
     * This function is responsible for applying blurring to merge biomes together
     * @return Near-final world height without rivers accounted for
     */
    public static double getRiverlessHeight(TerraformWorld tw, int x, int z) {

        int maskRadius = 5;
        int maskDiameter = (maskRadius*2) + 1;
        //int maskDiameterSquared = maskDiameter*maskDiameter;
        double coreHeight;

        ChunkCache mainCache = TerraformGenerator.getCache(tw, x, z);

        //If this chunk cache hasn't cached a blurred value,
        if(mainCache.getBlurredHeight(x, z) == TerraformGeneratorPlugin.injector.getMinY()-1) {

            //Box blur across the biome section
            //MegaChunk mc = new MegaChunk(x, 0, z);
            BiomeSection sect = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);

            //For every point in the biome section, blur across the X axis.
            for(int relX = sect.getLowerBounds().getX(); relX <= sect.getUpperBounds().getX(); relX++) {
                for(int relZ = sect.getLowerBounds().getZ() - maskRadius; relZ <= sect.getUpperBounds().getZ() + maskRadius; relZ++) {

                    ChunkCache targetCache = TerraformGenerator.getCache(tw, relX, relZ);
                    float lineTotalHeight = 0;
                    for(int offsetX = -maskRadius; offsetX <= maskRadius; offsetX++) {
                        lineTotalHeight += getDominantBiomeHeight(tw, relX + offsetX, relZ);
                    }

                    //Temporarily cache these X-Blurred values into chunkcache.
                    //Do not purge values that are legitimate.
                    if(targetCache.getIntermediateBlurHeight(relX, relZ) == TerraformGeneratorPlugin.injector.getMinY()-1)
                    {
                        targetCache.cacheIntermediateBlurredHeight(relX, relZ, lineTotalHeight/maskDiameter);
                    }
                }
            }

            //For every point in the biome section, blur across the Z axis.
            for(int relX = sect.getLowerBounds().getX(); relX <= sect.getUpperBounds().getX(); relX++) {
                for(int relZ = sect.getLowerBounds().getZ(); relZ <= sect.getUpperBounds().getZ(); relZ++) {

                    ChunkCache targetCache = TerraformGenerator.getCache(tw, relX, relZ);
                    float lineTotalHeight = 0;
                    for(int offsetZ = -maskRadius; offsetZ <= maskRadius; offsetZ++) {
                        ChunkCache queryCache = TerraformGenerator.getCache(tw, relX, relZ + offsetZ);

                        //Note, this may accidentally blur twice for some Z values if
                        //chunks generate in a specific weird order. That's (probably) fine.
                        lineTotalHeight += (float) queryCache.getIntermediateBlurHeight(relX, relZ + offsetZ);
                    }
                    //final blurred value
                    targetCache.cacheBlurredHeight(relX, relZ, lineTotalHeight/maskDiameter);
                }
            }
        }

        coreHeight = mainCache.getBlurredHeight(x, z);

        coreHeight += HeightMap.ATTRITION.getHeight(tw, x, z);

        return coreHeight;
    }

    public static int getBlockHeight(TerraformWorld tw, int x, int z) {
        return (int) getPreciseHeight(tw, x, z);
    }

    public abstract double getHeight(TerraformWorld tw, int x, int z);
}