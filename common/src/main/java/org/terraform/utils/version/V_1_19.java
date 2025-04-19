package org.terraform.utils.version;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class V_1_19 {

    // public static final EntityType FROG = getEntityType("FROG", "FISH");

    public static final EntityType ALLAY = getEntityType();

    public static final @NotNull Material MUD = Objects.requireNonNull(!Version.isAtLeast(19) ? Material.getMaterial(
            "PODZOL") : Material.getMaterial("MUD"));

    public static final @Nullable Material MUD_BRICKS = !Version.isAtLeast(19)
                                                        ? Material.getMaterial("BRICKS")
                                                        : Material.getMaterial("MUD_BRICKS");

    public static final @Nullable Material REINFORCED_DEEPSLATE = !Version.isAtLeast(19) ? Material.getMaterial(
            "POLISHED_DIORITE") : Material.getMaterial("REINFORCED_DEEPSLATE");


    public static final @Nullable Material MANGROVE_LEAVES = !Version.isAtLeast(19)
                                                             ? Material.getMaterial("OAK_LEAVES")
                                                             : Material.getMaterial("MANGROVE_LEAVES");
    public static final @Nullable Material MANGROVE_LOG = !Version.isAtLeast(19)
                                                          ? Material.getMaterial("OAK_LOG")
                                                          : Material.getMaterial("MANGROVE_LOG");
    public static final @Nullable Material MANGROVE_WOOD = !Version.isAtLeast(19)
                                                           ? Material.getMaterial("OAK_WOOD")
                                                           : Material.getMaterial("MANGROVE_WOOD");
    public static final @Nullable Material MANGROVE_PROPAGULE = !Version.isAtLeast(19)
                                                                ? Material.getMaterial("AIR")
                                                                : Material.getMaterial("MANGROVE_PROPAGULE");
    public static final @Nullable Material MANGROVE_ROOTS = !Version.isAtLeast(19)
                                                            ? Material.getMaterial("OAK_WOOD")
                                                            : Material.getMaterial("MANGROVE_ROOTS");
    public static final @Nullable Material MUDDY_MANGROVE_ROOTS = !Version.isAtLeast(19) ? Material.getMaterial(
            "OAK_WOOD") : Material.getMaterial("MUDDY_MANGROVE_ROOTS");

    public static final @Nullable Material MANGROVE_FENCE = !Version.isAtLeast(19)
                                                            ? Material.getMaterial("OAK_FENCE")
                                                            : Material.getMaterial("MANGROVE_FENCE");

    // Pre-1.19 versions WILL NOT have any sculk related spawns.
    public static final @Nullable Material SCULK_VEIN = Material.getMaterial("SCULK_VEIN");
    public static final @Nullable Material SCULK = !Version.isAtLeast(19) ? Material.getMaterial(
            "STONE") : Material.getMaterial("SCULK");
    public static final @Nullable Material SCULK_CATALYST = Material.getMaterial("SCULK_CATALYST");
    public static final @Nullable Material SCULK_SHRIEKER = Material.getMaterial("SCULK_SHRIEKER");
    public static final @Nullable Material SCULK_SENSOR = Material.getMaterial("SCULK_SENSOR");


    public static final Biome MANGROVE_SWAMP = getBiome("MANGROVE_SWAMP", "SWAMP");
    public static final Biome DEEP_DARK = getBiome("DEEP_DARK", "PLAINS");
    private static final String shriekerDataString = "minecraft:sculk_shrieker[can_summon=true,shrieking=false,waterlogged=false]";
    private static final String propaguleDataString = "minecraft:mangrove_propagule[hanging=true,age=4,waterlogged=false]";

    private static @NotNull Biome getBiome(String name, String fallback) {
        try {
            return Biome.valueOf(name);
        }
        catch (IllegalArgumentException e) {
            return Biome.valueOf(fallback);
        }
    }

    private static @NotNull EntityType getEntityType() {
        try {
            return EntityType.valueOf("ALLAY");
        }
        catch (IllegalArgumentException e) {
            return EntityType.valueOf("CHICKEN");
        }
    }

    public static @NotNull BlockData getActiveSculkShrieker() {
        return Bukkit.createBlockData(shriekerDataString);
    }

    public static @NotNull BlockData getHangingMangrovePropagule() {
        return Bukkit.createBlockData(propaguleDataString);
    }
}
