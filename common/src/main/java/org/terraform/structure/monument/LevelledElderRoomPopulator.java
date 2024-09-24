package org.terraform.structure.monument;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;

import java.util.Map.Entry;
import java.util.Random;

public class LevelledElderRoomPopulator extends LevelledRoomPopulator {

    public LevelledElderRoomPopulator(Random rand, MonumentDesign design, boolean forceSpawn, boolean unique) {
        super(rand, design, forceSpawn, unique);
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getHeight() > 12;
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);

        // Decorated walls
        for (Entry<Wall, Integer> walls : room.getFourWalls(data, 1).entrySet()) {
            Wall w = walls.getKey().getUp(4);
            int length = walls.getValue();
            for (int j = 0; j < length; j++) {
                if (j % 2 == 0) {
                    w.LPillar(room.getHeight() - 1, rand, Material.PRISMARINE_BRICKS);
                }
                else {
                    w.LPillar(room.getHeight() - 1, rand, Material.PRISMARINE);
                    w.getUp(3).Pillar(4, rand, Material.SEA_LANTERN);
                }
                w.setType(Material.DARK_PRISMARINE);
                // w.getRelative(0,room.getHeight()-2-4,0).setType(Material.DARK_PRISMARINE);

                w = w.getLeft();
            }
        }


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

        // Elder
        data.addEntity(room.getX() + 3, room.getY() + 8, room.getZ() - 3, EntityType.ELDER_GUARDIAN);
    }
}
