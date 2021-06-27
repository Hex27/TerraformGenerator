package org.terraform.coregen.bukkit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeSupportedBiomeGrid;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.ChunkCacheLoader;
import org.terraform.coregen.HeightMap;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class TerraformGenerator extends ChunkGenerator {
    public static final List<SimpleChunkLocation> preWorldInitGen = new ArrayList<>();

    private static final Object LOCK = new Object();

    private static final LoadingCache<ChunkCache, ChunkCache> CHUNK_CACHE = 
    		CacheBuilder.newBuilder()
    		.maximumSize(1000).build(new ChunkCacheLoader());//new LoadingCache<ChunkCache, ChunkCache>();
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

    private static boolean debugged = false;
    
    @SuppressWarnings("deprecation")
    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);
        TerraformWorld tw = TerraformWorld.get(world);
        
        //Patch for WorldInitEvent issues.
        if (!TerraformGeneratorPlugin.INJECTED_WORLDS.contains(world.getName())) {
            preWorldInitGen.add(new SimpleChunkLocation(world.getName(), chunkX, chunkZ));
        }

        List<BiomeHandler> biomesToTransform = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                //tw.getBiomeBank(rawX, rawZ);
                int height = (int) HeightMap.getBlockHeight(tw, rawX, rawZ); //bank.getHandler().calculateHeight(tw, rawX, rawZ);

                BiomeBank bank = BiomeBank.calculateBiome(tw, rawX, height, rawZ);
                
                Material[] crust = bank.getHandler().getSurfaceCrust(random);
                
                if(bank.getHandler().getCustomBiome() != CustomBiomeType.NONE && 
                		biome instanceof CustomBiomeSupportedBiomeGrid) {
                	if(!debugged) {
                		TerraformGeneratorPlugin.logger.info("[TerraformGenerator.class] Instance of CustomBiomeSupportedBiomeGrid! Setting biome...");
                		debugged = true;
                	}
                	((CustomBiomeSupportedBiomeGrid) biome).setBiome(
                			x, z, 
                			bank.getHandler().getCustomBiome(), 
                			bank.getHandler().getBiome());
                }
                else
                {
                	biome.setBiome(x, z, bank.getHandler().getBiome());
                }
                int undergroundHeight = height;
                int index = 0;
                while (index < crust.length) {
                    setBlockSync(chunk, x, undergroundHeight, z, crust[index]);
                    index++;
                    undergroundHeight--;
                }

                for (int y = undergroundHeight; y > 0; y--) {
                    setBlockSync(chunk, x, y, z, Material.STONE);
                }

                //Any low elevation is sea
                for (int y = height + 1; y <= seaLevel; y++) {
                    setBlockSync(chunk, x, y, z, Material.WATER);
                }

                //Bedrock Base
                setBlockSync(chunk, x, 2, z, GenUtils.randMaterial(random, Material.STONE, Material.BEDROCK));
                setBlockSync(chunk, x, 1, z, GenUtils.randMaterial(random, Material.STONE, Material.BEDROCK));
                setBlockSync(chunk, x, 0, z, Material.BEDROCK);

                BiomeHandler transformHandler = bank.getHandler().getTransformHandler();
                if (transformHandler != null && !biomesToTransform.contains(transformHandler))
                	biomesToTransform.add(transformHandler);
            }
        }

        for (BiomeHandler handler : biomesToTransform) {
            handler.transformTerrain(tw, random, chunk, biome, chunkX, chunkZ);
        }

        return chunk;
    }

    private void setBlockSync(ChunkData data, int x, int y, int z, Material material) {
        synchronized(LOCK) {
            data.setBlock(x, y, z, material);
        }
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
}
