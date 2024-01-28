package org.terraform.structure.room.path;

import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;

public class CavePathWriter extends PathWriter{
    @Override
    public void apply(PopulatorDataAbstract popData, TerraformWorld tw, PathState.PathNode node) {
        BlockUtils.carveCaveAir((int) (node.center.hashCode() * tw.getSeed()),
                node.pathWidth, node.pathWidth + 1, node.pathWidth,
                new SimpleBlock(popData, node.center), false,
                BlockUtils.caveCarveReplace);
    }
}
