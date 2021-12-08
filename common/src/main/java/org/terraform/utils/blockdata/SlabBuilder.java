package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Slab;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
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

    public SlabBuilder apply(SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public SlabBuilder apply(Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public SlabBuilder apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public SlabBuilder lapply(SimpleBlock block) {
    	if(block.getType().isSolid())
    		return this;
        block.setBlockData(blockData);
        return this;
    }

    public SlabBuilder lapply(Wall block) {
    	if(block.getType().isSolid())
    		return this;
        block.setBlockData(blockData);
        return this;
    }

    public SlabBuilder lapply(PopulatorDataAbstract data, int x, int y, int z) {
    	if(data.getType(x,y,z).isSolid())
    		return this;
        data.setBlockData(x, y, z, blockData);
        return this;
    }
    
    public Slab get() {
        return blockData;
    }
}
