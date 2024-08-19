package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class MultipleFacingBuilder {

    private final @NotNull MultipleFacing blockData;

    public MultipleFacingBuilder(@NotNull Material mat) {
        this.blockData = (MultipleFacing) Bukkit.createBlockData(mat);
    }

    public MultipleFacingBuilder(Material... mat) {
        this.blockData = (MultipleFacing) Bukkit.createBlockData(GenUtils.randChoice(mat));
    }


    public @NotNull MultipleFacingBuilder setFace(@NotNull BlockFace face, boolean isEnabled) {
        this.blockData.setFace(face, isEnabled);
        return this;
    }

    public @NotNull MultipleFacingBuilder apply(@NotNull SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull MultipleFacingBuilder apply(@NotNull Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull MultipleFacingBuilder apply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public @NotNull MultipleFacing get() {
        return blockData;
    }
}
