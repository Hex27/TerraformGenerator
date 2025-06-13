package org.terraform.coregen.bukkit;

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
import org.terraform.data.TWCoordPair;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.CommonMat;
import org.terraform.utils.datastructs.ConcurrentLRUCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TerraformGenerator extends ChunkGenerator {
    public static final List<SimpleChunkLocation> preWorldInitGen = new ArrayList<>();
    // Explode if a read is attempted. Transform Handlers are not supposed to read.
    private static final DudChunkData DUD = new DudChunkData();
    //This cache is NOT fucking used correctly.
    // By right, nobody's supposed to be writing to it at the same time, but in
    // practice, that doesn't matter
    public static ConcurrentLRUCache<TWCoordPair, ChunkCache> CHUNK_CACHE;
    public static int seaLevel = 62;

    public static void updateSeaLevelFromConfig() {
        seaLevel = TConfig.c.HEIGHT_MAP_SEA_LEVEL;
    }

    /**
     * @param x chunk X
     * @param z chunk Z
     */
    public static @NotNull ChunkCache getCache(TerraformWorld tw, int x, int z) {
        // Note how it DOES NOT initInternalCache here
        // Cos this is the damn key
        // Don't fucking run calculations here
        return CHUNK_CACHE.get(new TWCoordPair(tw, x,z));
    }

    // This method ONLY fills transformedHeight with meaningful values,
    // and writes nothing.
    public static void buildFilledCache(@NotNull TerraformWorld tw, int chunkX, int chunkZ, @NotNull ChunkCache cache) {
        // TerraformGeneratorPlugin.watchdogSuppressant.tickWatchdog(); don't unnecessarily tick this shit

        // Ensure that this shit is the same as the one in generateSurface
        Random random = tw.getHashedRand(chunkX, chunkZ, 31278);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double preciseHeight = HeightMap.getPreciseHeight(
                        tw,
                        rawX,
                        rawZ
                ); // bank.getHandler().calculateHeight(tw, rawX, rawZ);
                cache.writeTransformedHeight(x, z, (short) preciseHeight);

                // Carve caves
                for (int y = (int) preciseHeight; y >= TerraformGeneratorPlugin.injector.getMinY(); y--)
                // Set stone if a cave CANNOT be carved here
                // Check canNoiseCarve because carver caves may expose
                // noise caves below, which contribute to height changes
                {
                    if (tw.noiseCaveRegistry.canGenerateCarve(rawX, y, rawZ, preciseHeight, cache)
                        || tw.noiseCaveRegistry.canNoiseCarve(rawX, y, rawZ, preciseHeight, cache))
                    {
                        cache.writeTransformedHeight(x, z, (short) (y - 1));
                    }
                    else {
                        break;
                    }
                }

                // Apply biome transforms to get real height
                BiomeBank bank = tw.getBiomeBank(rawX, (int) preciseHeight, rawZ);
                BiomeHandler transformHandler = bank.getHandler().getTransformHandler();

                if (transformHandler != null) {
                    transformHandler.transformTerrain(cache, tw, random, DUD, x, z, chunkX, chunkZ);
                }
            }
        }
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

    /**
     * EVERY STAGE IS DONE INSIDE HERE FOR A REASON.
     * The cache MAY invalidate between stages, making it infeasible to even
     * bother splitting it up.
     * <br>
     * It was originally split into 4 phases for readability's sake,
     * but there's really no point - its faster to iterate x/z ONCE here,
     * and avoid all the cache and other nonsense issues.
     * <br>
     * It's that or throw ChunkCaches into ConcurrentHashMaps and
     * then flush it into the CHUNK_CACHE after generation, which is
     * dumb. That's dumb.
     */
    public void generateNoise(@NotNull WorldInfo worldInfo,
                              @NotNull Random dontCareRandom,
                              int chunkX,
                              int chunkZ,
                              @NotNull ChunkData chunkData)
    {
        TerraformGeneratorPlugin.watchdogSuppressant.tickWatchdog();

        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(), worldInfo.getSeed());
        ChunkCache cache = getCache(tw, chunkX, chunkZ);

        // For transformation ONLY
        Random transformRandom = tw.getHashedRand(chunkX, chunkZ, 31278);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = (chunkX<<4) + x;
                int rawZ = (chunkZ<<4) + z;

                double height = HeightMap.getPreciseHeight(
                        tw,
                        rawX,
                        rawZ
                ); // bank.getHandler().calculateHeight(tw, rawX, rawZ);
                cache.writeTransformedHeight(x, z, (short) height);

                // Fill stone up to the world height. Differentiate between deepslate or not.
                chunkData.setRegion(x,3,z,x+1, (int) height+1,z+1, CommonMat.STONE);
                chunkData.setRegion(x,TerraformGeneratorPlugin.injector.getMinY(),z,
                        x+1, 0,z+1, CommonMat.DEEPSLATE);

                //Iterate the remaining area to carve out caves
                for (int y = (int) height; y >= TerraformGeneratorPlugin.injector.getMinY(); y--) {
                   if (y >= 0 && y <= 2) {
                       chunkData.setBlock(x, y, z, GenUtils.randChoice(
                               dontCareRandom,CommonMat.DEEPSLATE, CommonMat.STONE));
                    }

                    // Set cave air if a cave CAN be carved here
                    if (tw.noiseCaveRegistry.canNoiseCarve(rawX, y, rawZ, height, cache)) {
                        chunkData.setBlock(x, y, z, CommonMat.CAVE_AIR);
                        cache.cacheNonSolid(x,y,z);
                    }
                    else cache.cacheSolid(x,y,z);

                }

                // PERFORM SURFACE AND CAVE CARVING
                BiomeBank bank = tw.getBiomeBank(rawX, (int) height, rawZ);
                int index = 0;
                Material[] crust = bank.getHandler().getSurfaceCrust(dontCareRandom);
                while (index < crust.length) {
                    chunkData.setBlock(x, (int) (height - index), z, crust[index]);
                    index++;
                }
                // Water for below certain heights
                chunkData.setRegion(x, (int) (height + 1),z,x+1,seaLevel+1,z+1, CommonMat.WATER);

                // Carve caves HERE.
                boolean mustUpdateHeight = true;
                for (int y = (int) height; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
                    if (tw.noiseCaveRegistry.canGenerateCarve(rawX, y, rawZ, height, cache)
                        || !chunkData.getType(x, y, z).isSolid())
                    {
                        chunkData.setBlock(x, y, z, CommonMat.CAVE_AIR);
                        cache.cacheNonSolid(x,y,z);
                        if (mustUpdateHeight) {
                            cache.writeTransformedHeight(x, z, (short) (y - 1));
                        }
                    }
                    else {
                        mustUpdateHeight = false;
                    }
                }

                // Transform height AFTER sea level is written.
                // Transformed below-sea areas are not supposed to be water.
                BiomeHandler transformHandler = bank.getHandler().getTransformHandler();
                if (transformHandler != null) {
                    transformHandler.transformTerrain(cache, tw, transformRandom, chunkData, x, z, chunkX, chunkZ);
                }

                // Up till y = minY+HEIGHT_MAP_BEDROCK_HEIGHT
                for (int i = 1; i < TConfig.c.HEIGHT_MAP_BEDROCK_HEIGHT; i++) {
                    if (GenUtils.chance(dontCareRandom, TConfig.c.HEIGHT_MAP_BEDROCK_DENSITY, 100)) {
                        chunkData.setBlock(x, TerraformGeneratorPlugin.injector.getMinY() + i, z, CommonMat.BEDROCK);
                    }
                    else
                        break;
                }
            }
        }
        // After this whole song and dance, place bedrock in one operation
        chunkData.setRegion(0,TerraformGeneratorPlugin.injector.getMinY(), 0,
                16,TerraformGeneratorPlugin.injector.getMinY()+1, 16, CommonMat.BEDROCK);
    }

    /**
     * Responsible for setting surface biome blocks and biomeTransforms
     */
    public void generateSurface(@NotNull WorldInfo worldInfo,
                                @NotNull Random dontCareRandom,
                                int chunkX,
                                int chunkZ,
                                @NotNull ChunkData chunkData)
    {

    }

    public void generateBedrock(@NotNull WorldInfo worldInfo,
                                @NotNull Random random,
                                int chunkX,
                                int chunkZ,
                                @NotNull ChunkData chunkData)
    {

    }

    public void generateCaves(@NotNull WorldInfo worldInfo,
                              @NotNull Random random,
                              int chunkX,
                              int chunkZ,
                              @NotNull ChunkData chunkData)
    {

    }

    @Override
    public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        return new Location(world, 0, HeightMap.getBlockHeight(TerraformWorld.get(world), 0, 0), 0);
    }

    @Override
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        TerraformWorld tw = TerraformWorld.get(world);
        return List.of(new TerraformPopulator(),new TerraformBukkitBlockPopulator(tw));
    }

    // Do exactly 0 of this, TFG now handles ALL of it.
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

    // This is true as StructureManager is now being overridden.
    public boolean shouldGenerateStructures() {
        return true;
    }
}
