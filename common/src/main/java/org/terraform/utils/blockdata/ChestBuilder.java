package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class ChestBuilder {
    private final @NotNull Chest blockData;
    private TerraLootTable lootTable;

    public ChestBuilder(@NotNull Material mat) {
        this.blockData = (Chest) Bukkit.createBlockData(mat);
    }

    public ChestBuilder(Material... mat) {
        this.blockData = (Chest) Bukkit.createBlockData(GenUtils.randChoice(mat));
    }

    public @NotNull ChestBuilder setLootTable(TerraLootTable @NotNull ... loottable) {
        this.lootTable = loottable[new Random().nextInt(loottable.length)];
        return this;
    }

    public @NotNull ChestBuilder setWaterlogged(boolean waterlogged)
    {
        this.blockData.setWaterlogged(waterlogged);
        return this;
    }

    public @NotNull ChestBuilder setFacing(@NotNull BlockFace face) {
        this.blockData.setFacing(face);
        return this;
    }

    public @NotNull ChestBuilder apply(@NotNull SimpleBlock block) {
        //Don't place chests if decorations are off
        if(!TConfig.areDecorationsEnabled()) return this;
        block.setBlockData(blockData);
        if (lootTable != null) {
            block.getPopData().lootTableChest(block.getX(), block.getY(), block.getZ(), lootTable);
        }
        return this;
    }

    public void extend(@NotNull SimpleBlock original, @NotNull SimpleBlock extended, boolean lootTableExtendedChest) {
        extended.setBlockData(blockData);
        Wall originalWall = new Wall(original, blockData.getFacing());
        Chest originalChest, extendedChest;
        try {
            originalChest = (Chest) original.getBlockData();
            extendedChest = (Chest) extended.getBlockData();
        }
        catch (ClassCastException e) {
            original.setBlockData(blockData);
            extended.setBlockData(blockData);
            originalChest = (Chest) blockData.clone(); // original.getBlockData();
            extendedChest = (Chest) blockData.clone(); // original.getBlockData();
        }


        if (originalWall.getLeft().equals(extended)) {
            originalChest.setType(Type.LEFT);
            extendedChest.setType(Type.RIGHT);
        }
        else if (originalWall.getRight().equals(extended)) {
            originalChest.setType(Type.RIGHT);
            extendedChest.setType(Type.LEFT);
        }
        else {
            throw new IllegalArgumentException(
                    "A request to extend a doublechest was made, but an invalid location was specified.");
        }

        original.setBlockData(originalChest);
        extended.setBlockData(extendedChest);

        if (lootTable != null) {
            original.getPopData().lootTableChest(original.getX(), original.getY(), original.getZ(), lootTable);
        }
        if (lootTableExtendedChest && lootTable != null) {
            extended.getPopData().lootTableChest(extended.getX(), extended.getY(), extended.getZ(), lootTable);
        }
    }

    public @NotNull ChestBuilder apply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        if (lootTable != null) {
            data.lootTableChest(x, y, z, lootTable);
        }
        return this;
    }

    public @NotNull Chest get() {
        return blockData;
    }
}
