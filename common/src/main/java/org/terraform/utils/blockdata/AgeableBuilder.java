package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class AgeableBuilder {

    private final Ageable blockData;

    public AgeableBuilder(Material mat) {
        this.blockData = (Ageable) Bukkit.createBlockData(mat);
    }

    public AgeableBuilder(Material... mat) {
        this.blockData = (Ageable) Bukkit.createBlockData(GenUtils.randMaterial(mat));
    }

    public AgeableBuilder setAge(int age) {
        this.blockData.setAge(age);
        return this;
    }

    public AgeableBuilder setFullyGrown() {
        this.blockData.setAge(this.blockData.getMaximumAge());
        return this;
    }

    public AgeableBuilder setRandomAge(Random rand) {
        this.blockData.setAge(rand.nextInt(this.blockData.getMaximumAge() + 1));
        return this;
    }

    public AgeableBuilder apply(SimpleBlock block) {
        block.setBlockData(blockData);
        return this;
    }

    public AgeableBuilder apply(Wall block) {
        block.setBlockData(blockData);
        return this;
    }

    public AgeableBuilder apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public Ageable get() {
        return blockData;
    }
}
