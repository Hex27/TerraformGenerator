package org.terraform.utils;

import org.terraform.utils.FastNoise.NoiseType;

public class FastNoiseGenerator {
	
	private FastNoise noise;
	private int octaves;
	
	public FastNoiseGenerator(int seed, NoiseType type, float frequency, int octaves){
		this.noise = new FastNoise(seed);
		noise.SetNoiseType(type);
		noise.SetFrequency(frequency);
		this.octaves = octaves;
	}
	
	

}
