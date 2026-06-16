package org.terraform.utils.version;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.Biome;
import org.bukkit.block.data.type.PointedDripstone;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;

/**
 * You're almost certainly going to have to drop old version support in 26.2 to
 * accommodate API breakages.
 */
public class V_26_2 {
    public static Material CINNABAR = Version.VERSION.isAtLeast(Version.v26_2) ?
                                      Material.valueOf("CINNABAR") : Material.RED_TERRACOTTA;
    public static Material POTENT_SULFUR = Version.VERSION.isAtLeast(Version.v26_2) ?
                                    Material.valueOf("POTENT_SULFUR") : Material.YELLOW_TERRACOTTA;
    public static BlockData WET_POTENT_SULFUR = Version.VERSION.isAtLeast(Version.v26_2) ?
                                                Bukkit.createBlockData("minecraft:potent_sulfur[potent_sulfur_state=wet]") : Bukkit.createBlockData(Material.YELLOW_TERRACOTTA);

    public static Material SULFUR = Version.VERSION.isAtLeast(Version.v26_2) ?
                                    Material.valueOf("SULFUR") : Material.DRIPSTONE_BLOCK;
    public static Material SULFUR_SPIKE = Version.VERSION.isAtLeast(Version.v26_2) ?
                                    Material.valueOf("SULFUR_SPIKE") : Material.POINTED_DRIPSTONE;
    public static Biome SULFUR_CAVES = Version.VERSION.isAtLeast(Version.v26_2) ?
                                       Biome.valueOf("SULFUR_CAVES") : Biome.DRIPSTONE_CAVES;




    public static void upLPointedSulfurSpike(int height, @NotNull SimpleBlock base) {
        if (!TConfig.areDecorationsEnabled()) {
            return;
        }

        int realHeight = 0;
        while (!base.getRelative(0, realHeight, 0).isSolid() && height > 0) {
            realHeight++;
            height--;
        }
        if (base.getRelative(0, realHeight, 0).isSolid()) {
            realHeight--;
        }

        if (realHeight <= 0) {
            return;
        }

        for (int i = 0; i < realHeight; i++) {
            PointedDripstone.Thickness thickness = PointedDripstone.Thickness.MIDDLE;

            if (realHeight >= 4) {
                if (i == realHeight - 1) {
                    thickness = PointedDripstone.Thickness.TIP;
                }
                if (i == realHeight - 2) {
                    thickness = PointedDripstone.Thickness.FRUSTUM;
                }
                if (i == 0) {
                    thickness = PointedDripstone.Thickness.BASE;
                }
            }
            else if (realHeight >= 3) {
                if (i == realHeight - 1) {
                    thickness = PointedDripstone.Thickness.TIP;
                }
                if (i == realHeight - 2) {
                    thickness = PointedDripstone.Thickness.FRUSTUM;
                }
                if (i == 0) {
                    thickness = PointedDripstone.Thickness.BASE;
                }
            }
            else if (realHeight >= 2) {
                thickness = PointedDripstone.Thickness.TIP;
                if (i == 0) {
                    thickness = PointedDripstone.Thickness.FRUSTUM;
                }
            }
            else {
                thickness = PointedDripstone.Thickness.TIP;
            }

            PointedDripstone dripstone = (PointedDripstone) Bukkit.createBlockData(Material.POINTED_DRIPSTONE);
            dripstone.setVerticalDirection(BlockFace.UP);
            dripstone.setThickness(thickness);
            base.getRelative(0, i, 0).setBlockData(swapOutPointedDripstone(dripstone));
        }
    }


    public static void downLPointedSulfurSpike(int height, @NotNull SimpleBlock base) {
        if (!TConfig.areDecorationsEnabled()) {
            return;
        }

        int realHeight = 0;
        while (!base.getRelative(0, -realHeight, 0).isSolid() && height > 0) {
            realHeight++;
            height--;
        }
        if (base.getRelative(0, -realHeight, 0).isSolid()) {
            realHeight--;
        }

        if (realHeight <= 0) {
            return;
        }

        for (int i = realHeight; i > 0; i--) {
            PointedDripstone.Thickness thickness = PointedDripstone.Thickness.MIDDLE;
            if (i == 1) {
                thickness = PointedDripstone.Thickness.TIP;
            }
            if (i == 2) {
                thickness = PointedDripstone.Thickness.FRUSTUM;
            }
            if (i == realHeight && realHeight > 2) {
                thickness = PointedDripstone.Thickness.BASE;
            }

            PointedDripstone dripstone = (PointedDripstone) Bukkit.createBlockData(Material.POINTED_DRIPSTONE);
            dripstone.setVerticalDirection(BlockFace.DOWN);
            dripstone.setThickness(thickness);
            base.getRelative(0, -(realHeight - i), 0).setBlockData(swapOutPointedDripstone(dripstone));
        }
    }


    /**
     * This is the fucking worst, but Spigot has no sulfur spike API in their javadocs yet.
     * @param in some pointed_dripstone block data
     * @return The same data but replaced with sulfur_spike
     */
    private static BlockData swapOutPointedDripstone(BlockData in){
        //27 is the length of "minecraft:pointed_dripstone"
        return Bukkit.createBlockData("minecraft:sulfur_spike" + in.getAsString().substring(27));
    }
}
