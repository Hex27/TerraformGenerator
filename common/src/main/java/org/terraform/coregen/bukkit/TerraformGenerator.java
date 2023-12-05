package org.terraform.coregen.bukkit;

import com.google.common.cache.LoadingCache;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.cave.NoiseCaveRegistry;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.data.MegaChunk;
import org.terraform.data.MegaChunkKey;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.StructurePregenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class TerraformGenerator extends ChunkGenerator {
    public static final List<SimpleChunkLocation> preWorldInitGen = new ArrayList<>();

    public static LoadingCache<ChunkCache, ChunkCache> CHUNK_CACHE;
    public static int seaLevel = 62;

    public static void updateSeaLevelFromConfig() {
        seaLevel = TConfigOption.HEIGHT_MAP_SEA_LEVEL.getInt();
    }

    /**
     * Refers to raw X and raw Z (block coords). NOT chunk coords.
     */
    public static ChunkCache getCache(TerraformWorld tw, int x, int z) {
        ChunkCache cache = new ChunkCache(tw, x, 0, z);
		
//		return CHUNK_CACHE.compute(cache, (k, v) -> { if (v != null) return v;
//		cache.initInternalCache(); return cache; });
        try {
			return CHUNK_CACHE.get(cache);
		} catch (ExecutionException e) {
			e.printStackTrace();
			e.getCause().printStackTrace();
			cache.initInternalCache();
			return cache;
		}
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    public void generateNoise(TerraformWorld tw, int chunkX, int chunkZ, @NotNull ChunkData chunkData, @NotNull ChunkCache cache)
    {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double height = HeightMap.getPreciseHeight(tw, rawX, rawZ); //bank.getHandler().calculateHeight(tw, rawX, rawZ);
                cache.writeTransformedHeight(x,z, (short) height);

                BiomeBank bank = tw.getBiomeBank(rawX, (int)height, rawZ);//BiomeBank.calculateBiome(tw, rawX, height, rawZ);

                boolean mustUpdateHeight = true;
                //Fill stone up to the world height. Differentiate between deepslate or not.
                for(int y = (int) height; y >= TerraformGeneratorPlugin.injector.getMinY(); y--)
                {
                    Material stoneType = Material.STONE;
                    if(y < 0)
                        stoneType = OneOneSevenBlockHandler.DEEPSLATE;
                    else if(y <= 2)
                        stoneType = GenUtils.randMaterial(OneOneSevenBlockHandler.DEEPSLATE, Material.STONE);

                    //Set stone if a cave CANNOT be carved here
                    if(!tw.noiseCaveRegistry.canNoiseCarve(rawX,y,rawZ,height))
                    {
                        mustUpdateHeight = false;
                        chunkData.setBlock(x, y, z, stoneType);
                    }
                    else if(mustUpdateHeight) //if not, update transformed height
                        cache.writeTransformedHeight(x,z, (short) (y-1));

                }
            }
        }
    }

    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        TerraformGeneratorPlugin.watchdogSuppressant.tickWatchdog();

        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(),worldInfo.getSeed());
        ChunkCache cache = getCache(tw, chunkX*16,chunkZ*16);
        generateNoise(tw,chunkX,chunkZ,chunkData,cache);
    }

    /**
     * Responsible for setting surface biome blocks and biomeTransforms
     */
    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(),worldInfo.getSeed());
        ChunkCache cache = getCache(tw, chunkX*16,chunkZ*16);

        List<BiomeHandler> biomesToTransform = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;
                int height = cache.getTransformedHeight(x,z); //NOT HeightMap
                BiomeBank bank = tw.getBiomeBank(rawX, height, rawZ);
                int index = 0;
                Material[] crust = bank.getHandler().getSurfaceCrust(random);
                while (index < crust.length) {
                    chunkData.setBlock(x, height-index, z, crust[index]);
                    index++;
                }

                BiomeHandler transformHandler = bank.getHandler().getTransformHandler();
                if (transformHandler != null && !biomesToTransform.contains(transformHandler))
                    biomesToTransform.add(transformHandler);

                //Water for below certain heights
                for(int y = height+1; y <= seaLevel; y++)
                {
                    chunkData.setBlock(x,y,z,Material.WATER);
                }
            }
        }

        //Actually apply transformations. Keep track of height changes
        //All writes will update the cache accordingly.
        for (BiomeHandler handler : biomesToTransform) {
            handler.transformTerrain(cache, tw, random, chunkData, chunkX, chunkZ);
        }
    }

    public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

        int minY = TerraformGeneratorPlugin.injector.getMinY();
        for (int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                chunkData.setBlock(x, minY, z, Material.BEDROCK);

                //Up till y = minY+HEIGHT_MAP_BEDROCK_HEIGHT
                for(int i = 1; i < TConfigOption.HEIGHT_MAP_BEDROCK_HEIGHT.getInt(); i++) {
                    if(GenUtils.chance(random, TConfigOption.HEIGHT_MAP_BEDROCK_DENSITY.getInt(), 100))
                        chunkData.setBlock(x, minY+i, z, Material.BEDROCK);
                    else
                        break;
                }
            }
        }
    }

    public void generateCaves(@NotNull TerraformWorld tw, int chunkX, int chunkZ, @NotNull ChunkData chunkData, @NotNull ChunkCache cache) {
        for(int x = 0; x < 16; x++)
            for(int z = 0; z < 16; z++)
            {
                boolean mustUpdateHeight = true;
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;
                int height = HeightMap.getBlockHeight(tw,rawX,rawZ);
                for(int y = height; y > TerraformGeneratorPlugin.injector.getMinY(); y--)
                {
                    if(tw.noiseCaveRegistry.canGenerateCarve(rawX,y,rawZ,height))
                    {
                        chunkData.setBlock(x, y, z, Material.CAVE_AIR);
                        if(mustUpdateHeight)
                            cache.writeTransformedHeight (x,z, (short) (y-1));
                    }else mustUpdateHeight = false;
                }
            }
    }

    @Override
    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(),worldInfo.getSeed());
        ChunkCache cache = getCache(tw, chunkX*16,chunkZ*16);
        generateCaves(tw,chunkX,chunkZ,chunkData,cache);
        //Push from here, as the other one tries to use the method above
        StructurePregenerator.pushChunkCache(this, cache);
    }

    @Override
    public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        return new Location(world, 0, HeightMap.getBlockHeight(TerraformWorld.get(world), 0, 0), 0);
    }

    @Override
    public @NotNull List<BlockPopulator> getDefaultPopulators(World world) {
        TerraformWorld tw = TerraformWorld.get(world);
        return new ArrayList<>(){{
            add(new TerraformPopulator(tw));
            add(new TerraformBukkitBlockPopulator(tw));
        }};
    }

    //Do exactly 0 of this, TFG now handles ALL of it.
    public boolean shouldGenerateNoise() {
        return false;
    }

    public boolean shouldGenerateSurface() {
        return false;
    }

    public boolean shouldGenerateBedrock() {
        return false;
    }

    public boolean shouldGenerateCaves() {
        return false;
    }

    public boolean shouldGenerateDecorations() {
        return false;
    }

    public boolean shouldGenerateMobs() {
        return false;
    }

    public boolean shouldGenerateStructures() {
        return false;
    }
}
