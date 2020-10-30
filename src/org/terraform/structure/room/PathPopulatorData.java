package org.terraform.structure.room;

import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;

public class PathPopulatorData {
    public SimpleBlock base;
    public BlockFace dir;
    public int pathWidth;

    public PathPopulatorData(SimpleBlock base, BlockFace dir, int pathWidth) {
        this.base = base;
        this.dir = dir;
        this.pathWidth = pathWidth;
    }
}