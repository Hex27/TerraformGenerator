package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class AgeableBuilder {

    private final @NotNull Ageable blockData;

    public AgeableBuilder(@NotNull Material mat) {
        this.blockData = (Ageable) Bukkit.createBlockData(mat);
    }

    public AgeableBuilder(Material... mat) {
        this.blockData = (Ageable) Bukkit.createBlockData(GenUtils.randChoice(mat));
    }

    public @NotNull AgeableBuilder setAge(int age) {
        this.blockData.setAge(age);
        return this;
    }

    public @NotNull AgeableBuilder setFullyGrown() {
        this.blockData.setAge(this.blockData.getMaximumAge());
        return this;
    }

    public @NotNull AgeableBuilder setRandomAge(@NotNull Random rand) {
        this.blockData.setAge(rand.nextInt(this.blockData.getMaximumAge() + 1));
        return this;
    }

    public @NotNull AgeableBuilder apply(@NotNull SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull AgeableBuilder apply(@NotNull Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public @NotNull AgeableBuilder apply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public @NotNull Ageable get() {
        return blockData;
    }
}
