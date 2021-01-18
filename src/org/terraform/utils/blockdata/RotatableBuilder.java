package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rotatable;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class RotatableBuilder {

    private final Rotatable blockData;

    public RotatableBuilder(Material mat) {
        this.blockData = (Rotatable) Bukkit.createBlockData(mat);
    }

    public RotatableBuilder(Material... mat) {
        this.blockData = (Rotatable) Bukkit.createBlockData(GenUtils.randMaterial(mat));
    }


    public RotatableBuilder setRotation(BlockFace face) {
        this.blockData.setRotation(face);
        return this;
    }

    public RotatableBuilder apply(SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public RotatableBuilder apply(Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public RotatableBuilder apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public Rotatable get() {
        return blockData;
    }
}
