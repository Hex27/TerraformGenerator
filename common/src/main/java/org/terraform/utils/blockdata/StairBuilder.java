package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;

public class StairBuilder {

    @NotNull
    final ArrayList<SimpleBlock> placed = new ArrayList<>();
    private final @NotNull Stairs blockData;

    public StairBuilder(@NotNull Material mat) {
        this.blockData = (Stairs) Bukkit.createBlockData(mat);
    }


    public StairBuilder(Material... mat) {
        this.blockData = (Stairs) Bukkit.createBlockData(GenUtils.randChoice(mat));
    }

    public @NotNull StairBuilder setFacing(@NotNull BlockFace face) {
        this.blockData.setFacing(face);
        return this;
    }

    public @NotNull StairBuilder setHalf(Bisected.@NotNull Half half) {
        this.blockData.setHalf(half);
        return this;
    }

    public @NotNull StairBuilder setShape(Stairs.@NotNull Shape shape) {
        this.blockData.setShape(shape);
        return this;
    }

    public @NotNull StairBuilder setWaterlogged(boolean bool) {
        this.blockData.setWaterlogged(bool);
        return this;
    }

    public @NotNull StairBuilder apply(@NotNull SimpleBlock block) {
        block.setBlockData(blockData);
        placed.add(block);
        return this;
    }

    public @NotNull StairBuilder lapply(@NotNull SimpleBlock block) {
        if (block.isSolid()) {
            return this;
        }

        block.setBlockData(blockData);
        placed.add(block);
        return this;
    }

    public @NotNull StairBuilder apply(@NotNull Wall block) {
        block.setBlockData(blockData);
        placed.add(block.get());
        return this;
    }

    public @NotNull StairBuilder apply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        placed.add(new SimpleBlock(data, x, y, z));
        return this;
    }

    public @NotNull StairBuilder correct() {
        for (SimpleBlock b : placed) {
            BlockUtils.correctSurroundingStairData(b);
        }
        return this;
    }

    public @NotNull Stairs get() {
        return blockData;
    }
}
