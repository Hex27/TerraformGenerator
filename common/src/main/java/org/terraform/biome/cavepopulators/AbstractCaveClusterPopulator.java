package org.terraform.biome.cavepopulators;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.*;

public abstract class AbstractCaveClusterPopulator extends AbstractCavePopulator {

    private final float radius;
    // Starts null, but will be populated by the time oneUnit is called.
    protected SimpleBlock center;
    protected SimpleBlock lowestYCenter;

    public AbstractCaveClusterPopulator(float radius) {
        this.radius = radius;
    }

    protected abstract void oneUnit(TerraformWorld tw,
                                    Random random,
                                    SimpleBlock ceil,
                                    SimpleBlock floor,
                                    boolean isBoundary);

    @Override
    public void populate(TerraformWorld tw, Random random, @NotNull SimpleBlock ceil, @NotNull SimpleBlock floor) {
        if (this.radius <= 0) {
            return;
        }
        ArrayList<SimpleBlock[]> ceilFloorPairs = new ArrayList<>();
        ArrayList<Boolean> boundaries = new ArrayList<>();

        FastNoise circleNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_CAVECLUSTER_CIRCLENOISE, world -> {
            FastNoise n = new FastNoise((int) (world.getSeed() * 11));
            n.SetNoiseType(FastNoise.NoiseType.Simplex);
            n.SetFrequency(0.09f);

            return n;
        });

        center = new SimpleBlock(ceil.getPopData(), ceil.getX(), (ceil.getY() + floor.getY()) / 2, ceil.getZ());
        int lowest = center.getY();

        // If this is too close with a structure with a cave cluster suppressant, don't
        // populate.


        // Perform a breadth-first search from the center.

        HashMap<SimpleBlock, Wall[]> seen = new HashMap<>();
        Queue<SimpleBlock> queue = new ArrayDeque<>();
        queue.add(center); // Add the root element
        seen.put(center, new Wall[] {new Wall(ceil), new Wall(floor)});

        // TerraformGeneratorPlugin.logger.info("Entering BFS for " + center);
        while (!queue.isEmpty()) {
            SimpleBlock v = queue.remove();

            // Process the node
            Wall vCeil = seen.get(v)[0];
            Wall vFloor = seen.get(v)[1];
            lowest = Math.min(vFloor.getY(), lowest);
            ceilFloorPairs.add(new SimpleBlock[] {
                    vCeil.get(), vFloor.get()
            });
            // TerraformGeneratorPlugin.logger.info("NLOOP: " + v);

            boolean sawFailCondition = false;
            for (BlockFace face : BlockUtils.directBlockFaces) {
                // Simulate the criteria as edge connections.
                // Continue if the neighbour doesn't meet the criteria
                SimpleBlock neighbour = v.getRelative(face);

                if (seen.containsKey(neighbour)) {
                    // TerraformGeneratorPlugin.logger.info("Seen " + neighbour);
                    continue;
                }

                // Check if neighbour is within radius
                double equationResult = Math.pow(neighbour.getX() - center.getX(), 2) / Math.pow(radius, 2)
                                        + Math.pow(neighbour.getZ() - center.getZ(), 2) / Math.pow(radius, 2);
                if (equationResult > 1 + 0.7 * circleNoise.GetNoise(neighbour.getX(), neighbour.getZ())) {
                    sawFailCondition = true;
                    // TerraformGeneratorPlugin.logger.info("OOB " + neighbour + ": " + equationResult);
                    continue;
                }

                Wall candidateFloorWall = new Wall(neighbour).findStonelikeFloor(60);
                Wall candidateCeilWall = new Wall(neighbour).findStonelikeCeiling(60);

                // Misc checks that don't affect boundary condition
                if (candidateFloorWall == null
                    || candidateCeilWall == null
                    || BlockUtils.amethysts.contains(floor.getType())
                    || BlockUtils.fluids.contains(floor.getUp().getType())
                    || BlockUtils.amethysts.contains(ceil.getDown().getType())
                    || candidateFloorWall.getType() == Material.MOSS_BLOCK
                    || candidateFloorWall.getType() == Material.DRIPSTONE_BLOCK
                    || candidateFloorWall.getUp().isSolid()
                    || candidateCeilWall.getDown().isSolid())
                {
                    // TerraformGeneratorPlugin.logger.info("Misc Skip " + neighbour);
                    continue;
                }

                // Process under BFS
                seen.put(neighbour, new Wall[] {candidateCeilWall, candidateFloorWall});
                queue.add(neighbour);
                // TerraformGeneratorPlugin.logger.info("Enqueued " + neighbour);
            }

            // If you saw a node that fails the radius equation,
            // then you're a boundary block.
            boundaries.add(sawFailCondition);
            // TerraformGeneratorPlugin.logger.info("Processed " + v + ", SZ Q: " + queue.size());
        }
        // TerraformGeneratorPlugin.logger.info("Finished for " + center);

        lowestYCenter = center.getAtY(lowest);
        for (int i = 0; i < ceilFloorPairs.size(); i++) {
            SimpleBlock[] candidates = ceilFloorPairs.get(i);

            // Late fluid checks
            if (BlockUtils.fluids.contains(candidates[1].getAtY(lowest + 1).getType())) {
                continue;
            }
            oneUnit(tw, random, candidates[0], candidates[1], boundaries.get(i));
        }
    }
}
