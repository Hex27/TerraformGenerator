package org.terraform.structure.room;

import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;

public class PathPopulatorData {
    public SimpleBlock base;
    public BlockFace dir;
    public boolean isOverlapped = false;
    public int pathWidth;
    public boolean isTurn;
    public boolean isEnd = false;

    public PathPopulatorData(SimpleBlock base, BlockFace dir, int pathWidth, boolean isTurn) {
        this.base = base;
        this.dir = dir;
        this.pathWidth = pathWidth;
        this.isTurn = isTurn;
    }

    public PathPopulatorData(Wall base, int pathWidth) {
        this.base = base.get();
        this.dir = base.getDirection();
        this.pathWidth = pathWidth;
    }

    /**
     * This is mainly used for creating repeating patterns.
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + base.getX();
        result = prime * result + base.getY();
        result = prime * result + base.getZ();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PathPopulatorData)) return false;
        PathPopulatorData other = (PathPopulatorData) obj;
        return base.getX() == other.base.getX() 
        		&& base.getZ() == other.base.getZ() 
        		&& base.getY() == other.base.getY();
    }
}