package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Slab;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class SlabBuilder {
    private final @NotNull Slab blockData;

    public SlabBuilder(@NotNull Material mat) {
        this.blockData = (Slab) Bukkit.createBlockData(mat);
    }

    public SlabBuilder(Material... mat) {
        this.blockData = (Slab) Bukkit.createBlockData(GenUtils.randChoice(mat));
    }


    public @NotNull SlabBuilder setType(Slab.@NotNull Type type) {
        this.blockData.setType(type);
        return this;
    }

    public @NotNull SlabBuilder setWaterlogged(boolean bool) {
        this.blockData.setWaterlogged(bool);
        return this;
    }

    public @NotNull SlabBuilder apply(@NotNull SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull SlabBuilder apply(@NotNull Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull SlabBuilder apply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public @NotNull SlabBuilder lapply(@NotNull SimpleBlock block) {
        if (block.isSolid()) {
            return this;
        }
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull SlabBuilder lapply(@NotNull Wall block) {
        if (block.isSolid()) {
            return this;
        }
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull SlabBuilder lapply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        if (data.getType(x, y, z).isSolid()) {
            return this;
        }
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public @NotNull Slab get() {
        return blockData;
    }
}
