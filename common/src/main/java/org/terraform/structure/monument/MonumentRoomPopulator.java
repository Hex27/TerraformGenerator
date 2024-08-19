package org.terraform.structure.monument;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Map.Entry;
import java.util.Random;

public class MonumentRoomPopulator extends RoomPopulatorAbstract {
    final MonumentDesign design;

    public MonumentRoomPopulator(Random rand, MonumentDesign design, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
        this.design = design;
    }

    protected static void setThickPillar(@NotNull Random rand,
                                         @NotNull MonumentDesign design,
                                         @NotNull SimpleBlock base)
    {
        Wall w = new Wall(base, BlockFace.NORTH);
        w.downUntilSolid(rand, Material.PRISMARINE);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            switch (design) {
                case DARK_LIGHTLESS:
                    w.getRelative(face).downUntilSolid(rand, Material.PRISMARINE_BRICKS);
                    break;
                case DARK_PRISMARINE_CORNERS:
                case PRISMARINE_LANTERNS:
                    w.getRelative(face).downUntilSolid(rand, Material.PRISMARINE_BRICKS, Material.SEA_LANTERN);
                    break;
            }
        }
        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            switch (design) {
                case DARK_LIGHTLESS:
                case DARK_PRISMARINE_CORNERS:
                    w.getRelative(face).downUntilSolid(rand, Material.DARK_PRISMARINE);
                    break;
                case PRISMARINE_LANTERNS:
                    w.getRelative(face).downUntilSolid(rand, Material.PRISMARINE_BRICKS);
                    break;
            }
        }
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        int[] upperBounds = room.getUpperCorner();
        int[] lowerBounds = room.getLowerCorner();

        // Fill with water
        for (int x = lowerBounds[0] + 1; x <= upperBounds[0] - 1; x++) {
            for (int z = lowerBounds[1] + 1; z <= upperBounds[1] - 1; z++) {
                for (int y = room.getY() + 1; y < room.getY() + room.getHeight(); y++) {
                    data.setType(x, y, z, Material.WATER);
                }
            }
        }

        // Don't bother with tiny rooms
        if (room.getHeight() < 7) {
            return;
        }

        // Corners are dark prismarine
        for (int[] corner : room.getAllCorners()) {
            for (int i = 1; i < room.getHeight(); i++) {
                if (data.getType(corner[0], i + room.getY(), corner[1]).isSolid()) {
                    data.setType(corner[0], i + room.getY(), corner[1], Material.DARK_PRISMARINE);
                }
            }
        }

        // Stairs at the top
        for (Entry<Wall, Integer> walls : room.getFourWalls(data, 0).entrySet()) {
            Wall w = walls.getKey().getRelative(0, room.getHeight() - 1, 0);
            int length = walls.getValue();
            for (int j = 0; j < length; j++) {
                if (!w.getUp().isSolid()) {
                    Stairs stair = (Stairs) Bukkit.createBlockData(design.stairs());
                    stair.setFacing(w.getDirection());
                    if (w.get().getY() < TerraformGenerator.seaLevel) {
                        stair.setWaterlogged(true);
                    }
                    w.setBlockData(stair);
                }
                // Wall decor
                if (j == length / 2) {
                    if (room.getHeight() >= 16 && room.getWidthX() >= 10 && room.getWidthZ() >= 10) {

                        MonumentWallPattern.values()[rand.nextInt(MonumentWallPattern.values().length)].apply(w.getDown(
                                4));
                    }
                }
                w = w.getLeft();
            }
        }

        // Sea lanterns
        for (int[] corner : room.getAllCorners()) {
            if (!data.getType(corner[0], room.getY() + room.getHeight() + 1, corner[1]).isSolid()) {
                data.setType(corner[0], room.getY() + room.getHeight(), corner[1], Material.SEA_LANTERN);
            }
        }


        // Spawn some designs on top if the top center is clear.
        if (!data.getType(room.getX(), room.getY() + room.getHeight() + 1, room.getZ()).isSolid()) {
            int i = GenUtils.randInt(1, 3);
            if (i == 1) {
                // Spires with dome thing
                for (int[] pos : room.getAllCorners(1)) {
                    int x = pos[0];
                    int z = pos[1];
                    design.spire(new Wall(
                            new SimpleBlock(data, x, room.getY() + room.getHeight() + 1, z),
                            BlockFace.NORTH
                    ), rand);
                }

                MonumentPopulator.arch(new Wall(
                        new SimpleBlock(data, room.getX(), room.getY() + room.getHeight(), room.getZ()),
                        BlockFace.NORTH
                ), design, rand, (room.getWidthX() - 4) / 2, 6);
                MonumentPopulator.arch(new Wall(
                        new SimpleBlock(data, room.getX(), room.getY() + room.getHeight(), room.getZ()),
                        BlockFace.EAST
                ), design, rand, (room.getWidthX() - 4) / 2, 6);

            }
            else if (i == 2) {
                // Some abraham lincoln architecture thingy
                for (Entry<Wall, Integer> walls : room.getFourWalls(data, 1).entrySet()) {
                    Wall w = walls.getKey().getRelative(0, room.getHeight(), 0);
                    int length = walls.getValue();
                    for (int j = 0; j < length; j++) {
                        if (j % 2 == 0) {
                            w.RPillar(4, rand, Material.PRISMARINE_WALL);
                        }
                        w.getUp(4).setType(design.slab());
                        w = w.getLeft();
                    }
                }
                for (int x = lowerBounds[0] + 2; x <= upperBounds[0] - 2; x++) {
                    for (int z = lowerBounds[1] + 2; z <= upperBounds[1] - 2; z++) {
                        data.setType(x, room.getY() + room.getHeight() + 5, z, design.mat(rand));
                    }
                }
            }
            else if (i == 3) {
                // Large Lamp
                for (Entry<Wall, Integer> walls : room.getFourWalls(data, 1).entrySet()) {
                    Wall w = walls.getKey().getRelative(0, room.getHeight(), 0);
                    int length = walls.getValue();
                    for (int j = 0; j < length; j++) {
                        if (j % 2 == 0) {
                            w.setType(design.mat(rand));
                            w.getUp().setType(Material.PRISMARINE_WALL);
                            w.getUp(2).setType(design.slab());
                        }
                        else {
                            w.setType(design.slab());
                        }
                        w = w.getLeft();
                    }
                }

                design.spawnLargeLight(data, room.getX(), room.getY() + room.getHeight() + 1, room.getZ());

            }
        }
        // Underneath the monument, spawn pillars down.
        setThickPillar(rand, design, new SimpleBlock(data, lowerBounds[0] + 1, room.getY() - 1, lowerBounds[1] + 1));
        setThickPillar(rand, design, new SimpleBlock(data, upperBounds[0] - 1, room.getY() - 1, lowerBounds[1]));
        setThickPillar(rand, design, new SimpleBlock(data, upperBounds[0] - 1, room.getY() - 1, upperBounds[1] - 1));
        setThickPillar(rand, design, new SimpleBlock(data, lowerBounds[0], room.getY() - 1, upperBounds[1] - 1));
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }


}
