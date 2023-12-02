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
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(),worldInfo.getSeed());

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                //tw.getBiomeBank(rawX, rawZ);
                double height = HeightMap.getPreciseHeight(tw, rawX, rawZ); //bank.getHandler().calculateHeight(tw, rawX, rawZ);

                BiomeBank bank = tw.getBiomeBank(rawX, (int)height, rawZ);//BiomeBank.calculateBiome(tw, rawX, height, rawZ);

                //Fill stone up to the world height. Differentiate between deepslate or not.
                for(int y = TerraformGeneratorPlugin.injector.getMinY(); y <= height; y++)
                {
                    Material stoneType = Material.STONE;
                    if(y < 0)
                        stoneType = OneOneSevenBlockHandler.DEEPSLATE;
                    else if(y <= 2)
                        stoneType = GenUtils.randMaterial(OneOneSevenBlockHandler.DEEPSLATE, Material.STONE);

                    //If a noise cave is to be carved here, then don't set stone.
                    if(!tw.noiseCaveRegistry.canCarve(rawX,y,rawZ,height))
                        chunkData.setBlock(x, y, z, stoneType);
                }

                //Water for below certain heights
                for(int y = (int)height+1; y <= seaLevel; y++)
                {
                    chunkData.setBlock(x,y,z,Material.WATER);
                }

            }
        }

    }

    /**
     * Responsible for setting surface biome blocks.
     */
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(),worldInfo.getSeed());

        List<BiomeHandler> biomesToTransform = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;
                int height = HeightMap.getBlockHeight(tw,rawX,rawZ);
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
            }
        }
        //TODO: Cache an updated value for caves/structures to operate on?
        //That or ignore this and just don't open caves on the surface on these.
        //This causes height cache to be anomalous for transformed biomes.
        //Do not rely on getHeight for them.
        for (BiomeHandler handler : biomesToTransform) {
            handler.transformTerrain(tw, random, chunkData, chunkX, chunkZ);
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

    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkData chunkData) {
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
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

    //This probably affects noise caves
    public boolean shouldGenerateNoise() {
        return false;
    }
    
    //No effect on plugin, this is overridden.
    public boolean shouldGenerateSurface() {
        return false;
    }

    //no effect on plugin, this is overridden.
    public boolean shouldGenerateBedrock() {
        return false;
    }

    //Affects the carver caves
    public boolean shouldGenerateCaves() {
        return false;
    }

    //No effect on plugin, this is overridden.
    public boolean shouldGenerateDecorations() {
        return false;
    }

    //No effect on plugin, this is overridden.
    public boolean shouldGenerateMobs() {
        return false;
    }

    //No effect on plugin, this is overridden.
    public boolean shouldGenerateStructures() {
        return false;
    }
}
