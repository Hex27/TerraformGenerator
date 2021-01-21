package org.terraform.coregen.bukkit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.ChunkCacheLoader;
import org.terraform.coregen.HeightMap;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class TerraformGenerator extends ChunkGenerator {
    public static final List<SimpleChunkLocation> preWorldInitGen = new ArrayList<>();
    private static final LoadingCache<ChunkCache, ChunkCache> CHUNK_CACHE = 
    		CacheBuilder.newBuilder()
    		.maximumSize(1000).build(new ChunkCacheLoader());//new LoadingCache<ChunkCache, ChunkCache>();
    public static int seaLevel = 62;
    public static int minMountainLevel = 85;
    
    public static void updateSeaLevelFromConfig() {
        seaLevel = TConfigOption.HEIGHT_MAP_SEA_LEVEL.getInt();
    }

    public static void updateMinMountainLevelFromConfig() {
        minMountainLevel = TConfigOption.BIOME_MOUNTAIN_HEIGHT.getInt();
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

    @SuppressWarnings("deprecation")
    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);
        TerraformWorld tw = TerraformWorld.get(world);
//        ChunkCache cache = new ChunkCache(tw, chunkX, chunkZ);
//        CHUNK_CACHE.put(cache, cache);
        //putToCache(cache);

        //Bukkit.getLogger().info("Attempting gen: " + chunkX + "," + chunkZ);

        //Patch for WorldInitEvent issues.
        if (!TerraformGeneratorPlugin.INJECTED_WORLDS.contains(world.getName())) {
            preWorldInitGen.add(new SimpleChunkLocation(world.getName(), chunkX, chunkZ));
        }

        List<BiomeHandler> biomesToTransform = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                // This will also cache the height
                int height = HeightMap.getBlockHeight(tw, rawX, rawZ);

                BiomeBank bank = tw.getBiomeBank(rawX, height, rawZ);
                Material[] crust = bank.getHandler().getSurfaceCrust(random);
                biome.setBiome(x, z, bank.getHandler().getBiome());
                int undergroundHeight = height;
                int index = 0;
                while (index < crust.length) {
                    chunk.setBlock(x, undergroundHeight, z, crust[index]);
                    index++;
                    undergroundHeight--;
                }

                for (int y = undergroundHeight; y > 0; y--) {
                    chunk.setBlock(x, y, z, Material.STONE);
                }

                //Any low elevation is sea
                for (int y = height + 1; y <= seaLevel; y++) {
                    chunk.setBlock(x, y, z, Material.WATER);
                }

                //Bedrock Base
                chunk.setBlock(x, 2, z, GenUtils.randMaterial(random, Material.STONE, Material.BEDROCK));
                chunk.setBlock(x, 1, z, GenUtils.randMaterial(random, Material.STONE, Material.BEDROCK));
                chunk.setBlock(x, 0, z, Material.BEDROCK);

                BiomeHandler transformHandler = bank.getHandler().getTransformHandler();
                if (transformHandler != null && !biomesToTransform.contains(transformHandler))
                	biomesToTransform.add(transformHandler);
            }
        }

        for (BiomeHandler handler : biomesToTransform) {
            handler.transformTerrain(tw, random, chunk, chunkX, chunkZ);
        }

        return chunk;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, HeightMap.getBlockHeight(TerraformWorld.get(world), 0, 0), 0);
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        TerraformWorld tw = TerraformWorld.get(world);
        return Collections.singletonList(new TerraformBukkitBlockPopulator(tw));
    }


//    private static boolean verifyCache(TerraformWorld tw, int x, int z, ChunkCache cache) {
//    	if(x >> 4 != cache.chunkX || z >> 4 != cache.chunkZ) {
//    	//if(x-cache.chunkX*16 > 15 || x-cache.chunkX*16 < 0 || z-cache.chunkZ*16 > 15 || z-cache.chunkZ*16 < 0) {
//    		int hash = ChunkCache.calculateHash(ChunkCache.getChunkCoordinate(x), ChunkCache.getChunkCoordinate(z),cache.tw);
//    		TerraformGeneratorPlugin.logger.info("BAD REQUEST DETECTED: " + x +","+z+":" + cache.tw.hashCode() + "," + cache.chunkX + "," + cache.chunkZ);
//    		TerraformGeneratorPlugin.logger.info("BITSHIFTS: " + (x>>4) + "," + (z>>4) + ":" + tw.hashCode() + "," + ChunkCache.getChunkCoordinate(x) + "," + ChunkCache
//    		.getChunkCoordinate(z));
//    		TerraformGeneratorPlugin.logger.info("HASHES: " + cache.getHash() + " - " + hash);
//    		//dumpCache();
//    		int throwMotherfucker = 5/0;
//    		return false;
//    	}
//    	return true;
//    }
//just shoot me already
//    public static void dumpCache() {
//    	TerraformGeneratorPlugin.logger.info("==================================================");
//    	for(Entry<Integer,ChunkCache> entry:chunkCaches.entrySet()) {
//    		TerraformGeneratorPlugin.logger.info(
//    				entry.getKey() + ":" + 
//    		entry.getValue().tw.getSeed() + "," + entry.getValue().chunkX + "," + entry.getValue().chunkZ
//    		+ " >>>> " + entry.getValue().getHash());
//    	}
//    }
}
