package org.terraform.structure.room;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class LegacyPathGenerator {
    @NotNull
    final HashSet<PathPopulatorData> path = new HashSet<>();
    final Random rand;
    final Material[] mat;
    private final int[] upperBound;
    private final int[] lowerBound;
    private final int maxNoBend;
    PathPopulatorAbstract populator;
    private SimpleBlock base;
    private BlockFace dir;
    private int straightInARow = 0;
    private int length = 0;
    private int pathWidth = 3;
    private int pathHeight = 3;
    private boolean dead = false;

    public LegacyPathGenerator(SimpleBlock origin,
                               Material[] mat,
                               @NotNull Random rand,
                               int[] upperBound,
                               int[] lowerBound,
                               int maxNoBend)
    {
        this.base = origin;
        this.rand = rand;
        this.dir = BlockUtils.directBlockFaces[GenUtils.randInt(rand, 0, 3)];
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        this.mat = mat;
        if (maxNoBend != -1) {
            this.maxNoBend = maxNoBend;
        }
        else {
            this.maxNoBend = (int) ((upperBound[0] - lowerBound[0]) * 0.5);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isDead() {
        return dead;
    }

    private boolean isOutOfBounds(@NotNull SimpleBlock base) {
        return base.getX() >= upperBound[0] + 10
               || base.getZ() >= upperBound[1] + 10
               || base.getX() <= lowerBound[0] - 10
               || base.getZ() <= lowerBound[1] - 10;
    }

    public void die() {
        this.dead = true;
        wall();

        PathPopulatorData candidate = new PathPopulatorData(base, dir, pathWidth, false);
        candidate.isEnd = true;
        // Prevent overlapping - path populators don't need to rerun against the same locations
        if (!path.add(candidate)) {
            path.remove(candidate);
            candidate.isOverlapped = true;
            path.add(candidate);
        }
    }

    public void populate() {
        if (populator != null) {
            for (PathPopulatorData pathPopulatorData : path) {
                populator.populate(pathPopulatorData);
            }
        }
    }

    public void placeNext() {
        if (length > (upperBound[0] - lowerBound[0])) {
            die();
            return;
        }

        if (isOutOfBounds(base)) {
            die();
            return;
        }

        BlockFace oldDir = dir;
        // Only turn if the path is turning at a nice degree
        // This should prevent weird intersections.
        if (length % (1 + 2 * this.pathWidth) == 0) {
            // Make a turn if out of bounds
            while (isOutOfBounds(base.getRelative(dir))) {
                straightInARow = 0;

                // For ensuring that corners are covered.
                int cover = this.pathWidth - 1;
                if (cover == 0) {
                    cover = 1;
                }
                for (int i = 0; i < cover; i++) {
                    setHall();
                    base = base.getRelative(dir);
                }
                for (int i = 0; i < cover; i++) {
                    base = base.getRelative(dir.getOppositeFace());
                }
                // turn
                dir = BlockUtils.getTurnBlockFace(rand, dir);
            }

            // Make a turn if too long
            straightInARow++;
            if (straightInARow > maxNoBend || GenUtils.chance(rand, 1, 500)) {
                straightInARow = 0;

                // For ensuring that corners are covered
                int cover = this.pathWidth - 1;
                if (cover == 0) {
                    cover = 1;
                }
                for (int i = 0; i < cover; i++) {
                    setHall();
                    base = base.getRelative(dir);
                }
                for (int i = 0; i < cover; i++) {
                    base = base.getRelative(dir.getOppositeFace());
                }
                // turn
                dir = BlockUtils.getTurnBlockFace(rand, dir);
            }
        }

        // Carve
        if (!populator.customCarve(base, dir, pathWidth)) {
            setHall();
        }

        // / Handle populating the paths
        PathPopulatorData candidate = new PathPopulatorData(base, dir, pathWidth, oldDir != dir);

        // Prevent overlapping - path populators don't need to rerun against the same locations
        if (!path.contains(candidate)) {
            path.add(candidate);
        }
        else {
            path.remove(candidate);
            candidate.isOverlapped = true;
            path.add(candidate);
        }

        base = base.getRelative(dir);
        length++;
    }

    private void wall() {
        if (mat[0] == Material.BARRIER) {
            return;
        }
        for (int h = 1; h <= pathHeight; h++) {
            if (base.getRelative(0, h, 0).getType() != Material.CAVE_AIR) {
                base.getRelative(0, h, 0).setType(GenUtils.randChoice(mat));
            }
        }

        for (BlockFace f : BlockUtils.getAdjacentFaces(dir)) {
            SimpleBlock rel = base;
            for (int i = 0; i <= pathWidth / 2; i++) {
                rel = rel.getRelative(f);
                for (int h = 1; h <= pathHeight; h++) {
                    if (rel.getRelative(0, h, 0).getType() != Material.CAVE_AIR) {
                        rel.getRelative(0, h, 0).setType(GenUtils.randChoice(mat));
                    }
                }
            }
        }
    }

    private void setHall() {
        if (mat[0] == Material.BARRIER) {
            return;
        }

        if (base.getType() != Material.CAVE_AIR) {
            base.setType(GenUtils.randChoice(mat));
        }

        Wall w = new Wall(base).getUp();
        w.Pillar(pathHeight, rand, Material.CAVE_AIR);
        if (base.getRelative(0, pathHeight + 1, 0).getType() != Material.CAVE_AIR) {
            base.getRelative(0, pathHeight + 1, 0).setType(GenUtils.randChoice(mat));
        }

        for (BlockFace f : BlockUtils.getAdjacentFaces(dir)) {
            SimpleBlock rel = base;
            for (int i = 0; i <= pathWidth / 2; i++) {
                rel = rel.getRelative(f);
                // Bukkit.getLogger().info(i + ":" + pathWidth/2);
                if (i == pathWidth / 2) { // Walls
                    for (int h = 1; h <= pathHeight; h++) {
                        if (rel.getRelative(0, h, 0).getType() != Material.CAVE_AIR) {
                            rel.getRelative(0, h, 0).setType(GenUtils.randChoice(mat));
                        }
                    }
                }
                else { // Air in hallway (And floor and ceiling)
                    if (rel.getType() != Material.CAVE_AIR) {
                        rel.setType(GenUtils.randChoice(mat));
                    }

                    w = new Wall(rel).getUp();
                    w.Pillar(pathHeight, rand, Material.CAVE_AIR);
                    if (rel.getRelative(0, pathHeight + 1, 0).getType() != Material.CAVE_AIR) {
                        rel.getRelative(0, pathHeight + 1, 0).setType(GenUtils.randChoice(mat));
                    }
                }
            }
        }
    }

    /**
     * Generate a straight path
     *
     * @param start     Start block. Can be null, in which case base block of the instance is used.
     * @param direction Start direction. Can be null, when random direction is used.
     */

    public void generateStraightPath(@Nullable SimpleBlock start, @Nullable BlockFace direction, int length) {
        ArrayList<PathPopulatorData> pathPopulatorDatas = new ArrayList<>();
        if (direction == null) {
            direction = this.dir;
        }
        if (start == null) {
            start = this.base;
        }

        for (int i = 0; i < length; i++) {
            if (!populator.customCarve(start, direction, pathWidth)) {
                setHall();
            }
            start = start.getRelative(direction);
            pathPopulatorDatas.add(new PathPopulatorData(start, direction, pathWidth, false));
        }

        if (populator != null) {
            for (PathPopulatorData pathPopulatorData : pathPopulatorDatas) {
                populator.populate(pathPopulatorData);
            }
        }
    }

    /**
     * @param populator the populator to set
     */
    public void setPopulator(@NotNull PathPopulatorAbstract populator) {
        this.populator = populator;
        this.pathWidth = populator.getPathWidth();
        this.pathHeight = populator.getPathHeight();
    }


}
