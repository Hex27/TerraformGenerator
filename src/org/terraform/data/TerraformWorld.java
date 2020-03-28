package org.terraform.data;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.terraform.biome.BiomeBank;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.TickTimer;

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
		TickTimer timer = new TickTimer("gettemperatureoctave");
		if(tempOctave == null){
			tempOctave = new FastNoise((int) (getSeed()*2));
			tempOctave.SetNoiseType(NoiseType.ValueFractal);
			tempOctave.SetFractalOctaves(6);
			tempOctave.SetFrequency(0.0006f); //Was 0.0008, Was 0.0004.
		}
		timer.finish();
		return tempOctave;
	}
	
	private FastNoise getMoistureOctave(){
		TickTimer timer = new TickTimer("getMoistureOctave");
		if(moistureOctave == null){
			moistureOctave = new FastNoise((int) (getSeed()*3));
			tempOctave.SetNoiseType(NoiseType.ValueFractal);
			moistureOctave.SetFrequency(0.001f);
		}
		timer.finish();
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
		return BiomeBank.calculateBiome(this, getTemperature(x,z), height);
	}
	
	/**
	 * 
	 * @param x block x
	 * @param z block z
	 * @return a value from 0 to 3 inclusive.
	 */
	public double getTemperature(int x, int z){
		TickTimer timer = new TickTimer("gettemperature");
		double temp = (getTemperatureOctave().GetNoise(x, z)*2)*3;
		timer.finish();
		return temp;
	}

	/**
	 * 
	 * @param x block x
	 * @param z block z
	 * @return
	 */
	public double getMoisture(int x, int z){
		TickTimer timer = new TickTimer("getmoisture");
		double temp = Math.abs(getMoistureOctave().GetNoise(x, z)*2)*3;
		timer.finish();
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
