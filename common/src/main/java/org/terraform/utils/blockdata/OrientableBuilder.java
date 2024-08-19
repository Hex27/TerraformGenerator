package org.terraform.utils.blockdata;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Orientable;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class OrientableBuilder {
    private final @NotNull Orientable blockData;

    public OrientableBuilder(@NotNull Material mat) {
        this.blockData = (Orientable) Bukkit.createBlockData(mat);
    }

    public OrientableBuilder(Material... mat) {
        this.blockData = (Orientable) Bukkit.createBlockData(GenUtils.randChoice(mat));
    }


    public @NotNull OrientableBuilder setAxis(@NotNull Axis axis) {
        this.blockData.setAxis(axis);
        return this;
    }


    public @NotNull OrientableBuilder apply(@NotNull SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull OrientableBuilder apply(@NotNull Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull OrientableBuilder apply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public @NotNull OrientableBuilder lapply(@NotNull SimpleBlock block) {
        if (!block.isSolid()) {
            block.setBlockData(blockData);
        }
        return this;
    }

    public @NotNull OrientableBuilder lapply(@NotNull Wall block) {
        if (!block.isSolid()) {
            block.setBlockData(blockData);
        }
        return this;
    }

    public @NotNull OrientableBuilder lapply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        if (!data.getType(x, y, z).isSolid()) {
            data.setBlockData(x, y, z, blockData);
        }
        return this;
    }

    public @NotNull Orientable get() {
        return blockData;
    }
}
