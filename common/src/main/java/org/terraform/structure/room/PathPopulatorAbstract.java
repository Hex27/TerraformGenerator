package org.terraform.structure.room;

import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;


public abstract class PathPopulatorAbstract {
    public int getPathWidth() {
        return 3;
    }

    public int getPathHeight() {
        return 3;
    }

    public abstract void populate(PathPopulatorData ppd);

    /**
     * To return false if you want the default carver to happen.
     * Return true if you're handling everything here.
     *
     * @param base
     * @param dir
     * @param pathWidth
     * @return
     */
    public boolean customCarve(SimpleBlock base, BlockFace dir, int pathWidth) {
        return false;
    }
}
