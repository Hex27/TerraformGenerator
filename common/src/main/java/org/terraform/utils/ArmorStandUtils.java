package org.terraform.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;

import java.util.Random;

public class ArmorStandUtils {

    public static void placeArmorStand(@NotNull SimpleBlock target, @NotNull BlockFace facing, @NotNull Random rand) {
        placeArmorStand(target, facing, ArmorStandQuality.rollQuality(rand));
    }

    public static void placeArmorStand(@NotNull SimpleBlock target,
                                       @NotNull BlockFace facing,
                                       @NotNull ArmorStandQuality quality)
    {
        if (target.getPopData() instanceof PopulatorDataPostGen postGen) {
            ArmorStand stand = (ArmorStand) postGen.getWorld().spawnEntity(new Location(postGen.getWorld(),

                    target.getX() + 0.5f, target.getY(), target.getZ() + 0.5f
            ), EntityType.ARMOR_STAND);
            stand.setRotation(BlockUtils.yawFromBlockFace(facing), 0);
            quality.apply(stand);
        }
    }

    public enum ArmorStandQuality {
        LEATHER(
                Material.LEATHER_HELMET,
                Material.LEATHER_CHESTPLATE,
                Material.LEATHER_LEGGINGS,
                Material.LEATHER_BOOTS
        ),
        IRON(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS),
        GOLD(Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS),
        DIAMOND(Material.DIAMOND_HELMET,
                Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS,
                Material.DIAMOND_BOOTS
        );
        private final Material[] pieces;

        ArmorStandQuality(Material... pieces) {
            this.pieces = pieces;
        }

        public static @NotNull ArmorStandQuality rollQuality(@NotNull Random rand) {
            int weight = rand.nextInt(100);
            if (weight > 95) // 15%
            {
                return ArmorStandQuality.DIAMOND;
            }
            else if (weight > 60) // 35%
            {
                return ArmorStandQuality.IRON;
            }
            else if (weight > 30) // 30%
            {
                return ArmorStandQuality.GOLD;
            }
            else // 30%
            {
                return ArmorStandQuality.LEATHER;
            }
        }

        public void apply(@NotNull ArmorStand entity) {
            entity.getEquipment().setHelmet(new ItemStack(pieces[0]));
            entity.getEquipment().setChestplate(new ItemStack(pieces[1]));
            entity.getEquipment().setLeggings(new ItemStack(pieces[2]));
            entity.getEquipment().setBoots(new ItemStack(pieces[3]));
        }
    }
}
