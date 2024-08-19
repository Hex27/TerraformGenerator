package org.terraform.structure.monument;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;

import java.util.Map.Entry;
import java.util.Random;

public class HollowPillarRoomPopulator extends CageRoomPopulator {

    public HollowPillarRoomPopulator(Random rand, MonumentDesign design, boolean forceSpawn, boolean unique) {
        super(rand, design, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);
        CubeRoom cage = new CubeRoom(
                room.getWidthX() - 8,
                room.getWidthZ() - 8,
                room.getHeight() - 9,
                room.getX(),
                room.getY() + (room.getHeight() / 2) - ((room.getHeight() - 9) / 2),
                room.getZ()
        );

        // Attach to the ceiling
        for (int[] corner : cage.getAllCorners()) {
            Wall w = new Wall(new SimpleBlock(data, corner[0], room.getY() + 1, corner[1]), BlockFace.NORTH);
            w.LPillar(room.getHeight() - 1, rand, design.tileSet());
        }

        // Lines
        for (Entry<Wall, Integer> entry : cage.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey();
            int length = entry.getValue();
            for (int i = 0; i < length; i++) {
                w.getDown().setType(design.mat(rand));
                w.getRelative(0, cage.getHeight() - 1, 0).setType(design.mat(rand));
                w = w.getLeft();
            }
        }

        // Lanterns
        for (int[] corner : cage.getAllCorners()) {
            int x = corner[0];
            int z = corner[1];
            data.setType(x, cage.getY(), z, Material.SEA_LANTERN);
            data.setType(x, cage.getY() + cage.getHeight(), z, Material.SEA_LANTERN);
        }

    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getHeight() > 13;
    }

}
