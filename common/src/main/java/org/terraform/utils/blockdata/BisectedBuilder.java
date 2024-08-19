package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;

public class BisectedBuilder {

    private final @NotNull Bisected blockData;

    public BisectedBuilder(@NotNull Material mat) {
        this.blockData = (Bisected) Bukkit.createBlockData(mat);
    }

    public @NotNull BisectedBuilder placeBoth(@NotNull SimpleBlock block) {
        Bisected upper = (Bisected) blockData.clone();
        upper.setHalf(Half.TOP);
        Bisected lower = (Bisected) blockData.clone();
        lower.setHalf(Half.BOTTOM);
        block.setBlockData(lower);
        block.getUp().setBlockData(upper);
        return this;
    }

}
