package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Chest;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class GenericAntechamber extends Antechamber {

    public GenericAntechamber(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
        // TODO Auto-generated constructor stub
    }

    /***
     * This room will contain the Pharoah's old collected possessions.
     * It can contain random items from anywhere
     */
    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);
        for (int i = 0; i < GenUtils.randInt(2, 5); i++) {
            int[] coords = room.randomCoords(rand, 2);
            data.setType(coords[0], room.getY() + 1, coords[2], GenUtils.randChoice(Material.CHISELED_SANDSTONE,
                    Material.CHISELED_SANDSTONE,
                    Material.CHISELED_SANDSTONE,
                    Material.BONE_BLOCK
            ));
        }

        // Flower pots
        randomRoomPlacement(data, room, 0, 5, Material.FLOWER_POT);

        // Dead corals
        Material[] deadCorals = {
                Material.DEAD_BRAIN_CORAL,
                Material.DEAD_BUBBLE_CORAL,
                Material.DEAD_FIRE_CORAL,
                Material.DEAD_HORN_CORAL,
                Material.DEAD_TUBE_CORAL,
                Material.DEAD_BRAIN_CORAL_FAN,
                Material.DEAD_BUBBLE_CORAL_FAN,
                Material.DEAD_FIRE_CORAL_FAN,
                Material.DEAD_HORN_CORAL_FAN,
                Material.DEAD_TUBE_CORAL_FAN
        };

        if (GenUtils.chance(rand, 1, 2)) {
            randomRoomPlacement(data, room, 1, 5, deadCorals);
        }

        // Animal items
        if (GenUtils.chance(rand, 1, 3)) {
            randomRoomPlacement(data, room, 1, 1, Material.TURTLE_EGG);
        }

        // Logs
        if (GenUtils.chance(rand, 1, 2)) {
            randomRoomPlacement(
                    data,
                    room,
                    1,
                    5,
                    Material.ACACIA_LOG,
                    Material.BIRCH_LOG,
                    Material.DARK_OAK_LOG,
                    Material.JUNGLE_LOG,
                    Material.SPRUCE_LOG,
                    Material.OAK_LOG
            );
        }

        // Rare, monster head
        if (GenUtils.chance(1, 3)) {
            randomRoomPlacement(data,
                    room,
                    1,
                    1,
                    Material.CREEPER_HEAD,
                    Material.BIRCH_LOG,
                    Material.DARK_OAK_LOG,
                    Material.JUNGLE_LOG,
                    Material.SPRUCE_LOG,
                    Material.OAK_LOG
            );
        }

        // Oceanic Treasure Chest
        if (GenUtils.chance(1, 10)) {
            int[] coords = room.randomCoords(rand, 2);
            if (!data.getType(coords[0], room.getY() + 1, coords[2]).isSolid()) {
                Chest chest = (Chest) Bukkit.createBlockData(Material.CHEST);
                chest.setFacing(BlockUtils.getDirectBlockFace(rand));
                data.setBlockData(coords[0], room.getY() + 1, coords[2], chest);
                data.lootTableChest(coords[0], room.getY() + 1, coords[2], TerraLootTable.BURIED_TREASURE);
            }
        }
    }

}
