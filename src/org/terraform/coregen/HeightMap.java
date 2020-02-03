package org.terraform.coregen;

import java.util.Random;

import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;

public class HeightMap {
	
	public double getCoreHeight(TerraformWorld tw, int x, int z){
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(tw.getRand(1), 8);
//		gen.setScale(0.007D);
//		
//		double height = gen.noise(x,z,1,0.5)*25+TerraformGenerator.seaLevel;
		
		FastNoise cubic = new FastNoise((int) tw.getSeed());
		cubic.SetNoiseType(NoiseType.CubicFractal);
		cubic.SetFractalOctaves(6);
		cubic.SetFrequency(0.002f); //Was: 0.003
		
		double height = cubic.GetNoise(x, z)*2*50 + TerraformGenerator.seaLevel;
		
		if(height > TerraformGenerator.seaLevel + 10){
			height = (height-TerraformGenerator.seaLevel-10)*0.1 + TerraformGenerator.seaLevel + 10;
		}
		if(height < TerraformGenerator.seaLevel - 30){
			height = -(TerraformGenerator.seaLevel-30-height)*0.1 + TerraformGenerator.seaLevel -30;
		}
		
		return height;
	}
	
	public double getAttritionHeight(TerraformWorld tw, int x, int z){
		FastNoise perlin = new FastNoise((int) tw.getSeed());
		perlin.SetNoiseType(NoiseType.PerlinFractal);
		perlin.SetFractalOctaves(3);
		perlin.SetFrequency(0.01f);
		double height = perlin.GetNoise(x, z)*2*7;
		if(height < 0) height = 0;
		return height;
	}
	
	public double getMountainousHeight(TerraformWorld tw, int x, int z){
		FastNoise cubic = new FastNoise((int) tw.getSeed()*7);
		cubic.SetNoiseType(NoiseType.CubicFractal);
		cubic.SetFractalOctaves(6);
		cubic.SetFrequency(0.002f);
		double height = cubic.GetNoise(x, z)*5;
		if(height < 0) height = 0;
		height = Math.pow(height, 5)*5; 
		//Was 33. Gonna cut it heavily to 5. 
		//Was 35. Let's cut it slightly to 33.
		
		return height;
	}
	
	public int getHeight(TerraformWorld tw, int x, int z){
		double height = getCoreHeight(tw,x,z);
		
		if(height > TerraformGenerator.seaLevel + 4){
			height += getAttritionHeight(tw,x,z);
		}else{
			height += getAttritionHeight(tw,x,z)*0.8;
		}
		
		//double oldHeight = height;
		if(height > TerraformGenerator.seaLevel + 4){
			height += getMountainousHeight(tw,x,z);
		}else{
			float frac = (float) ((float)height / (float) (TerraformGenerator.seaLevel+4));
			height += getMountainousHeight(tw,x,z)*(frac);
		}
		
		if(height > 200) height = 200 + (height-200)*0.5;
		if(height > 230) height = 230 + (height-230)*0.3;
		if(height > 240) height = 240 + (height-240)*0.1;
		if(height > 250) height = 250 + (height-250)*0.05;
		
		return (int) height;
	}
	
	private int getPetriDishWorld(TerraformWorld tw, int x, int z){
		FastNoise myNoise = new FastNoise(); // Create a FastNoise object
		myNoise.SetSeed((int) tw.getSeed());
		myNoise.SetNoiseType(NoiseType.Simplex);
		myNoise.SetFrequency(0.001f);
		
		SimplexOctaveGenerator oceanGen = new SimplexOctaveGenerator(tw.getRand(2),1);
		oceanGen.setScale(0.03D);
		
        SimplexOctaveGenerator gen = new SimplexOctaveGenerator(tw.getRand(1), 8);
		gen.setScale(2*0.005D);
		
		PerlinOctaveGenerator pgen = new PerlinOctaveGenerator(tw.getRand(1), 8);
		pgen.setScale(0.005D);

		double mountainGenerator = myNoise.GetNoise(x,z)*2*50;
		if(mountainGenerator < 0) mountainGenerator = 0;
		
		double perlin = pgen.noise(x, z, 0.1, 0.5)*60;
		double simplex = gen.noise(x, z, 0.1, 0.5)*30;

		double core = ((50 + (perlin+simplex)/2));
		int height = (int) core;
		if(height > TerraformGenerator.seaLevel){
			height += mountainGenerator;
		}else{
			float frac = (float) ((float)height / (float) TerraformGenerator.seaLevel);
			height += mountainGenerator*(frac);
		}
		return height;
	}

}
