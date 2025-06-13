package org.terraform.data;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.cave.NoiseCaveRegistry;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformBukkitBlockPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class TerraformWorld {
    private static final ConcurrentHashMap<String, TerraformWorld> WORLDS = new ConcurrentHashMap<>();
    public final @NotNull NoiseCaveRegistry noiseCaveRegistry;
    private final String worldName;
    private final long seed;
    private final @NotNull TerraformBukkitBlockPopulator bukkitBlockPopulator;
    public int minY = 0;
    public int maxY = 256;

    public TerraformWorld(String name, long seed) {
        TerraformGeneratorPlugin.logger.info("Creating TW instance: " + name + " - " + seed);
        this.worldName = name;
        this.seed = seed;
        this.bukkitBlockPopulator = new TerraformBukkitBlockPopulator(this);
        this.noiseCaveRegistry = new NoiseCaveRegistry(this);
    }

    private TerraformWorld(@NotNull World world) {
        TerraformGeneratorPlugin.logger.info("Creating TW instance: " + world.getName() + " - " + world.getSeed());
        this.worldName = world.getName();
        this.seed = world.getSeed();
        this.bukkitBlockPopulator = new TerraformBukkitBlockPopulator(this);
        this.noiseCaveRegistry = new NoiseCaveRegistry(this);
    }

    /**
     * For multiverse. Ignores the cache entry.
     */
    public static @NotNull TerraformWorld forceOverrideSeed(@NotNull World world)
    {
        TerraformWorld tw = new TerraformWorld(world);
        WORLDS.put(world.getName(), tw);
        return tw;
    }

    public static @NotNull TerraformWorld get(@NotNull World world) {
        return WORLDS.computeIfAbsent(world.getName(), (k) -> new TerraformWorld(world));
    }

    public static @NotNull TerraformWorld get(String name, long seed) {
        return WORLDS.computeIfAbsent(name, (k) -> new TerraformWorld(name, seed));
    }

    public @NotNull FastNoise getTemperatureOctave() {

        return NoiseCacheHandler.getNoise(this, NoiseCacheEntry.TW_TEMPERATURE, tw -> {
            FastNoise n = new FastNoise((int) (tw.getSeed() * 2));
            n.SetNoiseType(NoiseType.Simplex);
            n.SetFrequency(TConfig.c.BIOME_TEMPERATURE_FREQUENCY); // Default 0.03f
            return n;
        });
    }

    public @NotNull FastNoise getMoistureOctave() {
        return NoiseCacheHandler.getNoise(this, NoiseCacheEntry.TW_MOISTURE, tw -> {
            FastNoise n = new FastNoise((int) (tw.getSeed() / 4));
            n.SetNoiseType(NoiseType.Simplex);
            n.SetFrequency(TConfig.c.BIOME_MOISTURE_FREQUENCY); // Default 0.03f
            return n;
        });
    }

    public @NotNull FastNoise getOceanicNoise() {
        return NoiseCacheHandler.getNoise(this, NoiseCacheEntry.TW_OCEANIC, tw -> {
            FastNoise n = new FastNoise((int) tw.getSeed() * 12);
            n.SetNoiseType(NoiseType.Simplex);
            n.SetFrequency(TConfig.c.BIOME_OCEANIC_FREQUENCY);
            return n;
        });
    }

    public @NotNull FastNoise getMountainousNoise() {
        return NoiseCacheHandler.getNoise(this, NoiseCacheEntry.TW_MOUNTAINOUS, tw -> {
            FastNoise n = new FastNoise((int) tw.getSeed() * 73);
            n.SetNoiseType(NoiseType.Simplex);
            n.SetFrequency((float)TConfig.c.BIOME_MOUNTAINOUS_FREQUENCY);
            return n;
        });
    }

    public long getSeed() {
        return seed;
    }

    public @NotNull Random getRand(long d) {
        return new Random(seed / 4 + 25981 * d);
    }

    public @NotNull Random getHashedRand(long a, int b, int c) {
        return new Random(11 * a + Objects.hash(seed, 127 * b, 773 * c));
    }

    public @NotNull Random getHashedRand(int x, int y, int z, long multiplier) {
        return new Random(Objects.hash(seed, 11 * x, 127 * y, 773 * z) * multiplier);
    }

    /**
     * Same as getBiomeBank(x,y,z), except y is autofilled to be HeightMap.getBlockHeight
     *
     * @param x blockX
     * @param z blockZ
     */
    public BiomeBank getBiomeBank(int x, int z) {
        ChunkCache cache = TerraformGenerator.getCache(this, x>>4, z>>4);
        BiomeBank cachedValue = cache.getBiome(x, z);
        if (!BiomeBank.debugPrint && cachedValue != null) {
            return cachedValue;
        }

        int y = HeightMap.getBlockHeight(this, x, z);

        return cache.cacheBiome(x, z, BiomeBank.calculateBiome(this, x, y, z));
    }

    public BiomeBank getBiomeBank(int x, int y, int z) {
        ChunkCache cache = TerraformGenerator.getCache(this, x>>4, z>>4);
        BiomeBank cachedValue = cache.getBiome(x, z);
        if (cachedValue != null) {
            return cachedValue;
        }

        return cache.cacheBiome(x, z, BiomeBank.calculateBiome(this, x, y, z));
    }


    public String getName() {
        return worldName;
    }

    public @NotNull World getWorld() {
        return Objects.requireNonNull(Bukkit.getWorld(worldName));
    }

    public @NotNull TerraformBukkitBlockPopulator getBukkitBlockPopulator() {
        return bukkitBlockPopulator;
    }
}
