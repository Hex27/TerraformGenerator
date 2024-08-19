package org.terraform.structure.monument;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class LevelledRoomPopulator extends CageRoomPopulator {

    public LevelledRoomPopulator(Random rand, MonumentDesign design, boolean forceSpawn, boolean unique) {
        super(rand, design, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);
        int[] lowerBounds = room.getLowerCorner();
        int[] upperBounds = room.getUpperCorner();

        // Create a floor to the above area
        for (int x = lowerBounds[0] + 1; x <= upperBounds[0] - 1; x++) {
            for (int z = lowerBounds[1] + 1; z <= upperBounds[1] - 1; z++) {
                data.setType(x, room.getY() + 4, z, Material.PRISMARINE_BRICKS);

                // Surrounding decor
                if (Math.abs(x - room.getX()) <= 2 && Math.abs(z - room.getZ()) <= 2) {
                    data.setType(x, room.getY() + 4, z, Material.DARK_PRISMARINE);

                    // Central area
                    if (Math.abs(x - room.getX()) <= 1 && Math.abs(z - room.getZ()) <= 1) {
                        data.setType(x, room.getY() + 4, z, Material.WATER);
                    }
                }
            }
        }

        // Some prismarine at the bottom
        Wall cent = new Wall(new SimpleBlock(data, room.getX(), room.getY() + 1, room.getZ()), BlockFace.NORTH);
        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            cent.getUp().getRelative(face).getRelative(face).Pillar(2, rand, Material.DARK_PRISMARINE);
            cent.getRelative(face).setType(Material.DARK_PRISMARINE);
        }

        // Lighting and pillars on lvl 2
        for (int[] corner : room.getAllCorners(1)) {
            for (int y = room.getY() + 5; y < room.getY() + room.getHeight(); y++) {
                if (y == room.getY() + 5 || y == room.getY() + room.getHeight() - 1) {
                    data.setType(corner[0], y, corner[1], Material.DARK_PRISMARINE);
                }
                else if (y % 2 == 0) {
                    Waterlogged wall = (Waterlogged) Bukkit.createBlockData(Material.PRISMARINE_WALL);
                    wall.setWaterlogged(y <= TerraformGenerator.seaLevel);
                    data.setBlockData(corner[0], y, corner[1], wall);
                }
                else {
                    data.setType(corner[0], y, corner[1], Material.SEA_LANTERN);
                }
            }
        }

        // Decorative pillars on level 1.
        for (int[] corner : room.getAllCorners(2)) {
            data.setType(corner[0], room.getY() + 3, corner[1], Material.SEA_LANTERN);
            data.setType(corner[0], room.getY() + 2, corner[1], Material.PRISMARINE_BRICKS);
        }
        for (int[] corner : room.getAllCorners(3)) {
            data.setType(corner[0], room.getY() + 1, corner[1], Material.PRISMARINE_BRICKS);
        }
    }
}