package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import java.util.Random;

public class BarrelBuilder {
    private final BlockData blockData;
    private TerraLootTable lootTable;

    public BarrelBuilder() {
        this.blockData = Bukkit.createBlockData(Material.BARREL);
    }

    public BarrelBuilder setLootTable(TerraLootTable... loottable) {
        this.lootTable = loottable[new Random().nextInt(loottable.length)];
        return this;
    }

    public BarrelBuilder setFacing(BlockFace face) {
        ((Directional) blockData).setFacing(face);
        return this;
    }

    public BarrelBuilder apply(SimpleBlock block) {
        block.setBlockData(blockData);
        if (lootTable != null)
            block.getPopData().lootTableChest(block.getX(), block.getY(), block.getZ(), lootTable);
        return this;
    }
    
    public BarrelBuilder apply(PopulatorDataAbstract data, int x, int y, int z) {
        data.setBlockData(x, y, z, blockData);
        if (lootTable != null)
            data.lootTableChest(x, y, z, lootTable);
        return this;
    }

    public BlockData get() {
        return blockData;
    }
}
