package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class MultipleFacingBuilder {

    private final MultipleFacing blockData;

    public MultipleFacingBuilder(Material mat) {
        this.blockData = (MultipleFacing) Bukkit.createBlockData(mat);
    }

    public MultipleFacingBuilder(Material... mat) {
        this.blockData = (MultipleFacing) Bukkit.createBlockData(GenUtils.randMaterial(mat));
    }


    public MultipleFacingBuilder setFace(BlockFace face, boolean isEnabled) {
        this.blockData.setFace(face, isEnabled);
        return this;
    }

    public MultipleFacingBuilder apply(SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public MultipleFacingBuilder apply(Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public MultipleFacingBuilder apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public MultipleFacing get() {
        return blockData;
    }
}
