package org.terraform.data;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.terraform.biome.BiomeBank;
import org.terraform.main.TConfigOption;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;

public class TerraformWorld{
	
	private String worldName;
	private long seed;
	
	public long getSeed(){
		return seed;
	}
	
	public static HashMap<String, TerraformWorld> worlds = new HashMap<>();
	
	public static TerraformWorld get(World world){
		if(worlds.containsKey(world.getName()))
			return worlds.get(world.getName());
		TerraformWorld tw = new TerraformWorld(world);
		worlds.put(world.getName(),tw);
		return tw;
	}
	
	public static TerraformWorld get(String name, long seed){
		if(worlds.containsKey(name))
			return worlds.get(name);
		TerraformWorld tw = new TerraformWorld(name, seed);
		worlds.put(name,tw);
		return tw;
	}
	
	private TerraformWorld(String name, long seed){
		this.worldName = name;
		this.seed = seed;
	}
	
	private TerraformWorld(World world) {
		this.worldName = world.getName();
		this.seed = world.getSeed();
	}

	private transient FastNoise tempOctave;
	private transient FastNoise moistureOctave;
	
	private FastNoise getTemperatureOctave(){
		if(tempOctave == null){
			tempOctave = new FastNoise((int) (getSeed()*2));
			tempOctave.SetNoiseType(NoiseType.ValueFractal);
			tempOctave.SetFractalOctaves(6);
			tempOctave.SetFrequency(TConfigOption.BIOME_TEMPERATURE_FREQUENCY.getFloat()); //Was 0.0006
		}
		return tempOctave;
	}
	
	private FastNoise getMoistureOctave(){
		if(moistureOctave == null){
			moistureOctave = new FastNoise((int) (getSeed()*3));
			moistureOctave.SetNoiseType(NoiseType.ValueFractal);
			moistureOctave.SetFractalOctaves(6);
			moistureOctave.SetFrequency(TConfigOption.BIOME_MOISTURE_FREQUENCY.getFloat());
		}
		return moistureOctave;
	}
	
	public double getRiverDepth(int x, int z){
		double depth = 15-100*ridge(x,z);
		if(depth < 0) depth = 0;
		return depth;
	}
	
	private double ridge(int nx, int ny){
		FastNoise noise = new FastNoise();
        noise.SetNoiseType(NoiseType.PerlinFractal);
        noise.SetFrequency(0.005f);
        noise.SetFractalOctaves(5);
        double n = noise.GetNoise(nx, ny);
        //if(n > 0) n = 0;
        return (Math.abs(n));
	}
	
	public Random getRand(long d){
		return new Random(seed*d);
	}
	
	public Random getHashedRand(int x, int y, int z){
		return new Random(Objects.hash(seed,x,y,z));
	}
	
	public Random getHashedRand(int x, int y, int z, long multiplier){
		return new Random(Objects.hash(seed,x,y,z) * multiplier);
	}
	
	//HashMap<int[], BiomeBank> banks = new HashMap<>();

	
	public BiomeBank getBiomeBank(int x, int height, int z){
//		BiomeBank bank = banks.get(new int[]{x,z});
//		if(bank == null){
//			bank = BiomeBank.calculateBiome(this, getTemperature(x,z), height);
//			banks.put(new int[]{x, z}, bank);
//		}
//		return bank;
		return BiomeBank.calculateBiome(this, x,z, height);
	}
	
	/**
	 * 
	 * @param x block x
	 * @param z block z
	 * @return a value from -5 to 5 inclusive.
	 */
	public double getTemperature(int x, int z){
		double temp = (getTemperatureOctave().GetNoise(x, z)*2)*3;
		return temp;
	}

	/**
	 * 
	 * @param x block x
	 * @param z block z
	 * @return a value from -5 to 5 inclusive.
	 */
	public double getMoisture(int x, int z){
		double temp = getMoistureOctave().GetNoise(x, z)*2*3;
		return temp;
	}
	
	public String getName() {
		// TODO Auto-generated method stub
		return worldName;
	}

	public World getWorld() {
		return Bukkit.getWorld(worldName);
	}

}
