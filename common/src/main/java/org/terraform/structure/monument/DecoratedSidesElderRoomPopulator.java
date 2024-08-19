package org.terraform.structure.monument;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;

import java.util.Map.Entry;
import java.util.Random;

public class DecoratedSidesElderRoomPopulator extends MonumentRoomPopulator {

    public DecoratedSidesElderRoomPopulator(Random rand, MonumentDesign design, boolean forceSpawn, boolean unique) {
        super(rand, design, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);

        // Stairs at the top
        for (Entry<Wall, Integer> walls : room.getFourWalls(data, 2).entrySet()) {
            Wall w = walls.getKey().getRelative(0, room.getHeight() - 2, 0);
            int length = walls.getValue();
            for (int j = 0; j < length; j++) {

                Stairs stair = (Stairs) Bukkit.createBlockData(design.stairs());
                stair.setFacing(w.getDirection().getOppositeFace());
                stair.setWaterlogged(true);
                stair.setHalf(Half.TOP);
                w.setBlockData(stair);

                w = w.getLeft();
            }
        }

        // Decorated walls
        for (Entry<Wall, Integer> walls : room.getFourWalls(data, 1).entrySet()) {
            Wall w = walls.getKey();
            int length = walls.getValue();
            for (int j = 0; j < length; j++) {
                if (!w.getRear().isSolid()) {
                    Wall wall = w.getUp(4);
                    wall.LPillar(room.getHeight() - 4, true, rand, Material.SEA_LANTERN, Material.DARK_PRISMARINE);
                }
                else {
                    if (j % 2 == 0) {
                        w.LPillar(room.getHeight() - 1, rand, Material.PRISMARINE_BRICKS);
                    }
                    else {
                        w.LPillar(room.getHeight() - 1, rand, Material.PRISMARINE);
                        w.getUp(3).Pillar(4, rand, Material.SEA_LANTERN);
                    }
                    w.setType(Material.DARK_PRISMARINE);
                    w.getRelative(0, room.getHeight() - 2, 0).setType(Material.DARK_PRISMARINE);
                }
                w = w.getLeft();
            }
        }

        // Elder
        data.addEntity(room.getX(), room.getY() + room.getHeight() / 2, room.getZ(), EntityType.ELDER_GUARDIAN);

        // Corners are sea lanterns
        Waterlogged wall = (Waterlogged) Bukkit.createBlockData(Material.PRISMARINE_WALL);
        wall.setWaterlogged(true);

        for (int[] corner : room.getAllCorners(2)) {
            data.setType(corner[0], room.getY() + room.getHeight() - 1, corner[1], Material.SEA_LANTERN);
            data.setBlockData(corner[0], room.getY() + room.getHeight() - 2, corner[1], wall);
        }
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getHeight() > 7;
    }
}
