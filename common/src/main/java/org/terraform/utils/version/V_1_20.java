package org.terraform.utils.version;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.utils.BlockUtils;

import java.util.Locale;
import java.util.Random;

public class V_1_20 {
    public static final @Nullable Material PITCHER_PLANT = !Version.isAtLeast(20)
                                                           ? Material.TALL_GRASS
                                                           : Material.getMaterial("PITCHER_PLANT");
    public static final @Nullable Material CHERRY_LOG = !Version.isAtLeast(20)
                                                        ? Material.DARK_OAK_LOG
                                                        : Material.getMaterial("CHERRY_LOG");
    public static final @Nullable Material CHERRY_WOOD = !Version.isAtLeast(20)
                                                         ? Material.DARK_OAK_WOOD
                                                         : Material.getMaterial("CHERRY_WOOD");
    public static final @Nullable Material CHERRY_LEAVES = !Version.isAtLeast(20)
                                                           ? Material.DARK_OAK_LEAVES
                                                           : Material.getMaterial("CHERRY_LEAVES");
    public static final @Nullable Material CHERRY_SAPLING = !Version.isAtLeast(20)
                                                            ? Material.DARK_OAK_SAPLING
                                                            : Material.getMaterial("CHERRY_SAPLING");
    public static final @Nullable Material SUSPICIOUS_SAND = !Version.isAtLeast(20)
                                                             ? Material.SAND
                                                             : Material.getMaterial("SUSPICIOUS_SAND");
    public static final @Nullable Material SUSPICIOUS_GRAVEL = !Version.isAtLeast(20)
                                                               ? Material.GRAVEL
                                                               : Material.getMaterial("SUSPICIOUS_GRAVEL");

    public static final @Nullable EntityType CAMEL = getCamel();

    public static @NotNull BlockData getPinkPetalData(int count)
    {
        return Bukkit.createBlockData("pink_petals[flower_amount=" + count + ",facing=" + BlockUtils.getDirectBlockFace(
                new Random()).toString().toLowerCase(Locale.ENGLISH) + "]");
    }

    private static @Nullable EntityType getCamel() {
        try {
            return EntityType.valueOf("CAMEL");
        }
        catch (Exception e) {
            return null;
        }
    }
}