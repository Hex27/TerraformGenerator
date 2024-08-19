package org.terraform.structure.monument;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.GenUtils;

import java.util.Map.Entry;
import java.util.Random;

public class CageRoomPopulator extends MonumentRoomPopulator {

    public CageRoomPopulator(Random rand, MonumentDesign design, boolean forceSpawn, boolean unique) {
        super(rand, design, forceSpawn, unique);
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getHeight() > 9;
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);

        // Not always a cage room
        if (GenUtils.chance(rand, 3, 5)) {
            return;
        }

        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey().getUp(7);
            int length = entry.getValue();
            for (int i = 0; i < length; i++) {
                if (i % 2 == 0) {
                    Waterlogged wall = (Waterlogged) Bukkit.createBlockData(Material.PRISMARINE_WALL);
                    wall.setWaterlogged(w.get().getY() <= TerraformGenerator.seaLevel);
                    for (int j = 0; j < room.getHeight() - 9; j++) {
                        w.getRelative(0, j, 0).setBlockData(wall);
                    }
                    // w.Pillar(room.getHeight()-9, rand, Material.PRISMARINE_WALL);
                }
                else {
                    w.Pillar(
                            room.getHeight() - 9,
                            rand,
                            Material.DARK_PRISMARINE_SLAB,
                            Material.PRISMARINE_SLAB,
                            Material.PRISMARINE_BRICK_SLAB
                    );
                    Stairs s = (Stairs) Bukkit.createBlockData(design.stairs());
                    s.setFacing(w.getDirection());
                    s.setWaterlogged(w.get().getY() <= TerraformGenerator.seaLevel);

                    w.setBlockData(s);
                    s = (Stairs) s.clone();
                    s.setHalf(Half.TOP);
                    w.getRelative(0, room.getHeight() - 9, 0).setBlockData(s);
                }
                w = w.getLeft();
            }
        }

        // Corners are dark prismarine
        for (int[] corner : room.getAllCorners()) {
            for (int i = 0; i < room.getHeight(); i++) {
                if (data.getType(corner[0], i + room.getY(), corner[1]).isSolid()) {
                    data.setType(corner[0], i + room.getY(), corner[1], Material.DARK_PRISMARINE);
                }
            }
        }
    }
}
