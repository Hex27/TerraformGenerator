package org.terraform.structure.room;

import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;


public abstract class PathPopulatorAbstract {
    /**
     * This is still used by the jigsaw system to get path RADIUS. It is
     * NOT width.
     */
    public int getPathWidth() {
        return 3;
    }

    /**
     * Not really used because path height is a stub in the jigsaw system
     */
    public int getPathHeight() {
        return 3;
    }

    /**
     * This is still used by the jigsaw system.
     */
    public int getPathMaxBend() {
        return -1;
    }

    public abstract void populate(PathPopulatorData ppd);

    /**
     * To return false if you want the default carver to happen.
     * Return true if you're handling everything here.
     * @deprecated Jigsaw system uses PathWriter to do this.
     */
    @Deprecated
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean customCarve(SimpleBlock base, BlockFace dir, int pathWidth) {
        return false;
    }
}
