package org.terraform.utils.version;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.Biome;
import org.bukkit.block.data.type.PointedDripstone;

/**
 * You're almost certainly going to have to drop old version support in 26.2 to
 * accommodate API breakages.
 */
public class V_26_2 {
    public static Material CINNABAR = Version.VERSION.isAtLeast(Version.v26_2) ?
                                      Material.valueOf("CINNABAR") : Material.RED_TERRACOTTA;
    public static Material POTENT_SULFUR = Version.VERSION.isAtLeast(Version.v26_2) ?
                                    Material.valueOf("POTENT_SULFUR") : Material.YELLOW_TERRACOTTA;
    public static Material SULFUR = Version.VERSION.isAtLeast(Version.v26_2) ?
                                    Material.valueOf("SULFUR") : Material.DRIPSTONE_BLOCK;
    public static Material SULFUR_SPIKE = Version.VERSION.isAtLeast(Version.v26_2) ?
                                    Material.valueOf("SULFUR_SPIKE") : Material.POINTED_DRIPSTONE;
    public static BlockData SULFUR_SPIKE_DATA = Version.VERSION.isAtLeast(Version.v26_2) ?
                                                Bukkit.createBlockData(SULFUR_SPIKE) : Bukkit.createBlockData(Material.POINTED_DRIPSTONE);
    public static Biome SULFUR_CAVES = Version.VERSION.isAtLeast(Version.v26_2) ?
                                       Biome.valueOf("SULFUR_CAVES") : Biome.DRIPSTONE_CAVES;

    //Bukkit is very likely going to get rid of the PointedDripstone interface in favour
    // of some spike abstraction
    public static void setSulfurSpike(BlockFace vertical_direction, PointedDripstone.Thickness thickness){

    }
}
