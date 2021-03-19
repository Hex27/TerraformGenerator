package org.terraform.data;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class TerraformWorld {
    public static final HashMap<String, TerraformWorld> WORLDS = new HashMap<>();
    private final String worldName;
    private final long seed;

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
     * @param x
     * @param z
     * @return
     */
    public BiomeBank getBiomeBank(int x, int z) {
    	int y = HeightMap.getBlockHeight(this, x, z);
    	
        ChunkCache cache = TerraformGenerator.getCache(this, x, z);
        BiomeBank cachedValue = cache.getBiome(x, z);
        if (cachedValue != null) return cachedValue;

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
