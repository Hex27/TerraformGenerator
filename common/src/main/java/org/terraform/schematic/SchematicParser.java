package org.terraform.schematic;

import org.bukkit.block.data.BlockData;
import org.terraform.data.SimpleBlock;

public class SchematicParser {
    public void applyData(SimpleBlock block, BlockData data) {
        block.setBlockData(data);
    }

    /**
     * Gets called after all the blocks are placed
     */
    public void finalizeSchematic() {}
}
