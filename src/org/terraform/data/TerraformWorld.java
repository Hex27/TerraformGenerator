package org.terraform.data;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.terraform.biome.BiomeBank;
import org.terraform.main.TConfigOption;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class TerraformWorld {
    public static final HashMap<String, TerraformWorld> WORLDS = new HashMap<>();
    private final String worldName;
    private final long seed;
    private transient FastNoise tempOctave;
    private transient FastNoise moistureOctave;

    private TerraformWorld(String name, long seed) {
        this.worldName = name;
        this.seed = seed;
    }

    private TerraformWorld(World world) {
        this.worldName = world.getName();
        this.seed = world.getSeed();
    }

    public static TerraformWorld get(World world) {
        return WORLDS.computeIfAbsent(world.getName(), (k) -> new TerraformWorld(world));
    }

    public static TerraformWorld get(String name, long seed) {
        return WORLDS.computeIfAbsent(name, (k) -> new TerraformWorld(name, seed));
    }

    public long getSeed() {
        return seed;
    }

    private FastNoise getTemperatureOctave() {
        if (tempOctave == null) {
            tempOctave = new FastNoise((int) (seed * 2));
            tempOctave.SetNoiseType(NoiseType.ValueFractal);
            tempOctave.SetFractalOctaves(6);
            tempOctave.SetFrequency(TConfigOption.BIOME_TEMPERATURE_FREQUENCY.getFloat()); //Was 0.0006
        }
        return tempOctave;
    }

    private FastNoise getMoistureOctave() {
        if (moistureOctave == null) {
            moistureOctave = new FastNoise((int) (seed * 3));
            moistureOctave.SetNoiseType(NoiseType.ValueFractal);
            moistureOctave.SetFractalOctaves(6);
            moistureOctave.SetFrequency(TConfigOption.BIOME_MOISTURE_FREQUENCY.getFloat());
        }
        return moistureOctave;
    }

    public Random getRand(long d) {
        return new Random(seed * d);
    }

    public Random getHashedRand(long x, int y, int z) {
        return new Random(Objects.hash(seed, x, y, z));
    }

    public Random getHashedRand(int x, int y, int z, long multiplier) {
        return new Random(Objects.hash(seed, x, y, z) * multiplier);
    }

    //HashMap<int[], BiomeBank> banks = new HashMap<>();


    public BiomeBank getBiomeBank(int x, int height, int z) {
//		BiomeBank bank = banks.get(new int[]{x,z});
//		if(bank == null){
//			bank = BiomeBank.calculateBiome(this, getTemperature(x,z), height);
//			banks.put(new int[]{x, z}, bank);
//		}
//		return bank;
        return BiomeBank.calculateBiome(this, x, z, height);
    }

    /**
     * @param x block x
     * @param z block z
     * @return a value from -2.5 to 2.5 inclusive.
     */
    public double getTemperature(int x, int z) {
        //double temp = 1.0;
        return (getTemperatureOctave().GetNoise(x, z) * 2) * 3;
    }

    /**
     * @param x block x
     * @param z block z
     * @return a value from -2.5 to 2.5 inclusive.
     */
    public double getMoisture(int x, int z) {
        //double moisture = -2.5;
        return getMoistureOctave().GetNoise(x, z) * 2 * 3;
    }

    public String getName() {
        return worldName;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }
}
