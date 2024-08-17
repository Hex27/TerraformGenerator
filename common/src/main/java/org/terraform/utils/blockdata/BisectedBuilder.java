package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class BisectedBuilder {

    private final @NotNull Bisected blockData;

    public BisectedBuilder(@NotNull Material mat) {
        this.blockData = (Bisected) Bukkit.createBlockData(mat);
    }

    public BisectedBuilder(Material... mat) {
        this.blockData = (Bisected) Bukkit.createBlockData(GenUtils.randMaterial(mat));
    }
    
    public @NotNull BisectedBuilder setHalf(Bisected.@NotNull Half half) {
        this.blockData.setHalf(half);
        return this;
    }

    public @NotNull BisectedBuilder apply(@NotNull SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull BisectedBuilder apply(@NotNull Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull BisectedBuilder apply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public @NotNull BisectedBuilder placeBoth(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
    	Bisected upper = (Bisected) blockData.clone();
    	upper.setHalf(Half.TOP);
        Bisected lower = (Bisected) blockData.clone();
        lower.setHalf(Half.BOTTOM);
        data.setBlockData(x, y, z, lower);
        data.setBlockData(x, y+1, z, upper);
        return this;
    }

    public @NotNull BisectedBuilder placeBoth(@NotNull SimpleBlock block) {
    	Bisected upper = (Bisected) blockData.clone();
    	upper.setHalf(Half.TOP);
        Bisected lower = (Bisected) blockData.clone();
        lower.setHalf(Half.BOTTOM);
        block.setBlockData(lower);
        block.getRelative(0,1,0).setBlockData(upper);
        return this;
    }
    
    public @NotNull Bisected get() {
        return blockData;
    }
}
