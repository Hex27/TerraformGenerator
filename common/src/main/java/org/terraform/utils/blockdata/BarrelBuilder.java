package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.main.config.TConfig;

import java.util.Random;

public class BarrelBuilder {
    private final @NotNull BlockData blockData;
    private TerraLootTable lootTable;

    public BarrelBuilder() {
        this.blockData = Bukkit.createBlockData(Material.BARREL);
    }

    public @NotNull BarrelBuilder setLootTable(TerraLootTable @NotNull ... loottable) {
        this.lootTable = loottable[new Random().nextInt(loottable.length)];
        return this;
    }

    public @NotNull BarrelBuilder setFacing(@NotNull BlockFace face) {
        ((Directional) blockData).setFacing(face);
        return this;
    }

    public @NotNull BarrelBuilder apply(@NotNull SimpleBlock block) {
        //Don't place barrels if decorations are off
        if(!TConfig.areDecorationsEnabled()) return this;
        block.setBlockData(blockData);
        if (lootTable != null) {
            block.getPopData().lootTableChest(block.getX(), block.getY(), block.getZ(), lootTable);
        }
        return this;
    }

}
