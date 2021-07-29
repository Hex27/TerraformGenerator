package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.TrapDoor;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class TrapdoorBuilder {

    private final TrapDoor blockData;

    public TrapdoorBuilder(Material mat) {
        this.blockData = (TrapDoor) Bukkit.createBlockData(mat);
    }

    public TrapdoorBuilder(Material... mat) {
        this.blockData = (TrapDoor) Bukkit.createBlockData(GenUtils.randMaterial(mat));
    }


    public TrapdoorBuilder setWaterlogged(boolean water) {
        this.blockData.setWaterlogged(water);
        return this;
    }

    public TrapdoorBuilder setHalf(Half h) {
        this.blockData.setHalf(h);
        return this;
    }

    public TrapdoorBuilder setPowered(boolean powered) {
        this.blockData.setPowered(powered);
        return this;
    }

    public TrapdoorBuilder setOpen(boolean open) {
        this.blockData.setOpen(open);
        return this;
    }

    public TrapdoorBuilder setFacing(BlockFace face) {
        this.blockData.setFacing(face);
        return this;
    }

    public TrapdoorBuilder apply(SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public TrapdoorBuilder apply(Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public TrapdoorBuilder lapply(Wall block) {
    	if(!block.getType().isSolid())
    		block.setBlockData(blockData);
        return this;
    }

    public TrapdoorBuilder apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public TrapDoor get() {
        return blockData;
    }
}
