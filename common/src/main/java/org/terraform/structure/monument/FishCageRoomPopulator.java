package org.terraform.structure.monument;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.GenUtils;

import java.util.Map.Entry;
import java.util.Random;

public class FishCageRoomPopulator extends LevelledRoomPopulator {

    public FishCageRoomPopulator(Random rand, MonumentDesign design, boolean forceSpawn, boolean unique) {
        super(rand, design, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);
        // Fish Cage
        CubeRoom cage = new CubeRoom(
                room.getWidthX() - 6,
                room.getWidthZ() - 6,
                room.getHeight() - 11,
                room.getX(),
                room.getY() + 7,
                room.getZ()
        );
        for (Entry<Wall, Integer> entry : cage.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey();
            int length = entry.getValue();
            for (int i = 0; i < length; i++) {
                if (i % 2 == 0) {
                    w.Pillar(cage.getHeight(), rand, Material.PRISMARINE_WALL);
                }
                else {
                    w.getFront().Pillar(cage.getHeight(), rand, Material.PRISMARINE_WALL);
                }
                Stairs stair = (Stairs) Bukkit.createBlockData(design.stairs());
                stair.setFacing(w.getDirection());
                w.getRelative(0, cage.getHeight(), 0).setBlockData(stair);
                w = w.getLeft();
            }
        }


        int[] cageLowerBounds = cage.getLowerCorner();
        int[] cageUpperBounds = cage.getUpperCorner();
        for (int x = cageLowerBounds[0]; x <= cageUpperBounds[0]; x++) {
            for (int z = cageLowerBounds[1]; z <= cageUpperBounds[1]; z++) {
                data.setType(x, cage.getY(), z, design.mat(rand));
                data.setType(x, cage.getY() + cage.getHeight(), z, design.mat(rand));
            }
        }

        // Attach to the ceiling
        for (int[] corner : cage.getAllCorners()) {
            new Wall(
                    new SimpleBlock(data, corner[0], cage.getY() + cage.getHeight() + 1, corner[1]),
                    BlockFace.NORTH
            ).Pillar(room.getHeight() - 8 - cage.getHeight(), rand, Material.PRISMARINE_WALL);
        }

        // Dolphins
        for (int i = 0; i < GenUtils.randInt(3, 6); i++) {
            data.addEntity(cage.getX(), cage.getY() + 1, cage.getZ(), EntityType.DOLPHIN);
        }
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getHeight() > 13;
    }
}
