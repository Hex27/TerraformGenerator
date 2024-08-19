package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class DirectionalBuilder {

    private final @NotNull Directional blockData;

    public DirectionalBuilder(@NotNull Material mat) {
        this.blockData = (Directional) Bukkit.createBlockData(mat);
    }

    public DirectionalBuilder(Material... mat) {
        this.blockData = (Directional) Bukkit.createBlockData(GenUtils.randChoice(mat));
    }


    public @NotNull DirectionalBuilder setFacing(@NotNull BlockFace face) {
        this.blockData.setFacing(face);
        return this;
    }

    public @NotNull DirectionalBuilder apply(@NotNull SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull DirectionalBuilder apply(@NotNull Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull DirectionalBuilder apply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public @NotNull Directional get() {
        return blockData;
    }
}
