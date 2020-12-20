package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Slab;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class SlabBuilder {
    private final Slab blockData;

    public SlabBuilder(Material mat) {
        this.blockData = (Slab) Bukkit.createBlockData(mat);
    }

    public SlabBuilder(Material... mat) {
        this.blockData = (Slab) Bukkit.createBlockData(GenUtils.randMaterial(mat));
    }


    public SlabBuilder setType(Slab.Type type) {
        this.blockData.setType(type);
        return this;
    }

    public SlabBuilder setWaterlogged(boolean bool) {
        this.blockData.setWaterlogged(bool);
        return this;
    }

    public void apply(SimpleBlock block) {
        block.setBlockData(blockData);
    }

    public void apply(Wall block) {
        block.setBlockData(blockData);
    }

    public void apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
    }

    public Slab get() {
        return blockData;
    }
}
