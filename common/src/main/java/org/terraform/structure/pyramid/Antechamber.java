package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.V_1_20;
import org.terraform.utils.version.Version;

import java.util.Random;

public abstract class Antechamber extends RoomPopulatorAbstract {

    public Antechamber(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    /**
     * This parent function will take care of floor and ceiling decorations,
     * along with some basic wall decorations
     */
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        // Ceiling Corner Decorations
        int[][] corners = room.getAllCorners(1);
        for (int[] corner : corners) {
            Wall w = new Wall(new SimpleBlock(data, corner[0], room.getY() + room.getHeight() - 1, corner[1]));
            w.downLPillar(rand, 2, Material.CUT_SANDSTONE, Material.SMOOTH_SANDSTONE);
            for (BlockFace face : BlockUtils.directBlockFaces) {
                w.getRelative(face).setType(GenUtils.randChoice(Material.CUT_SANDSTONE, Material.SMOOTH_SANDSTONE));
            }
        }

        // Create randomised patterns
        int[] choices = {-2, -1, 0, 1, 2};
        int[] steps = new int[15];
        for (int i = 0; i < 15; i++) {
            steps[i] = choices[rand.nextInt(choices.length)];
        }

        // For the floor
        SimpleBlock center = new SimpleBlock(data, room.getX(), room.getY(), room.getZ());

        for (BlockFace face : BlockUtils.directBlockFaces) {
            int length = room.getWidthX() / 2;
            if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                length = room.getWidthZ() / 2;
            }
            for (int i = 0; i < length; i++) {
                if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                    center.getRelative(face, i)
                          .getRelative(steps[i] * face.getModZ(), 0, 0)
                          .setType(Material.ORANGE_TERRACOTTA);
                }
                else {
                    center.getRelative(face, i)
                          .getRelative(0, 0, steps[i] * face.getModX())
                          .setType(Material.ORANGE_TERRACOTTA);
                }
            }
        }
        center.setType(Material.BLUE_TERRACOTTA);

        // If at 1.20, spawn suspicious sand in the floor
        if (Version.isAtLeast(20)) {
            for (int i = 0; i < TConfig.c.STRUCTURES_PYRAMID_SUSPICIOUS_SAND_COUNT_PER_ANTECHAMBER; i++) {
                SimpleBlock target = center.getRelative(GenUtils.getSign(rand) * GenUtils.randInt(rand,
                                1,
                                room.getWidthX() / 2 - 1
                        ),
                        0,
                        GenUtils.getSign(rand) * GenUtils.randInt(rand, 1, room.getWidthZ() / 2 - 1)
                );
                target.setType(V_1_20.SUSPICIOUS_SAND);
                data.lootTableChest(target.getX(),
                        target.getY(),
                        target.getZ(),
                        TerraLootTable.DESERT_PYRAMID_ARCHAEOLOGY
                );
            }
        }

        // For the ceiling
        center = new SimpleBlock(data, room.getX(), room.getY() + room.getHeight(), room.getZ());
        for (BlockFace face : BlockUtils.directBlockFaces) {
            int length = room.getWidthX() / 2;
            if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                length = room.getWidthZ() / 2;
            }
            for (int i = 0; i < length; i++) {
                if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                    center.getRelative(face, i)
                          .getRelative(steps[i] * face.getModZ(), 0, 0)
                          .setType(Material.ORANGE_TERRACOTTA);
                }
                else {
                    center.getRelative(face, i)
                          .getRelative(0, 0, steps[i] * face.getModX())
                          .setType(Material.ORANGE_TERRACOTTA);
                }
            }
        }
        center.setType(Material.BLUE_TERRACOTTA);
    }

    protected void randomRoomPlacement(@NotNull PopulatorDataAbstract data,
                                       @NotNull CubeRoom room,
                                       int lowerbound,
                                       int upperbound,
                                       Material... types)
    {

        for (int i = 0; i < GenUtils.randInt(lowerbound, upperbound); i++) {
            int[] coords = room.randomCoords(rand, 1);
            BlockData bd = Bukkit.createBlockData(GenUtils.randChoice(types));
            if (bd instanceof Waterlogged) {
                ((Waterlogged) bd).setWaterlogged(false);
            }
            if (!data.getType(coords[0], room.getY() + 1, coords[2]).isSolid()) {
                data.setBlockData(coords[0], room.getY() + 1, coords[2], bd);
            }
            else {
                data.setBlockData(coords[0], room.getY() + 2, coords[2], bd);
            }
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}