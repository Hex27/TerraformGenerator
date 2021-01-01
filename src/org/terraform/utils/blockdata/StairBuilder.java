package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class StairBuilder {

    private final Stairs blockData;

    public StairBuilder(Material mat) {
        this.blockData = (Stairs) Bukkit.createBlockData(mat);
    }

    public StairBuilder(Material... mat) {
        this.blockData = (Stairs) Bukkit.createBlockData(GenUtils.randMaterial(mat));
    }


    public StairBuilder setFacing(BlockFace face) {
        this.blockData.setFacing(face);
        return this;
    }

    public StairBuilder setHalf(Bisected.Half half) {
        this.blockData.setHalf(half);
        return this;
    }

    public StairBuilder setShape(Stairs.Shape shape) {
        this.blockData.setShape(shape);
        return this;
    }

    public StairBuilder setWaterlogged(boolean bool) {
        this.blockData.setWaterlogged(bool);
        return this;
    }

    public StairBuilder apply(SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public StairBuilder apply(Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public StairBuilder apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public Stairs get() {
        return blockData;
    }
}
