package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class BisectedBuilder {

    private final Bisected blockData;

    public BisectedBuilder(Material mat) {
        this.blockData = (Bisected) Bukkit.createBlockData(mat);
    }

    public BisectedBuilder(Material... mat) {
        this.blockData = (Bisected) Bukkit.createBlockData(GenUtils.randMaterial(mat));
    }
    
    public BisectedBuilder setHalf(Bisected.Half half) {
        this.blockData.setHalf(half);
        return this;
    }

    public BisectedBuilder apply(SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public BisectedBuilder apply(Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public BisectedBuilder apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public BisectedBuilder placeBoth(PopulatorDataAbstract data, int x, int y, int z) {
    	Bisected upper = (Bisected) blockData.clone();
    	upper.setHalf(Half.TOP);
        Bisected lower = (Bisected) blockData.clone();
        lower.setHalf(Half.BOTTOM);
        data.setBlockData(x, y, z, lower);
        data.setBlockData(x, y+1, z, upper);
        return this;
    }

    public BisectedBuilder placeBoth(SimpleBlock block) {
    	Bisected upper = (Bisected) blockData.clone();
    	upper.setHalf(Half.TOP);
        Bisected lower = (Bisected) blockData.clone();
        lower.setHalf(Half.BOTTOM);
        block.setBlockData(lower);
        block.getRelative(0,1,0).setBlockData(upper);
        return this;
    }
    
    public Bisected get() {
        return blockData;
    }
}
