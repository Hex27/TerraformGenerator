package org.terraform.coregen;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
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
            return 15 - 100 * Math.abs(noise.GetNoise(x, z));
        }
    },
    CORE {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise cubic = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.HEIGHTMAP_CORE, world -> {
                FastNoise n = new FastNoise((int) world.getSeed());
                n.SetNoiseType(NoiseType.CubicFractal);
                n.SetFractalOctaves(6);
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
    MOUNTAINOUS {
        @Override
        public double getHeight(TerraformWorld tw, int x, int z) {
            FastNoise attrition = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.HEIGHTMAP_MOUNTAINOUS, world -> {
                FastNoise n = new FastNoise((int) world.getSeed()/4);
                n.SetNoiseType(NoiseType.SimplexFractal);
                n.SetFractalOctaves(6);
                n.SetFrequency(0.002f);
                return n;
            });

            double attritionHeight = Math.pow(Math.abs(attrition.GetNoise(x,z) * 31),1.5);
            
            double height = HeightMap.CORE.getHeight(tw, x, z) + attritionHeight;
            
            //Remove river carving
            //I am the pinnacle of efficiency
            height += getRawRiverDepth(tw,x,z);
            
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
    	double depth = HeightMap.RIVER.getHeight(tw, x, z);
        depth = depth < 0 ? 0 : depth;
        return depth;
    }

    public static double getPreciseHeight(TerraformWorld tw, int x, int z) {
        ChunkCache cache = TerraformGenerator.getCache(tw, x, z);

        double cachedValue = cache.getHeight(x, z);
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

    	
        cache.cacheHeight(x, z, height);
        return height;
    }

    /**
     * Biome calculations are done here as well.
     * @param tw
     * @param x
     * @param z
     * @return
     */
    public static double getRiverlessHeight(TerraformWorld tw, int x, int z) {
    	
    	int maskRadius = 10;
    	int candidateCount = 81; //20*4 + 1
    	double totalHeight = BiomeBank.calculateHeightIndependentBiome(tw, x, z)
				.getHandler().calculateHeight(tw,x,z);
    	
		//First, blur by averaging horizontally, vertically and diagonally by maskRadius.
    	
    	//X dir
    	for(int nx = x-maskRadius; nx <= x+maskRadius; nx++) {
    		if(nx == x) continue;
			totalHeight += BiomeBank.calculateHeightIndependentBiome(tw, nx, z)
					.getHandler().calculateHeight(tw,nx,z);
    	}
    	
    	//Z dir
    	for(int nz = z-maskRadius; nz <= z+maskRadius; nz++) {
    		if(nz == z) continue;
			totalHeight += BiomeBank.calculateHeightIndependentBiome(tw, x, nz)
					.getHandler().calculateHeight(tw,x,nz);
    	}
    	
    	//-x to +x, -z to +z Diagonal
    	for(int rel = -maskRadius; rel <= maskRadius; rel++) {
    		int nx = x + rel;
    		int nz = z + rel;
    		if(nz == z && nx == x) continue;
			totalHeight += BiomeBank.calculateHeightIndependentBiome(tw, nx, nz)
					.getHandler().calculateHeight(tw,nx,nz);
    	}
    	//-x to +x, +z to -z Diagonal
    	for(int rel = -maskRadius; rel <= maskRadius; rel++) {
    		int nx = x + rel;
    		int nz = z - rel;
    		if(nz == z && nx == x) continue;
			totalHeight += BiomeBank.calculateHeightIndependentBiome(tw, nx, nz)
					.getHandler().calculateHeight(tw,nx,nz);
    	}
    	
    	double coreHeight = totalHeight/candidateCount;
    	
    	//Now, boxBlur coreHeight by 2D averaging with a much smaller radius
    	int boxBlurRadius = 3;
    	candidateCount = 49; //Math.pow(3*2+1,2)
    	totalHeight = 0;
    	for(int nx = x-boxBlurRadius; nx <= x+boxBlurRadius; nx++) {
    		for(int nz = z-boxBlurRadius; nz <= z+boxBlurRadius; nz++) {
    			if(nx == x&& nz == z)
    				totalHeight += coreHeight;
    			else
    				totalHeight += BiomeBank.calculateHeightIndependentBiome(tw, nx, nz)
    					.getHandler().calculateHeight(tw,nx,nz);
        	}
    	}
    	
    	coreHeight = totalHeight/candidateCount;
    	
    	coreHeight += HeightMap.ATTRITION.getHeight(tw, x, z);
    	
    	return coreHeight;
    }

    public static int getBlockHeight(TerraformWorld tw, int x, int z) {
        return (int) getPreciseHeight(tw, x, z);
    }

    public abstract double getHeight(TerraformWorld tw, int x, int z);
}
