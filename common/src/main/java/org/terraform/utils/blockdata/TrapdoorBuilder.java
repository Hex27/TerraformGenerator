package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.TrapDoor;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class TrapdoorBuilder {

    private final @NotNull TrapDoor blockData;

    public TrapdoorBuilder(@NotNull Material mat) {
        this.blockData = (TrapDoor) Bukkit.createBlockData(mat);
    }

    public TrapdoorBuilder(Material... mat) {
        this.blockData = (TrapDoor) Bukkit.createBlockData(GenUtils.randChoice(mat));
    }


    public @NotNull TrapdoorBuilder setWaterlogged(boolean water) {
        this.blockData.setWaterlogged(water);
        return this;
    }

    public @NotNull TrapdoorBuilder setHalf(@NotNull Half h) {
        this.blockData.setHalf(h);
        return this;
    }

    public @NotNull TrapdoorBuilder setPowered(boolean powered) {
        this.blockData.setPowered(powered);
        return this;
    }

    public @NotNull TrapdoorBuilder setOpen(boolean open) {
        this.blockData.setOpen(open);
        return this;
    }

    public @NotNull TrapdoorBuilder setFacing(@NotNull BlockFace face) {
        this.blockData.setFacing(face);
        return this;
    }

    public @NotNull TrapdoorBuilder apply(@NotNull SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull TrapdoorBuilder apply(@NotNull Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull TrapdoorBuilder lapply(@NotNull Wall block) {
        if (!block.isSolid()) {
            block.setBlockData(blockData);
        }
        return this;
    }

    public @NotNull TrapdoorBuilder apply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public @NotNull TrapDoor get() {
        return blockData;
    }
}
