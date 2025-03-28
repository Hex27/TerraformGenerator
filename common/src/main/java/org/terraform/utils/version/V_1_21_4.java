package org.terraform.utils.version;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;

public class V_1_21_4 {
    public static Biome PALE_GARDEN = Version.isAtLeast(21.4) ? Biome.valueOf("PALE_GARDEN") : Biome.DARK_FOREST;
    public static BlockData PALE_HANGING_MOSS = Version.isAtLeast(21.4) ?
                                                Bukkit.createBlockData("minecraft:pale_hanging_moss[tip=false]")
                                                                        : Bukkit.createBlockData(Material.VINE);
    public static BlockData PALE_HANGING_MOSS_TIP = Version.isAtLeast(21.4) ?
                                                Bukkit.createBlockData("minecraft:pale_hanging_moss[tip=true]")
                                                                        : Bukkit.createBlockData(Material.VINE);
    public static Material PALE_MOSS = Version.isAtLeast(21.4) ?
                                               Material.valueOf("PALE_MOSS_BLOCK") : Material.MOSS_BLOCK;
    public static Material PALE_OAK_LOG = Version.isAtLeast(21.4) ?
                                          Material.valueOf("PALE_OAK_LOG") : Material.DARK_OAK_LOG;
    public static Material PALE_OAK_WOOD = Version.isAtLeast(21.4) ?
                                          Material.valueOf("PALE_OAK_WOOD") : Material.DARK_OAK_WOOD;
    public static Material PALE_OAK_LEAVES = Version.isAtLeast(21.4) ?
                                             Material.valueOf("PALE_OAK_LEAVES") : Material.DARK_OAK_LEAVES;
    public static Material CLOSED_EYEBLOSSOM = Version.isAtLeast(21.4) ?
                                               Material.valueOf("CLOSED_EYEBLOSSOM") : Material.POPPY;

    //this is so fucking stupid
    public static BlockData CREAKING_HEART = Version.isAtLeast(21.4) ?
                                             (
                                                 Version.isAtLeast(21.5) ?
                                                 Bukkit.createBlockData("minecraft:creaking_heart[creaking_heart_state=awake,natural=true]")
                                                 : Bukkit.createBlockData("minecraft:creaking_heart[active=true,natural=true]")
                                             )
                                             : Bukkit.createBlockData(Material.DARK_OAK_WOOD);
    public static Material PALE_MOSS_CARPET = Version.isAtLeast(21.4) ?
                                            Material.valueOf("PALE_MOSS_CARPET") : Material.MOSS_CARPET;
}
