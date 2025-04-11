package org.terraform.utils;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;

public class StalactiteBuilder {

    private Material[] solidBlockType;
    private Material[] wallType;
    private boolean isFacingUp;
    private int verticalSpace;
    private float minRadius = 0;

    public StalactiteBuilder(Material... wallType) {
        this.wallType = wallType;
    }

    public void build(@NotNull Random rand, @NotNull Wall w) {
        if (!TConfig.areDecorationsEnabled()) {
            return;
        }

        if (verticalSpace < 6) {
            return;
        }

        int stalactiteHeight;
        if (verticalSpace > 60) // massive cave
        {
            stalactiteHeight = GenUtils.randInt(rand, 6, 25);
        }
        else if (verticalSpace > 30) // large cave
        {
            stalactiteHeight = GenUtils.randInt(rand, 5, 17);
        }
        else if (verticalSpace > 15) // medium cave
        {
            stalactiteHeight = GenUtils.randInt(rand, 3, 10);
        }
        else // likely noodle cave
        {
            stalactiteHeight = GenUtils.randInt(rand, 1, 2);
        }

        if (stalactiteHeight < 4) {
            // tiny stalactite (1-3 blocks)
            if (isFacingUp) {
                w.LPillar(stalactiteHeight, rand, wallType);
            }
            else {
                w.downLPillar(rand, stalactiteHeight, wallType);
            }
        }
        else if (stalactiteHeight < 7) {
            // Bigger stalactite. (4-7 blocks)
            if (isFacingUp) {
                w.LPillar(stalactiteHeight, rand, wallType);
                w.Pillar(GenUtils.randInt(rand, 2, 3), rand, solidBlockType);
            }
            else {
                w.downLPillar(rand, stalactiteHeight, wallType);
                w.downPillar(GenUtils.randInt(rand, 2, 3), solidBlockType);
            }
        }
        else {
            // Large stalactite (8+ blocks)
            if (isFacingUp) {
                makeSpike(
                        w.getDown(),
                        GenUtils.randDouble(rand, stalactiteHeight / 6.0, stalactiteHeight / 4.0),
                        stalactiteHeight,
                        true
                );
            }
            else {
                makeSpike(
                        w.getUp(),
                        GenUtils.randDouble(rand, stalactiteHeight / 6.0, stalactiteHeight / 4.0),
                        stalactiteHeight,
                        false
                );
            }
        }
    }

    public @NotNull StalactiteBuilder setSolidBlockType(Material... solidBlockType) {
        this.solidBlockType = solidBlockType;
        return this;
    }

    public @NotNull StalactiteBuilder setWallType(Material... wallType) {
        this.wallType = wallType;
        return this;
    }

    public @NotNull StalactiteBuilder setFacingUp(boolean isFacingUp) {
        this.isFacingUp = isFacingUp;
        return this;
    }

    public @NotNull StalactiteBuilder setMinRadius(int minRadius) {
        this.minRadius = minRadius;
        return this;
    }

    public @NotNull StalactiteBuilder setVerticalSpace(int verticalSpace) {
        this.verticalSpace = verticalSpace;
        return this;
    }

    /**
     * Responsible for generating a stalactite or stalagmite.
     *
     * @param facingUp generates stalagmites if true. If not, makes stalactites.
     */
    public void makeSpike(@NotNull SimpleBlock root, double baseRadius, int height, boolean facingUp) {

        // HEIGHT CANNOT BE LESS THAN 1. (1.0/0.0) DOES NOT THROW ARITHMETIC ERRORS
        if (height < 8) {
            return;
        }

        float maxRadius = 3f;
        baseRadius = Math.min(maxRadius, Math.max(baseRadius, minRadius));

        // Perform a BFS against the cone 3d equation to prevent spheres from overwriting
        // each other. Should reduce chunk r/w ops

        // Assume that it will use slightly more than coneVolume blocks
        Queue<SimpleBlock> queue = new ArrayDeque<>((int) (Math.PI * Math.pow(baseRadius, 2) * (height / 2.5)));
        queue.add(root);
        HashSet<SimpleBlock> seen = new HashSet<>();
        seen.add(root);
        while (!queue.isEmpty()) {
            SimpleBlock v = queue.remove();
            v.setType(solidBlockType);

            // Place blocks for v
            for (BlockFace rel : BlockUtils.sixBlockFaces) {
                SimpleBlock neighbour = v.getRelative(rel);
                if (seen.contains(neighbour)) {
                    continue;
                }

                int yOffset = neighbour.getY() - root.getY();
                if (facingUp && (yOffset > height || yOffset < 0)) {
                    continue;
                }
                if (!facingUp && (yOffset < -height || yOffset > 0)) {
                    continue;
                }
                /*
                 * x^2 + z^2 - ((y-h)/baseRadius)^2 = 0
                 */
                double coneEqn = facingUp ?
                                 // Stalagmites. Minus as it grows up
                                 Math.pow(neighbour.getX() - root.getX(), 2) + Math.pow(neighbour.getZ() - root.getZ(),
                                         2) - Math.pow((yOffset - height) / (height / baseRadius), 2) :
                                 // Stalactites. Plus as it grows down
                                 Math.pow(neighbour.getX() - root.getX(), 2) + Math.pow(neighbour.getZ() - root.getZ(),
                                         2) - Math.pow((yOffset + height) / (height / baseRadius), 2);
                // Only make cones larger, not smaller. This prevents blobs.
                // coneEqn -= Math.abs(noise.GetNoise(neighbour.getX(), neighbour.getZ()));
                if (coneEqn > 0) {
                    continue; // <=0 is within the spike
                }

                queue.add(neighbour);
                seen.add(neighbour);
            }
        }
    }

}
