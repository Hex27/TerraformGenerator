package org.terraform.biome.cavepopulators;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.CoordPair;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.*;

/**
 * This class will distribute ALL cave post-population to the right populators,
 * as well as handle placements for special small clusters like dripzone, lush and
 * deep zones.
 */
public class MasterCavePopulatorDistributor {

    private static final HashSet<Class<?>> populatedBefore = new HashSet<>();

    public void populate(@NotNull TerraformWorld tw, @NotNull Random random, @NotNull PopulatorDataAbstract data, boolean generateClusters) {
        HashMap<CoordPair, CaveClusterRegistry> clusters = generateClusters ?
           calculateClusterLocations(
                random,
                tw,
                data.getChunkX(),
                data.getChunkZ()
           ) : new HashMap<>();
        ChunkCache cache = TerraformGenerator.getCache(tw, data.getChunkX(), data.getChunkZ());

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {

                BiomeBank bank = tw.getBiomeBank(x, z);
                int maxHeightForCaves = bank.getHandler().getMaxHeightForCaves(tw, x, z);

                // Remove clusters when they're spawned.
                CaveClusterRegistry reg = clusters.remove(new CoordPair(x, z));

                Collection<CoordPair> pairs = getCaveCeilFloors(data, x, z, 4, cache);

                // This is the index to spawn the cluster in.
                int clusterPair = !pairs.isEmpty() ? random.nextInt(pairs.size()) : 0;

                for (CoordPair pair : pairs) {

                    // Biome disallows caves above this height
                    if (pair.x() > maxHeightForCaves) {
                        continue;
                    }

                    SimpleBlock ceil = new SimpleBlock(data, x, pair.x(), z); // non-solid
                    SimpleBlock floor = new SimpleBlock(data, x, pair.z(), z); // solid

                    // If this is wet, don't touch it.
                    // Don't populate inside amethysts
                    if (BlockUtils.amethysts.contains(floor.getType())
                        || BlockUtils.fluids.contains(floor.getUp()
                                                           .getType())
                        || BlockUtils.amethysts.contains(ceil.getDown().getType()))
                    {
                        continue;
                    }

                    AbstractCavePopulator pop;

                    /*
                     * Deep cave floors will use the deep cave populator.
                     * This has to happen, as most surfaces
                     * too low down will be lava. Hard to decorate.
                     */
                    if (floor.getY() < TerraformGeneratorPlugin.injector.getMinY() + 32) {
                        pop = new DeepCavePopulator();
                    }
                    else {
                        /*
                         * Cluster Populators won't just decorate one block, they
                         * will populate the surrounding surfaces in a fuzzy
                         * radius.
                         */
                        // If there is no cluster to spawn, then revert to the
                        // basic biome-based cave populator
                        pop = (clusterPair == 0 && reg != null) ? reg.getPopulator(random) : bank.getCavePop();
                    }
                    clusterPair--;

                    pop.populate(tw, random, ceil, floor);

                    // Locating and debug print
                    if (populatedBefore.add(pop.getClass())) {
                        TerraformGeneratorPlugin.logger.info("Spawning "
                                                             + pop.getClass().getSimpleName()
                                                             + " at "
                                                             + floor);
                    }
                }
            }
        }
    }

    private @NotNull HashMap<CoordPair, CaveClusterRegistry> calculateClusterLocations(@NotNull Random rand,
                                                                                       @NotNull TerraformWorld tw,
                                                                                       int chunkX,
                                                                                       int chunkZ)
    {
        HashMap<CoordPair, CaveClusterRegistry> locs = new HashMap<>();
        //Don't waste compute if caves don't exist
        if(!TConfig.areCavesEnabled()) return locs;

        for (CaveClusterRegistry type : CaveClusterRegistry.values()) {
            CoordPair[] positions = GenUtils.vectorRandomObjectPositions(tw.getHashedRand(
                            chunkX,
                            type.getHashSeed(),
                            chunkZ
                    ).nextInt(9999999),
                    chunkX,
                    chunkZ,
                    type.getSeparation(),
                    type.getPertub()
            );
            for (CoordPair pos : positions) {
                if (locs.containsKey(pos))
                // give a chance to replace the old one
                {
                    if (rand.nextBoolean()) {
                        continue;
                    }
                }

                locs.put(pos, type);
            }

        }

        return locs;
    }

    /**
     * Uses the ChunkCache's optimised boolean holder
     */
    public static @NotNull Collection<CoordPair> getCaveCeilFloors(PopulatorDataAbstract data,
                                                                   int x,
                                                                   int z,
                                                                   int minimumHeight,
                                                                   ChunkCache cache)
    {
        int y = cache.getTransformedHeight(x&0xF, z&0xF);
        final int INVAL = TerraformGeneratorPlugin.injector.getMinY() - 1;
        int[] pair = {INVAL, INVAL};
        List<CoordPair> list = new ArrayList<>();
        // Subtract one as the first cave floor cannot be the surface
        for (int ny = y - 1; ny > TerraformGeneratorPlugin.injector.getMinY(); ny--) {
            //Material type = data.getType(x, ny, z); //maybe the rocks are the friends we make along the way
            if (cache.isSolid(x&0xf,ny,z&0xf)) {
                pair[1] = ny;
                if (pair[0] - pair[1] >= minimumHeight) {
                    list.add(new CoordPair(pair[0],pair[1]));
                }
                pair[0] = INVAL;
                pair[1] = INVAL;
            }
            else if (pair[0] == INVAL) {
                pair[0] = ny;
            }
        }

        return list;
    }
}
