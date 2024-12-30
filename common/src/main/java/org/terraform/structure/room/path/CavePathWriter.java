package org.terraform.structure.room.path;

import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;

public class CavePathWriter extends PathWriter {
    private final float rXMod;
    private final float rYMod;
    private final float rZMod;
    private final int xOff;
    private final int yOff;
    private final int zOff;

    public CavePathWriter(float rXMod, float rYMod, float rZMod, int xOff, int yOff, int zOff) {
        this.rXMod = rXMod;
        this.rYMod = rYMod;
        this.rZMod = rZMod;
        this.xOff = xOff;
        this.yOff = yOff;
        this.zOff = zOff;
    }

    @Override
    public void apply(@NotNull PopulatorDataAbstract popData,
                      @NotNull TerraformWorld tw,
                      PathState.@NotNull PathNode node)
    {
        BlockUtils.carveCaveAir(
                (int) (node.center.hashCode() * tw.getSeed()),
                node.pathRadius + rXMod,
                node.pathRadius + rYMod,
                node.pathRadius + rZMod,
                new SimpleBlock(popData, node.center.getRelative(xOff, yOff, zOff)),
                false,
                BlockUtils.caveCarveReplace
        );
    }
}
