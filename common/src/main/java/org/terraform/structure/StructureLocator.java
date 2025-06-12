package org.terraform.structure;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * This hasn't been migrated to the new cache system, because it's uniquely a pain in the
 * ass. It uses the cache with a variable timeout parameter, which is a huge pain.
 */
public class StructureLocator {

    private static final int[] TIMEDOUT = new int[] {-7, 13};
    private static final LoadingCache<StructureLocatorKey, int[]> STRUCTURELOCATION_CACHE = CacheBuilder.newBuilder()
                                                                                                        .maximumSize(300)
                                                                                                        .build(new StructureLocatorCacheLoader());

    public static int[] locateMultiMegaChunkStructure(TerraformWorld tw,
                                                      @NotNull MegaChunk center,
                                                      @NotNull MultiMegaChunkStructurePopulator populator,
                                                      int timeoutMillis)
    {

        if (!populator.isEnabled()) {
            return null;
        }
        StructureLocatorKey cacheKey = new StructureLocatorKey(center, tw, populator);

        // Do not use the cache if timeout is -1.
        // /terra locate should keep re-running this code for debug purposes.
        if (timeoutMillis != -1) {
            int[] coords = STRUCTURELOCATION_CACHE.getIfPresent(cacheKey);
            if (coords != null) {
                // Query timed out before. Don't try again.
                if (coords[0] == TIMEDOUT[0] && coords[1] == TIMEDOUT[1]) {
                    return null;
                }
                return coords;
            }
        }
        long currentTimeMillis = System.currentTimeMillis();

        int blockX = -1;
        int blockZ = -1;
        int radius = 0;
        boolean found = false;

        while (!found) {
            for (MegaChunk mc : getSurroundingChunks(center, radius)) {
                // Timeout catcher
                if (timeoutMillis != -1 && System.currentTimeMillis() - currentTimeMillis > timeoutMillis) {
                    STRUCTURELOCATION_CACHE.put(cacheKey, TIMEDOUT);
                    break;
                }
                for (int[] coords : populator.getCoordsFromMegaChunk(tw, mc)) {
                    if (coords == null) {
                        continue;
                    }

                    if (TConfig.areStructuresEnabled() && populator.canSpawn(
                            tw,
                            coords[0] >> 4,
                            coords[1] >> 4
                    ))
                    {
                        found = true;
                        blockX = coords[0];
                        blockZ = coords[1];
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            radius++;
        }
        STRUCTURELOCATION_CACHE.put(cacheKey, new int[] {blockX, blockZ});
        return new int[] {blockX, blockZ};
    }

    public static int[] locateSingleMegaChunkStructure(@NotNull TerraformWorld tw,
                                                       int rawX,
                                                       int rawZ,
                                                       @NotNull SingleMegaChunkStructurePopulator populator,
                                                       int timeoutMillis)
    {

        MegaChunk center = new MegaChunk(rawX, 0, rawZ);
        return locateSingleMegaChunkStructure(tw, center, populator, timeoutMillis);
    }

    public static int[] locateSingleMegaChunkStructure(@NotNull TerraformWorld tw,
                                                       @NotNull MegaChunk center,
                                                       @NotNull SingleMegaChunkStructurePopulator populator,
                                                       int timeoutMillis)
    {

        if (!populator.isEnabled()) {
            return null;
        }

        StructureLocatorKey cacheKey = new StructureLocatorKey(center, tw, populator);

        // Do not use the cache if timeout is -1.
        // /terra locate should keep re-running this code for debug purposes.
        // Also to prevent potential bugs where bad coordinates are cached.
        // Caching must be done anyway for vanilla, as it will cause locks when
        // players level up villagers.
        if (timeoutMillis != -1) {
            int[] coords = STRUCTURELOCATION_CACHE.getIfPresent(cacheKey);
            if (coords != null) {
                // Query timed out before. Don't try again.
                if (coords[0] == TIMEDOUT[0] && coords[1] == TIMEDOUT[1]) {
                    return null;
                }
                return coords;
            }
        }
        long currentTimeMillis = System.currentTimeMillis();
        MegaChunk lowerBound = null;
        MegaChunk upperBound = null;
        int blockX = -1;
        int blockZ = -1;
        int radius = 0;
        boolean found = false;

        while (!found) {
            for (MegaChunk mc : getSurroundingChunks(center, radius)) {
                // Timeout catcher
                if (timeoutMillis != -1 && System.currentTimeMillis() - currentTimeMillis > timeoutMillis) {
                    STRUCTURELOCATION_CACHE.put(cacheKey, TIMEDOUT);
                    break;
                }
                if (lowerBound == null) {
                    lowerBound = mc;
                }
                if (upperBound == null) {
                    upperBound = mc;
                }
                if (mc.getX() < lowerBound.getX() || mc.getZ() < lowerBound.getZ()) {
                    lowerBound = mc;
                }
                if (mc.getX() > upperBound.getX() || mc.getZ() > upperBound.getZ()) {
                    upperBound = mc;
                }
                int[] coords = mc.getCenterBiomeSectionBlockCoords(); // populator.getCoordsFromMegaChunk(tw, mc);
                if (coords == null) {
                    continue;
                }
                BiomeBank biome = mc.getCenterBiomeSection(tw).getBiomeBank();
                // Right bitshift of 4 is conversion from block coords to chunk coords.

                if (TConfig.areStructuresEnabled() && populator.canSpawn(
                        tw,
                        coords[0] >> 4,
                        coords[1] >> 4,
                        biome
                ))
                {
                    for (SingleMegaChunkStructurePopulator availablePops : StructureRegistry.getLargeStructureForMegaChunk(
                            tw,
                            mc
                    )) {
                        if (availablePops == null) {
                            continue;
                        }
                        if (TConfig.areStructuresEnabled() && availablePops.canSpawn(
                                tw,
                                coords[0] >> 4,
                                coords[1] >> 4,
                                biome
                        ))
                        {
                            if (availablePops.getClass().equals(populator.getClass())) {
                                // Can spawn
                                found = true;
                                blockX = coords[0];
                                blockZ = coords[1];
                            }

                            // Break either way, as the first structure that can spawn will spawn.
                            // If the populator could spawn, but it isn't the target populator
                            // we're looking for, then this megachunk didn't spawn our structure.
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
            radius++;

        }
        STRUCTURELOCATION_CACHE.put(cacheKey, new int[] {blockX, blockZ});
        return new int[] {blockX, blockZ};
    }

    private static @NotNull Collection<MegaChunk> getSurroundingChunks(@NotNull MegaChunk center, int radius) {
        if (radius == 0) {
            return List.of(center);
        }
        //     xxxxx
        // xxx  x   x
        // xox  x o x
        // xxx  x   x
        //     xxxxx
        ArrayList<MegaChunk> candidates = new ArrayList<>();
        // Lock rX, iterate rZ
        for (int rx : new int[] {-radius, radius}) {
            for (int rz = -radius; rz <= radius; rz++) {
                candidates.add(center.getRelative(rx, rz));
            }
        }

        // Lock rZ, iterate rX
        for (int rz : new int[] {-radius, radius}) {
            for (int rx = 1 - radius; rx <= radius - 1; rx++) {
                candidates.add(center.getRelative(rx, rz));
            }
        }

        return candidates;
    }

    //9/5/2025 this is fuckin stupid
    public static class StructureLocatorCacheLoader extends CacheLoader<StructureLocatorKey, int[]> {
        /**
         * Does not do loading.
         * If this is null, the caller is responsible for inserting it.
         */
        @Override
        public int @NotNull [] load(@NotNull StructureLocatorKey key) {
            return null;
        }
    }

    private static class StructureLocatorKey {
        private final MegaChunk mc;
        private final TerraformWorld tw;
        private final StructurePopulator pop;

        public StructureLocatorKey(MegaChunk mc, TerraformWorld tw, StructurePopulator pop) {
            super();
            this.mc = mc;
            this.tw = tw;
            this.pop = pop;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof StructureLocatorKey other) {
                if (other.mc.equals(mc) && other.tw.getName().equals(tw.getName())) {
                    return pop.getClass().isInstance(other.pop);
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mc, tw, pop.getClass());
        }
    }

}
