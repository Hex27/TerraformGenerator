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
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.data.DudChunkData;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;

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
		//Note how it DOES NOT initInternalCache here
        //Cos this is the damn key
        //Don't fucking run calculations here
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
        TerraformGeneratorPlugin.watchdogSuppressant.tickWatchdog();

        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(),worldInfo.getSeed());
        ChunkCache cache = getCache(tw, chunkX*16,chunkZ*16);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double height = HeightMap.getPreciseHeight(tw, rawX, rawZ); //bank.getHandler().calculateHeight(tw, rawX, rawZ);
                cache.writeTransformedHeight(x,z, (short) height);

                //Fill stone up to the world height. Differentiate between deepslate or not.
                for(int y = (int) height; y >= TerraformGeneratorPlugin.injector.getMinY(); y--)
                {
                    Material stoneType = Material.STONE;
                    if(y < 0)
                        stoneType = Material.DEEPSLATE;
                    else if(y <= 2)
                        stoneType = GenUtils.randMaterial(Material.DEEPSLATE, Material.STONE);

                    //Set stone if a cave CANNOT be carved here
                    if(!tw.noiseCaveRegistry.canNoiseCarve(rawX,y,rawZ,height))
                        chunkData.setBlock(x, y, z, stoneType);

                }
            }
        }

    }

    /**
     * Responsible for setting surface biome blocks and biomeTransforms
     */
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random dontCareRandom, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(),worldInfo.getSeed());
        ChunkCache cache = getCache(tw, chunkX*16,chunkZ*16);

        //For transformation ONLY
        Random transformRandom = tw.getHashedRand(chunkX, chunkZ, 31278);

        for (int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;
                int height = cache.getTransformedHeight(x,z); //NOT HeightMap
                BiomeBank bank = tw.getBiomeBank(rawX, height, rawZ);
                int index = 0;
                Material[] crust = bank.getHandler().getSurfaceCrust(dontCareRandom);
                while (index < crust.length) {
                    chunkData.setBlock(x, height-index, z, crust[index]);
                    index++;
                }
                //Water for below certain heights
                for(int y = height+1; y <= seaLevel; y++)
                {
                    chunkData.setBlock(x,y,z,Material.WATER);
                }

                //Carve caves HERE.
                boolean mustUpdateHeight = true;
                for(int y = height; y > TerraformGeneratorPlugin.injector.getMinY(); y--)
                {
                    if(tw.noiseCaveRegistry.canGenerateCarve(rawX,y,rawZ,height)
                            || !chunkData.getType(x,y,z).isSolid())
                    {
                        chunkData.setBlock(x, y, z, Material.CAVE_AIR);
                        if(mustUpdateHeight)
                            cache.writeTransformedHeight (x,z, (short) (y-1));
                    }else mustUpdateHeight = false;
                }

                //Transform height AFTER sea level is written.
                //Transformed below-sea areas are not supposed to be water.
                BiomeHandler transformHandler = bank.getHandler().getTransformHandler();
                if(transformHandler != null)
                    transformHandler.transformTerrain(cache, tw, transformRandom, chunkData, x, z,chunkX, chunkZ);

                //After this whole song and dance, place bedrock
                chunkData.setBlock(x, TerraformGeneratorPlugin.injector.getMinY(), z, Material.BEDROCK);

                //Up till y = minY+HEIGHT_MAP_BEDROCK_HEIGHT
                for(int i = 1; i < TConfigOption.HEIGHT_MAP_BEDROCK_HEIGHT.getInt(); i++) {
                    if(GenUtils.chance(dontCareRandom, TConfigOption.HEIGHT_MAP_BEDROCK_DENSITY.getInt(), 100))
                        chunkData.setBlock(x, TerraformGeneratorPlugin.injector.getMinY()+i, z, Material.BEDROCK);
                    else
                        break;
                }

                //END ONE COLUMN
            }
        }
    }

    public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

    }

    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

    }

    //Explode if a read is attempted. Transform Handlers are not supposed to read.
    private static final DudChunkData DUD = new DudChunkData();

    //This method ONLY fills transformedHeight with meaningful values,
    // and writes nothing.
    public static void buildFilledCache(TerraformWorld tw, int chunkX, int chunkZ, ChunkCache cache){
        //TerraformGeneratorPlugin.watchdogSuppressant.tickWatchdog(); don't unnecessarily tick this shit

        //Ensure that this shit is the same as the one in generateSurface
        Random random = tw.getHashedRand(chunkX, chunkZ, 31278);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ); //bank.getHandler().calculateHeight(tw, rawX, rawZ);
                cache.writeTransformedHeight(x,z, (short) preciseHeight);

                //Carve caves
                for(int y = (int) preciseHeight; y >= TerraformGeneratorPlugin.injector.getMinY(); y--)
                    //Set stone if a cave CANNOT be carved here
                    //Check canNoiseCarve because carver caves may expose
                    //noise caves below, which contribute to height changes
                    if(tw.noiseCaveRegistry.canGenerateCarve(rawX,y,rawZ,preciseHeight)
                       || tw.noiseCaveRegistry.canNoiseCarve(rawX,y,rawZ,preciseHeight))
                        cache.writeTransformedHeight(x,z, (short) (y-1));
                    else break;

                //Apply biome transforms to get real height
                BiomeBank bank = tw.getBiomeBank(rawX, (int) preciseHeight,rawZ);
                BiomeHandler transformHandler = bank.getHandler().getTransformHandler();

                if(transformHandler != null)
                    transformHandler.transformTerrain(cache, tw, random, DUD, x, z,chunkX, chunkZ);
            }
        }
    }

    @Override
    public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        return new Location(world, 0, HeightMap.getBlockHeight(TerraformWorld.get(world), 0, 0), 0);
    }

    @Override
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
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
