package org.terraform.data;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.terraform.biome.BiomeBank;
import org.terraform.cave.NoiseCaveRegistry;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformBukkitBlockPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class TerraformWorld {
    public static final HashMap<String, TerraformWorld> WORLDS = new HashMap<>();
    private final String worldName;
    private final long seed;
    public int minY = 0;
    public int maxY = 256;
    private final TerraformBukkitBlockPopulator bukkitBlockPopulator;

    public final NoiseCaveRegistry noiseCaveRegistry;
    private TerraformWorld(String name, long seed) {
        this.worldName = name;
        this.seed = seed;
        this.bukkitBlockPopulator = new TerraformBukkitBlockPopulator(this);
        this.noiseCaveRegistry = new NoiseCaveRegistry(this);
    }

    private TerraformWorld(World world) {
        this.worldName = world.getName();
        this.seed = world.getSeed();
        this.bukkitBlockPopulator = new TerraformBukkitBlockPopulator(this);
        this.noiseCaveRegistry = new NoiseCaveRegistry(this);
    }

    public static TerraformWorld get(World world) {
        return WORLDS.computeIfAbsent(world.getName(), (k) -> new TerraformWorld(world));
    }

    public static TerraformWorld get(String name, long seed) {
        return WORLDS.computeIfAbsent(name, (k) -> new TerraformWorld(name, seed));
    }

    public FastNoise getTemperatureOctave() {

        return NoiseCacheHandler.getNoise(
        		this, 
        		NoiseCacheEntry.TW_TEMPERATURE, 
        		tw -> {
                    FastNoise n = new FastNoise((int) (tw.getSeed() * 2));
                    n.SetNoiseType(NoiseType.Simplex);
                    n.SetFrequency(TConfigOption.BIOME_TEMPERATURE_FREQUENCY.getFloat()); //Default 0.03f
        	        return n;
        		});
    }

    public FastNoise getMoistureOctave() {
        return NoiseCacheHandler.getNoise(
        		this, 
        		NoiseCacheEntry.TW_MOISTURE, 
        		tw -> {
                    FastNoise n = new FastNoise((int) (tw.getSeed()/4));
                    n.SetNoiseType(NoiseType.Simplex);
                    n.SetFrequency(TConfigOption.BIOME_MOISTURE_FREQUENCY.getFloat()); //Default 0.03f
        	        return n;
        		});
    }

    public FastNoise getOceanicNoise() {
        return NoiseCacheHandler.getNoise(
        		this, 
        		NoiseCacheEntry.TW_OCEANIC, 
        		tw -> {
                	FastNoise n = new FastNoise((int) tw.getSeed() * 12);
                	n.SetNoiseType(NoiseType.Simplex);
                	n.SetFrequency(TConfigOption.BIOME_OCEANIC_FREQUENCY.getFloat());
        	        return n;
        		});
    }

    public FastNoise getMountainousNoise() {
        return NoiseCacheHandler.getNoise(
        		this, 
        		NoiseCacheEntry.TW_MOUNTAINOUS, 
        		tw -> {
                	FastNoise n = new FastNoise((int) tw.getSeed() * 73);
                	n.SetNoiseType(NoiseType.Simplex);
                	n.SetFrequency(TConfigOption.BIOME_MOUNTAINOUS_FREQUENCY.getFloat());
        	        return n;
        		});
    }
    
    public long getSeed() {
        return seed;
    }

    public Random getRand(long d) {
        return new Random(seed/4 + 25981*d);
    }

    public Random getHashedRand(long x, int y, int z) {
        return new Random(11*x + Objects.hash(seed, 127*y, 773*z));
    }

    public Random getHashedRand(int x, int y, int z, long multiplier) {
        return new Random(Objects.hash(seed, 11*x, 127*y, 773*z) * multiplier);
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
        if (!BiomeBank.debugPrint && cachedValue != null) return cachedValue;
        
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

	public TerraformBukkitBlockPopulator getBukkitBlockPopulator() {
		return bukkitBlockPopulator;
	}
}
