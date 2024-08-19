package org.terraform.structure.stronghold;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SupplyRoomPopulator extends RoomPopulatorAbstract {

    public SupplyRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        int[] upperBounds = room.getUpperCorner();
        int[] lowerBounds = room.getLowerCorner();
        int y = room.getY();

        // Spawn torches
        for (int i = 0; i < GenUtils.randInt(rand, 1, 4); i++) {
            int x = GenUtils.randInt(rand, lowerBounds[0] + 1, upperBounds[0] - 1);
            int z = GenUtils.randInt(rand, lowerBounds[1] + 1, upperBounds[1] - 1);
            int ny = y + 1;
            while (data.getType(x, ny, z).isSolid() && ny < room.getHeight() + room.getY()) {
                ny++;
            }
            if (ny == room.getHeight() + room.getY()) {
                continue;
            }
            data.setType(x, ny, z, Material.TORCH);
        }

        // Spawn piles of supply blocks.
        for (int i = 0; i < GenUtils.randInt(rand, 1, 3); i++) {
            int x = GenUtils.randInt(rand, lowerBounds[0] + 1, upperBounds[0] - 1);
            int z = GenUtils.randInt(rand, lowerBounds[1] + 1, upperBounds[1] - 1);
            BlockUtils.replaceUpperSphere(rand.nextInt(992),
                    GenUtils.randInt(rand, 1, 3),
                    GenUtils.randInt(rand, 1, 3),
                    GenUtils.randInt(rand, 1, 3),
                    new SimpleBlock(data, x, y, z),
                    false,
                    GenUtils.randChoice(rand,
                            Material.IRON_ORE,
                            Material.HAY_BLOCK,
                            Material.CHISELED_STONE_BRICKS,
                            Material.COAL_BLOCK,
                            Material.COAL_ORE
                    )
            );
        }

        // Spawn utilities
        for (int i = 0; i < GenUtils.randInt(rand, 5, 20); i++) {
            int x = GenUtils.randInt(rand, lowerBounds[0] + 1, upperBounds[0] - 1);
            int z = GenUtils.randInt(rand, lowerBounds[1] + 1, upperBounds[1] - 1);
            int ny = y + 1;
            while (data.getType(x, ny, z).isSolid() && ny < room.getHeight() + room.getY()) {
                ny++;
            }
            if (ny == room.getHeight() + room.getY()) {
                continue;
            }

            Material type = GenUtils.randChoice(rand,
                    Material.CRAFTING_TABLE,
                    Material.ANVIL,
                    Material.CAULDRON,
                    Material.FLETCHING_TABLE,
                    Material.SMITHING_TABLE,
                    Material.CARTOGRAPHY_TABLE,
                    Material.BARREL,
                    Material.OAK_LOG
            );
            BlockData typeData = Bukkit.createBlockData(type);

            if (typeData instanceof Rotatable) {
                ((Rotatable) typeData).setRotation(BlockUtils.getDirectBlockFace(rand));
            }
            else if (typeData instanceof Directional) {
                ((Directional) typeData).setFacing(BlockUtils.getDirectBlockFace(rand));
            }
            else if (typeData instanceof Orientable) {
                ((Orientable) typeData).setAxis(Axis.values()[GenUtils.randInt(rand, 0, 2)]);
            }
            data.setBlockData(x, ny, z, typeData);
        }

        // Spawn loot chests
        for (int i = 0; i < GenUtils.randInt(rand, 5, 20); i++) {
            int x = GenUtils.randInt(rand, lowerBounds[0] + 1, upperBounds[0] - 1);
            int z = GenUtils.randInt(rand, lowerBounds[1] + 1, upperBounds[1] - 1);
            int ny = y + 1;
            while (data.getType(x, ny, z).isSolid() && ny < room.getHeight() + room.getY()) {
                ny++;
            }
            if (ny == room.getHeight() + room.getY()) {
                continue;
            }

            data.setType(x, ny, z, Material.CHEST);
            org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) Bukkit.createBlockData(Material.CHEST);
            chest.setFacing(BlockUtils.getDirectBlockFace(rand));
            data.setBlockData(x, ny, z, chest);
            data.lootTableChest(x, ny, z, TerraLootTable.STRONGHOLD_CROSSING);
        }


    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return !room.isBig();
    }


}
