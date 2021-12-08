package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class DirectionalBuilder {

    private final Directional blockData;

    public DirectionalBuilder(Material mat) {
        this.blockData = (Directional) Bukkit.createBlockData(mat);
    }

    public DirectionalBuilder(Material... mat) {
        this.blockData = (Directional) Bukkit.createBlockData(GenUtils.randMaterial(mat));
    }


    public DirectionalBuilder setFacing(BlockFace face) {
        this.blockData.setFacing(face);
        return this;
    }

    public DirectionalBuilder apply(SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public DirectionalBuilder apply(Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public DirectionalBuilder apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public Directional get() {
        return blockData;
    }
}
