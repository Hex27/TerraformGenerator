package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;

import java.util.Random;

public class AgeableBuilder {

    private final @NotNull Ageable blockData;

    public AgeableBuilder(@NotNull Material mat) {
        this.blockData = (Ageable) Bukkit.createBlockData(mat);
    }

    public @NotNull AgeableBuilder setRandomAge(@NotNull Random rand) {
        this.blockData.setAge(rand.nextInt(this.blockData.getMaximumAge() + 1));
        return this;
    }

    public @NotNull AgeableBuilder apply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

}
