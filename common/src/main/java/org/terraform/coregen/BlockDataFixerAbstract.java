package org.terraform.coregen;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import org.terraform.data.SimpleBlock;

import java.util.ArrayList;

public abstract class BlockDataFixerAbstract {
    private final ArrayList<Vector> multifacing = new ArrayList<>();
    public boolean hasFlushed = false;

    public Vector[] flush() {
        Vector[] stuff = multifacing.toArray(new Vector[0]);
        multifacing.clear();
        hasFlushed = true;
        return stuff;
    }

    public void pushChanges(Vector e) {
        multifacing.add(e);
    }

    public abstract String updateSchematic(String schematic);

    public abstract void correctFacing(Vector v, SimpleBlock b, BlockData data, BlockFace face);
}
