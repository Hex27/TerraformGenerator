package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class ChestBuilder {
    private final Chest blockData;
    private TerraLootTable lootTable;

    public ChestBuilder(Material mat) {
        this.blockData = (Chest) Bukkit.createBlockData(mat);
    }

    public ChestBuilder(Material... mat) {
        this.blockData = (Chest) Bukkit.createBlockData(GenUtils.randMaterial(mat));
    }

    public ChestBuilder setLootTable(TerraLootTable loottable) {
        this.lootTable = loottable;
        return this;
    }

    public ChestBuilder setFacing(BlockFace face) {
        this.blockData.setFacing(face);
        return this;
    }

    public void apply(SimpleBlock block) {
        block.setBlockData(blockData);
        if (lootTable != null)
            block.getPopData().lootTableChest(block.getX(), block.getY(), block.getZ(), lootTable);
    }

    public void apply(Wall block) {
        block.setBlockData(blockData);
        if (lootTable != null)
            block.get().getPopData().lootTableChest(block.getX(), block.getY(), block.getZ(), lootTable);
    }

    public void apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        if (lootTable != null)
            data.lootTableChest(x, y, z, lootTable);
    }

    public Chest get() {
        return blockData;
    }
}
