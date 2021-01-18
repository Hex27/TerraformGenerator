package org.terraform.utils.blockdata;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Orientable;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class OrientableBuilder {
    private final Orientable blockData;

    public OrientableBuilder(Material mat) {
        this.blockData = (Orientable) Bukkit.createBlockData(mat);
    }

    public OrientableBuilder(Material... mat) {
        this.blockData = (Orientable) Bukkit.createBlockData(GenUtils.randMaterial(mat));
    }


    public OrientableBuilder setAxis(Axis axis) {
        this.blockData.setAxis(axis);
        return this;
    }


    public OrientableBuilder apply(SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public OrientableBuilder apply(Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public OrientableBuilder apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public OrientableBuilder lapply(SimpleBlock block) {
    	if(!block.getType().isSolid())
    		block.setBlockData(blockData);
        return this;
    }

    public OrientableBuilder lapply(Wall block) {
    	if(!block.getType().isSolid())
    		block.setBlockData(blockData);
        return this;
    }

    public OrientableBuilder lapply(PopulatorDataAbstract data, int x, int y, int z) {
    	if(!data.getType(x, y, z).isSolid())
    		data.setBlockData(x, y, z, blockData);
        return this;
    }

    public Orientable get() {
        return blockData;
    }
}
