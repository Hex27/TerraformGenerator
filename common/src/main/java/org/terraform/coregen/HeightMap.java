package org.terraform.coregen;

import java.util.ArrayList;

import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
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
            FastNoise cubic = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.HEIGHTMAP_CORE, world -> {
                FastNoise n = new FastNoise((int) world.getSeed());
                n.SetNoiseType(NoiseType.CubicFractal);
                n.SetFractalOctaves(3);
                n.SetFrequency(TConfigOption.HEIGHT_MAP_CORE_FREQUENCY.getFloat());
                return n;
            });

            double height = cubic.GetNoise(x, z) * 2 * 5 + 7 + defaultSeaLevel;

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
    },
//    MOUNTAINOUS {
//        @Override
//        public double getHeight(TerraformWorld tw, int x, int z) {
//            FastNoise attrition = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.HEIGHTMAP_MOUNTAINOUS, world -> {
//                FastNoise n = new FastNoise((int) world.getSeed()/4);
//                n.SetNoiseType(NoiseType.SimplexFractal);
//                n.SetFractalOctaves(6);
//                n.SetFrequency(0.002f);
//                return n;
//            });
//
//            double attritionHeight = Math.pow(Math.abs(attrition.GetNoise(x,z) * 31),1.5);
//            
//            double height = HeightMap.CORE.getHeight(tw, x, z) + attritionHeight;
//            
//            //Remove river carving
//            //I am the pinnacle of efficiency
//            height += getRawRiverDepth(tw,x,z);
//            
//            return height;
//        }
//    }, 
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
        //Bukkit.getLogger().info("Count: " + count);
        //Bukkit.getLogger().info("Total: " + totalChangeInGradient);

        return totalChangeInGradient / count;
    }
    
    /**
     * Returns the average increase or decrease in height for surrounding blocks compared to the provided height at those coords.
     * 1.5 for a radius of 3 is considered steep.
     * Does GenUtils.getHighestGround to get height values.
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
        //Bukkit.getLogger().info("Count: " + count);
        //Bukkit.getLogger().info("Total: " + totalChangeInGradient);

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
        if (cachedValue != 0) return cachedValue;

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

    
    private static float getDominantBiomeHeight(TerraformWorld tw, int x, int z) {
    	ChunkCache cache = TerraformGenerator.getCache(tw, x, z);
    	float h = cache.getDominantBiomeHeight(x, z);
    	if(h == Float.MIN_VALUE) {
            h = (float) BiomeBank.calculateHeightIndependentBiome(tw, x, z)
                    .getHandler().calculateHeight(tw, x, z);
            if(Math.pow(x,2) + Math.pow(z,2) < spawnFlatRadiusSquared)
                h = (float) HeightMap.CORE.getHeight(tw,x,z);
        }
        cache.cacheDominantBiomeHeight(x, z, h);
    	return h;
    }
    
    /**
     * Biome calculations are done here as well.
     * 
     * This function is responsible for applying blurring to merge biomes together
     * @param tw
     * @param x
     * @param z
     * @return
     */
    public static double getRiverlessHeight(TerraformWorld tw, int x, int z) {
    	
    	int maskRadius = 5;
    	int maskDiameter = (maskRadius*2) + 1;
    	//int maskDiameterSquared = maskDiameter*maskDiameter;
    	double coreHeight = 0;

		ChunkCache mainCache = TerraformGenerator.getCache(tw, x, z);
		
		//If this chunk cache hasn't cached a blurred value, 
		if(mainCache.getBlurredHeight(x, z) == Float.MIN_VALUE) {
			
			//Box blur across the biome section
	    	//MegaChunk mc = new MegaChunk(x, 0, z);
	    	BiomeSection sect = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);
	    	
	    	//Some extra Z values will be calculated to allow for blurring across sections.
	    	//These values must be deleted afterwards.
	    	ArrayList<ChunkCache> toPurgeValues = new ArrayList<ChunkCache>();
	    	
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
                    if(targetCache.getIntermediateBlurHeight(relX, relZ) == Float.MIN_VALUE)
                    {
                        //add these to toPurgeValues as they must be deleted after Z calculation.
                        targetCache.cacheIntermediateBlurredHeight(relX, relZ, lineTotalHeight/maskDiameter);
                        //toPurgeValues.add(targetCache);
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
//		    			if(queryCache != targetCache) {
//		    				//This is a little suspicious, because this whole optimisation relies
//		    				//on the fact that this thing is supposed to already be blurred 
//		    				//in the X direction, but this seems* to be ok. May produce weird
//		    				//artifacts in the Z direction.
//		    				lineTotalHeight += getDominantBiomeHeight(tw, relX, relZ + offsetZ);
//		    			}
//		    			else
		    			
		    			//Note, this may accidentally blur twice for some Z values if 
		    			//chunks generate in a specific weird order. That's (probably) fine.
	    				lineTotalHeight += queryCache.getIntermediateBlurHeight(relX, relZ + offsetZ);
		    		}
		    		//final blurred value
		    		targetCache.cacheBlurredHeight(relX, relZ, lineTotalHeight/maskDiameter);
	        	}
	    	}
	    	
//	    	for(ChunkCache toPurge:toPurgeValues) {
//	        	for(short i = 0; i < 16; i++)
//	        		for(short j = 0; j < 16; j++) {
//	        			toPurge.blurredHeightCache[i][j] = Float.MIN_VALUE;
//	        		}
//	    	}
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
