package org.terraform.structure.warmoceanruins;

import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;

public class WarmOceansPathPopulator extends PathPopulatorAbstract {
    @Override
    public void populate(PathPopulatorData ppd) {

    }

    // This path populator doesn't carve anything.
    @Override
    public boolean customCarve(SimpleBlock base, BlockFace dir, int pathWidth) {
        return true;
    }
}
