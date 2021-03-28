package org.terraform.data;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
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
    private transient FastNoise oceanOctave;

    public TerraformWorld(String name, long seed) {
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

    public FastNoise getTemperatureOctave() {
        if (tempOctave == null) {
            tempOctave = new FastNoise((int) (seed * 2));
            tempOctave.SetNoiseType(NoiseType.Simplex);
            //tempOctave.SetFractalOctaves(3);
            tempOctave.SetFrequency(0.1f); //Was 0.0006
        }
        return tempOctave;
    }

    public FastNoise getMoistureOctave() {
        if (moistureOctave == null) {
            moistureOctave = new FastNoise((int) (seed/4));
            moistureOctave.SetNoiseType(NoiseType.Simplex);
            //moistureOctave.SetFractalOctaves(3);
            moistureOctave.SetFrequency(0.1f);
        }
        return moistureOctave;
    }

    public FastNoise getOceanOctave() {
        if (oceanOctave == null) {
        	oceanOctave = new FastNoise((int) getSeed() * 12);
        	oceanOctave.SetNoiseType(NoiseType.Simplex);
        	oceanOctave.SetFrequency(0.11f);
        	//oceanOctave.SetFrequency(TConfigOption.HEIGHT_MAP_OCEANIC_FREQUENCY.getFloat());
        }
        return oceanOctave;
    }
    
    public long getSeed() {
        return seed;
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

    /**
     * Same as getBiomeBank(x,y,z), except y is autofilled to be HeightMap.getBlockHeight
     * @param x blockX
     * @param z blockZ
     * @return
     */
    public BiomeBank getBiomeBank(int x, int z) {
    	
        ChunkCache cache = TerraformGenerator.getCache(this, x, z);
        BiomeBank cachedValue = cache.getBiome(x, z);
        if (cachedValue != null) return cachedValue;
        
    	int y = HeightMap.getBlockHeight(this, x, z);

        return cache.cacheBiome(x, z, BiomeBank.calculateBiome(this, x, y, z));
    }
    
    public BiomeBank getBiomeBank(int x, int y, int z) {
        ChunkCache cache = TerraformGenerator.getCache(this, x, z);
        BiomeBank cachedValue = cache.getBiome(x, z);
        if (cachedValue != null) return cachedValue;

        return cache.cacheBiome(x, z, BiomeBank.calculateBiome(this, x, y, z));
    }


    public String getName() {
        return worldName;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }
}
