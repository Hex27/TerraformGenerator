package org.terraform.coregen;

import java.util.HashMap;
import java.util.Objects;

import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;

public abstract class HeightMap {
    private static final int defaultSeaLevel = 62;
    private static final HashMap<Integer, FastNoise> noiseCache = new HashMap<>();

    /**
     * Returns the average increase or decrease in height for surrounding blocks compared to the provided height at those coords.
     * 1.5 for a radius of 3 is considered steep.
     */
    public static double getNoiseGradient(TerraformWorld tw, int x, int z, int radius) {
        double totalChangeInGradient = 0;
        int count = 0;
        double centerNoise = getHeight(tw, x, z);
        for (int nx = -radius; nx <= radius; nx++)
            for (int nz = -radius; nz <= radius; nz++) {
                if (nx == 0 && nz == 0) continue;
                //Bukkit.getLogger().info(nx + "," + nz + ":"+(getHeight(tw,x+nx,z+nz)-centerNoise));
                totalChangeInGradient += Math.abs(getHeight(tw, x + nx, z + nz) - centerNoise);
                count++;
            }
        //Bukkit.getLogger().info("Count: " + count);
        //Bukkit.getLogger().info("Total: " + totalChangeInGradient);

        return totalChangeInGradient / count;
    }
    
    /**
     * @return Current river depth, also returns
     *         negative values if on dry ground.
     */
    public static double getRiverDepth(TerraformWorld tw, int x, int z) {
    	int hash = Objects.hash(tw,"river");
    	FastNoise noise = noiseCache.get(hash);
    	if(noise == null) {
            noise = new FastNoise();
            noise.SetSeed((int) tw.getSeed());
            noise.SetNoiseType(NoiseType.PerlinFractal);
            noise.SetFrequency(0.005f);
            noise.SetFractalOctaves(5);
            noiseCache.put(hash,noise);
    	}

        return 15 - 100 * Math.abs(noise.GetNoise(x, z));
    }

    public static double getOceanicHeight(TerraformWorld tw, int x, int z) {
       	int hash = Objects.hash(tw,"oceanic");
    	FastNoise cubic = noiseCache.get(hash);
    	if(cubic == null) {
            cubic = new FastNoise((int) tw.getSeed() * 12);
            cubic.SetNoiseType(NoiseType.CubicFractal);
            cubic.SetFractalOctaves(6);
            cubic.SetFrequency(TConfigOption.HEIGHT_MAP_OCEANIC_FREQUENCY.getFloat());
            noiseCache.put(hash, cubic);
    	}
        
        double height = cubic.GetNoise(x, z) * 2.5;

        //Only negative height (Downwards)
        if (height > 0) height = 0;
        return height * 50; //Depth
    }

    public static double getCoreHeight(TerraformWorld tw, int x, int z) {
    	int hash = Objects.hash(tw,"core");
    	FastNoise cubic = noiseCache.get(hash);
    	if(cubic == null) {
    		cubic = new FastNoise((int) tw.getSeed());
            cubic.SetNoiseType(NoiseType.CubicFractal);
            cubic.SetFractalOctaves(6);
            cubic.SetFrequency(0.003f);
    		noiseCache.put(hash, cubic);
    	}
    	
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

    public static double getAttritionHeight(TerraformWorld tw, int x, int z) {
    	int hash = Objects.hash(tw,"attrition");
    	FastNoise perlin = noiseCache.get(hash);
    	if(perlin == null) {
            perlin = new FastNoise((int) tw.getSeed());
            perlin.SetNoiseType(NoiseType.PerlinFractal);
            perlin.SetFractalOctaves(4);
            perlin.SetFrequency(0.02f);
    		noiseCache.put(hash, perlin);
    	}
    	
        double height = perlin.GetNoise(x, z) * 2 * 7;
        return height < 0 ? 0 : height;
    }

    public static double getMountainousHeight(TerraformWorld tw, int x, int z) {

    	int hash = Objects.hash(tw,"mountain");
    	FastNoise cubic = noiseCache.get(hash);
    	if(cubic == null) {
            cubic = new FastNoise((int) tw.getSeed() * 7);
            cubic.SetNoiseType(NoiseType.CubicFractal);
            cubic.SetFractalOctaves(6);
            cubic.SetFrequency(TConfigOption.HEIGHT_MAP_MOUNTAIN_FREQUENCY.getFloat());
    		noiseCache.put(hash, cubic);
    	}
        
        double height = cubic.GetNoise(x, z) * 5;
        if (height < 0) height = 0;
        return Math.pow(height, 5) * 5;
    }

    public static double getPreciseHeight(TerraformWorld tw, int x, int z) {
        double height = getCoreHeight(tw, x, z);

        if (height > defaultSeaLevel + 4) {
            height += getAttritionHeight(tw, x, z);
        } else {
            height += getAttritionHeight(tw, x, z) * 0.8;
        }

        if (height > defaultSeaLevel + 4) {
            height += getMountainousHeight(tw, x, z);
        } else {
            float frac = (float) height / (float) (TerraformGenerator.seaLevel + 4);
            height += getMountainousHeight(tw, x, z) * (frac);
        }

        if (height > 200) height = 200 + (height - 200) * 0.5;
        if (height > 230) height = 230 + (height - 230) * 0.3;
        if (height > 240) height = 240 + (height - 240) * 0.1;
        if (height > 250) height = 250 + (height - 250) * 0.05;

        //Oceans
        height += getOceanicHeight(tw, x, z);

        //River Depth
        double depth = getRiverDepth(tw, x, z);
        depth = depth < 0 ? 0 : depth;

        //Normal scenario: Shallow area
        if (height - depth >= TerraformGenerator.seaLevel - 15) {
            height -= depth;

            //Fix for underwater river carving: Don't carve deeply
        } else if (height > TerraformGenerator.seaLevel - 15
                && height - depth < TerraformGenerator.seaLevel - 15) {
            height = TerraformGenerator.seaLevel - 15;
        }

        return height;
    }

    public static int getHeight(TerraformWorld tw, int x, int z) {
        return (int) getPreciseHeight(tw, x, z);
    }

    /**
     * Used for calculating biomes and calculating terrain shapes.
     * When used with biomes, output value should be type casted to int.
     */
    public static double getRiverlessHeight(TerraformWorld tw, int x, int z) {
        double height = getCoreHeight(tw, x, z);

        if (height > defaultSeaLevel + 4) {
            height += getAttritionHeight(tw, x, z);
        } else {
            height += getAttritionHeight(tw, x, z) * 0.8;
        }

        //double oldHeight = height;
        if (height > defaultSeaLevel + 4) {
            height += getMountainousHeight(tw, x, z);
        } else {
            float frac = (float) height / (float) (TerraformGenerator.seaLevel + 4);
            height += getMountainousHeight(tw, x, z) * (frac);
        }

        if (height > 200) height = 200 + (height - 200) * 0.5;
        if (height > 230) height = 230 + (height - 230) * 0.3;
        if (height > 240) height = 240 + (height - 240) * 0.1;
        if (height > 250) height = 250 + (height - 250) * 0.05;

        return height + getOceanicHeight(tw, x, z);
    }
}
