package org.terraform.coregen;

import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;

public abstract class HeightMap {
	
	private static final int defaultSeaLevel = 62;
	
	public static double getRiverDepth(TerraformWorld tw, int x, int z){
		double depth = 15-100*riverRidge(tw,x,z);
		if(depth < 0) depth = 0;
		return depth;
	}
	
	private static double riverRidge(TerraformWorld tw, int nx, int ny){
		FastNoise noise = new FastNoise();
		noise.SetSeed((int) tw.getSeed());
        noise.SetNoiseType(NoiseType.PerlinFractal);
        noise.SetFrequency(0.005f);
        noise.SetFractalOctaves(5);
        double n = noise.GetNoise(nx, ny);
        //if(n > 0) n = 0;
        return (Math.abs(n));
	}
	
	public static double getOceanicHeight(TerraformWorld tw, int x, int z) {
		FastNoise cubic = new FastNoise((int) tw.getSeed()*12);
		cubic.SetNoiseType(NoiseType.CubicFractal);
		cubic.SetFractalOctaves(6);
		
		cubic.SetFrequency(TConfigOption.HEIGHT_MAP_OCEANIC_FREQUENCY.getFloat());
		double height = cubic.GetNoise(x, z)*2.5;
		
		//Only negative height (Downwards)
		if(height > 0) height = 0;
		
		height = height*50; //Depth
		
		return height;
	}
	
	public static double getCoreHeight(TerraformWorld tw, int x, int z){

		FastNoise cubic = new FastNoise((int) tw.getSeed());
		cubic.SetNoiseType(NoiseType.CubicFractal);
		cubic.SetFractalOctaves(6);
		cubic.SetFrequency(0.003f);
		
		double height = cubic.GetNoise(x, z)*2*15 + 13 + defaultSeaLevel;
		
		//Ensure that height doesn't automatically go upwards sharply
		if(height > defaultSeaLevel + 10){
			height = (height-defaultSeaLevel-10)*0.1 + defaultSeaLevel + 10;
		}
		
		//Ensure that height doesn't automatically go too deep
		if(height < defaultSeaLevel - 30){
			height = -(defaultSeaLevel-30-height)*0.1 + defaultSeaLevel -30;
		}
		
		return height;
	}
	
	public static double getAttritionHeight(TerraformWorld tw, int x, int z){
		FastNoise perlin = new FastNoise((int) tw.getSeed());
		perlin.SetNoiseType(NoiseType.PerlinFractal);
		perlin.SetFractalOctaves(4);
		perlin.SetFrequency(0.02f);
		double height = perlin.GetNoise(x, z)*2*7;
		if(height < 0) height = 0;
		return height;
	}
	
	public static double getMountainousHeight(TerraformWorld tw, int x, int z){
		FastNoise cubic = new FastNoise((int) tw.getSeed()*7);
		cubic.SetNoiseType(NoiseType.CubicFractal);
		cubic.SetFractalOctaves(6);
		
		cubic.SetFrequency(TConfigOption.HEIGHT_MAP_MOUNTAIN_FREQUENCY.getFloat());
		double height = cubic.GetNoise(x, z)*5;
		if(height < 0) height = 0;
		height = Math.pow(height, 5)*5; 
		
		return height;
	}
	
	public static int getHeight(TerraformWorld tw, int x, int z){
		double height = getCoreHeight(tw,x,z);
		
		if(height > defaultSeaLevel + 4){
			height += getAttritionHeight(tw,x,z);
		}else{
			height += getAttritionHeight(tw,x,z)*0.8;
		}
		
		if(height > defaultSeaLevel + 4){
			height += getMountainousHeight(tw,x,z);
		}else{
			float frac = (float) ((float)height / (float) (TerraformGenerator.seaLevel+4));
			height += getMountainousHeight(tw,x,z)*(frac);
		}
		
		if(height > 200) height = 200 + (height-200)*0.5;
		if(height > 230) height = 230 + (height-230)*0.3;
		if(height > 240) height = 240 + (height-240)*0.1;
		if(height > 250) height = 250 + (height-250)*0.05;

		//Oceans
		height += getOceanicHeight(tw,x,z);
		
		//River Depth
		double depth = getRiverDepth(tw,x,z);
		
		//Normal scenario: Shallow area
		if(height - depth >= TerraformGenerator.seaLevel-15){
			height -= depth;
			
	    //Fix for underwater river carving: Don't carve deeply
		}else if(height > TerraformGenerator.seaLevel-15
				&& height - depth < TerraformGenerator.seaLevel-15){
			height = TerraformGenerator.seaLevel - 15;
		}
		
		return (int) height;
	}
	
	//Used for calculating biomes
	public static int getRiverlessHeight(TerraformWorld tw, int x, int z){
		double height = getCoreHeight(tw,x,z);
		
		if(height > defaultSeaLevel + 4){
			height += getAttritionHeight(tw,x,z);
		}else{
			height += getAttritionHeight(tw,x,z)*0.8;
		}
		
		//double oldHeight = height;
		if(height > defaultSeaLevel + 4){
			height += getMountainousHeight(tw,x,z);
		}else{
			float frac = (float) ((float)height / (float) (TerraformGenerator.seaLevel+4));
			height += getMountainousHeight(tw,x,z)*(frac);
		}
		
		if(height > 200) height = 200 + (height-200)*0.5;
		if(height > 230) height = 230 + (height-230)*0.3;
		if(height > 240) height = 240 + (height-240)*0.1;
		if(height > 250) height = 250 + (height-250)*0.05;

		height += getOceanicHeight(tw,x,z);
		
		return (int) height;
	}

}
