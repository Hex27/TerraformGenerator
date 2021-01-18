package org.terraform.structure.room;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class PathGenerator {
    private final int[] upperBound;
    private final int[] lowerBound;
    ArrayList<PathPopulatorData> path = new ArrayList<>();
    PathPopulatorAbstract populator;
    Random rand;
    Material[] mat;
    private SimpleBlock base;
    private BlockFace dir;
    private int maxNoBend = 15;
    private int straightInARow = 0;
    private int length = 0;
    private int pathWidth = 3;
    private int pathHeight = 3;
    private boolean dead = false;

    public PathGenerator(SimpleBlock origin, Material[] mat, Random rand, int[] upperBound, int[] lowerBound) {
        this.base = origin;
        this.rand = rand;
        this.dir = BlockUtils.directBlockFaces[GenUtils.randInt(rand, 0, 3)];
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        this.mat = mat;
        this.maxNoBend = (int) ((upperBound[0] - lowerBound[0]) * 0.5);
    }

    public boolean isDead() {
        return dead;
    }

    private boolean isOutOfBounds(SimpleBlock base) {
//		Bukkit.getLogger().info(base.getX() + "," + base.getZ() + ": " 
//	+ upperBound[0] + "," + upperBound[1]
//	+ " - " + lowerBound[0] + "," + lowerBound[1]);
        return base.getX() >= upperBound[0] + 10
                || base.getZ() >= upperBound[1] + 10
                || base.getX() <= lowerBound[0] - 10
                || base.getZ() <= lowerBound[1] - 10;
    }

    public void die() {
        this.dead = true;
        wall();
    }

    public void populate() {
        if (populator != null) {
            for (PathPopulatorData pathPopulatorData : path) {
                populator.populate(pathPopulatorData);
            }
        }
    }

    public void next() {
        if (length > (upperBound[0] - lowerBound[0])) {
            die();
            return;
        }

        if (isOutOfBounds(base)) {
            die();
            return;
        }

        while (isOutOfBounds(base.getRelative(dir))) {
            straightInARow = 0;

            //For ensuring that corners are covered.
            int cover = this.pathWidth - 1;
            if (cover == 0) cover = 1;
            for (int i = 0; i < cover; i++) {
                setHall();
                base = base.getRelative(dir);
            }
            for (int i = 0; i < cover; i++)
                base = base.getRelative(dir.getOppositeFace());

            dir = BlockUtils.getTurnBlockFace(rand, dir);
        }

        straightInARow++;
        if (straightInARow > maxNoBend || GenUtils.chance(rand, 1, 500)) {
            straightInARow = 0;

            //For ensuring that corners are covered
            int cover = this.pathWidth - 1;
            if (cover == 0) cover = 1;
            for (int i = 0; i < cover; i++) {
                setHall();
                base = base.getRelative(dir);
            }
            for (int i = 0; i < cover; i++)
                base = base.getRelative(dir.getOppositeFace());

            dir = BlockUtils.getTurnBlockFace(rand, dir);
        }

        //base.setType(mat);
        if (!populator.customCarve(base, dir, pathWidth)) {
            setHall();
        }
        path.add(new PathPopulatorData(base, dir, pathWidth));
        base = base.getRelative(dir);
        length++;
    }

    private void wall() {
        if (mat[0] == Material.BARRIER) return;
        for (int h = 1; h <= pathHeight; h++)
            if (base.getRelative(0, h, 0).getType() != Material.CAVE_AIR)
                base.getRelative(0, h, 0).setType(GenUtils.randMaterial(mat));

        for (BlockFace f : BlockUtils.getAdjacentFaces(dir)) {
            SimpleBlock rel = base;
            for (int i = 0; i <= pathWidth / 2; i++) {
                rel = rel.getRelative(f);
                for (int h = 1; h <= pathHeight; h++)
                    if (rel.getRelative(0, h, 0).getType() != Material.CAVE_AIR)
                        rel.getRelative(0, h, 0).setType(GenUtils.randMaterial(mat));

            }
        }
    }

    private void setHall() {
        if (mat[0] == Material.BARRIER) return;

        if (base.getType() != Material.CAVE_AIR)
            base.setType(GenUtils.randMaterial(mat));

        Wall w = new Wall(base).getRelative(0, 1, 0);
        w.Pillar(pathHeight, rand, Material.CAVE_AIR);
        if (base.getRelative(0, pathHeight + 1, 0).getType() != Material.CAVE_AIR)
            base.getRelative(0, pathHeight + 1, 0).setType(GenUtils.randMaterial(mat));

        for (BlockFace f : BlockUtils.getAdjacentFaces(dir)) {
            SimpleBlock rel = base;
            for (int i = 0; i <= pathWidth / 2; i++) {
                rel = rel.getRelative(f);
                //Bukkit.getLogger().info(i + ":" + pathWidth/2);
                if (i == pathWidth / 2) { //Walls
                    for (int h = 1; h <= pathHeight; h++)
                        if (rel.getRelative(0, h, 0).getType() != Material.CAVE_AIR)
                            rel.getRelative(0, h, 0).setType(GenUtils.randMaterial(mat));
                } else { //Air in hallway (And floor and ceiling)
                    if (rel.getType() != Material.CAVE_AIR)
                        rel.setType(GenUtils.randMaterial(mat));
//					rel.getRelative(0,1,0).setType(Material.CAVE_AIR);
//					rel.getRelative(0,2,0).setType(Material.CAVE_AIR);
//					rel.getRelative(0,3,0).setType(Material.CAVE_AIR);
//					if(rel.getRelative(0,4,0).getType() != Material.CAVE_AIR)
//						rel.getRelative(0,4,0).setType(GenUtils.randMaterial(mat));
                    w = new Wall(rel).getRelative(0, 1, 0);
                    w.Pillar(pathHeight, rand, Material.CAVE_AIR);
                    if (rel.getRelative(0, pathHeight + 1, 0).getType() != Material.CAVE_AIR)
                        rel.getRelative(0, pathHeight + 1, 0).setType(GenUtils.randMaterial(mat));
                }
            }
        }
    }

    /**
     * @param populator the populator to set
     */
    public void setPopulator(PathPopulatorAbstract populator) {
        this.populator = populator;
        this.pathWidth = populator.getPathWidth();
        this.pathHeight = populator.getPathHeight();
    }


}
