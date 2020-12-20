package org.terraform.structure.room;

import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;

public class PathPopulatorData {
    public SimpleBlock base;
    public BlockFace dir;
    public int pathWidth;

    public PathPopulatorData(SimpleBlock base, BlockFace dir, int pathWidth) {
        this.base = base;
        this.dir = dir;
        this.pathWidth = pathWidth;
    }

    public PathPopulatorData(Wall base, int pathWidth) {
        this.base = base.get();
        this.dir = base.getDirection();
        this.pathWidth = pathWidth;
    }

    /**
     * This is mainly used for creating repeating patterns.
     *
     * @param multiplier
     * @return
     */
    public int calcRemainder(int multiplier) {
        if (dir.getModX() != 0) {
            return base.getX() % multiplier;
        } else if (dir.getModZ() != 0) {
            return base.getZ() % multiplier;
        } else {
            return 0; //BlockFace was not NSEW
        }
    }
}