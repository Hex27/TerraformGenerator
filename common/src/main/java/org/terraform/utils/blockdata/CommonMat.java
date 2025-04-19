package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class CommonMat {
    public static BlockData BEDROCK = Bukkit.createBlockData(Material.BEDROCK);
    public static BlockData AIR = Bukkit.createBlockData(Material.AIR);
    public static BlockData CAVE_AIR = Bukkit.createBlockData(Material.CAVE_AIR);
    public static BlockData WATER = Bukkit.createBlockData(Material.WATER);
    public static BlockData STONE = Bukkit.createBlockData(Material.STONE);
    public static BlockData DEEPSLATE = Bukkit.createBlockData(Material.DEEPSLATE);
}
