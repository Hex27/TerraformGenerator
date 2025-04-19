package org.terraform.structure.ancientcity;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Candle;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class AncientCitySchematicParser extends SchematicParser {
    @Override
    public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
        Random rand = new Random();
        if (data.getMaterial() == Material.DEEPSLATE_TILES) { // Crack deepslate tiles
            if (rand.nextBoolean()) {
                data = Bukkit.createBlockData(Material.CRACKED_DEEPSLATE_TILES);
            }
        }
        else if (data.getMaterial() == Material.DEEPSLATE_BRICKS) { // Crack deepslate bricks
            if (rand.nextBoolean()) {
                data = Bukkit.createBlockData(Material.CRACKED_DEEPSLATE_BRICKS);
            }
        }
        else if (data.getMaterial() == Material.DARK_OAK_PLANKS
                 || data.getMaterial() == Material.DARK_OAK_SLAB)
        { // Rot some wood away
            if (rand.nextBoolean()) {
                data = Bukkit.createBlockData(Material.AIR);
            }
        }
        else if (data.getMaterial() == Material.CANDLE) {
            Candle candle = (Candle) Bukkit.createBlockData(Material.CANDLE);
            candle.setLit(true);

            candle.setCandles(1 + rand.nextInt(4));
            data = candle;
        }
        else if (data.getMaterial() == Material.CHEST) { // Populate chests
            if (GenUtils.chance(rand, 2, 5)) {
                block.setType(Material.AIR);
                return; // 2 fifths of chests are not placed.
            }
            super.applyData(block, data);
            block.getPopData().lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.ANCIENT_CITY);
            return; // do not apply data again.
        }
        super.applyData(block, data);
    }
}