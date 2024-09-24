package org.terraform.structure.monument;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class LanternPillarRoomPopulator extends MonumentRoomPopulator {

    public LanternPillarRoomPopulator(Random rand, MonumentDesign design, boolean forceSpawn, boolean unique) {
        super(rand, design, forceSpawn, unique);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);

        int y = room.getY() + room.getHeight() - 1;

        // Pyramid interior look
        for (int i = 0; i < 5; i++) {
            int[] upperBounds = room.getUpperCorner(1);
            int[] lowerBounds = room.getLowerCorner(1);
            // TerraformGeneratorPlugin.logger.info(lowerBounds[0]+"->"+upperBounds[0]+","+i);

            // Solid fill
            for (int x = lowerBounds[0]; x <= upperBounds[0]; x++) {
                for (int z = lowerBounds[1]; z <= upperBounds[1]; z++) {
                    data.setType(x, y - i, z, design.mat(rand));
                }
            }

            upperBounds = room.getUpperCorner(1 + 5 - i);
            lowerBounds = room.getLowerCorner(1 + 5 - i);

            // Hollow out
            for (int x = lowerBounds[0]; x <= upperBounds[0]; x++) {
                for (int z = lowerBounds[1]; z <= upperBounds[1]; z++) {
                    data.setType(x, y - i, z, Material.WATER);
                }
            }
        }

        // Pillar for the center//
        Wall w = new Wall(new SimpleBlock(data, room.getX(), room.getY() + 1, room.getZ()), BlockFace.NORTH);
        w.LPillar(room.getHeight(), rand, Material.SEA_LANTERN);

        // Diagonals
        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            w.getRelative(face)
             .LPillar(room.getHeight(), true, rand, Material.DARK_PRISMARINE, Material.PRISMARINE_WALL);
        }

        // Direct faces
        for (BlockFace face : BlockUtils.directBlockFaces) {
            w.getRelative(face).LPillar(room.getHeight(), true, rand, Material.PRISMARINE_WALL, Material.WATER);
            for (int i = 0; i < room.getHeight() - 2; i++) {
                BlockUtils.correctSurroundingMultifacingData(w.getRelative(face).getRelative(0, i, 0).get());
            }
        }


        // Base
        for (int nx = -2; nx <= 2; nx++) {
            for (int nz = -2; nz <= 2; nz++) {
                w.getRelative(nx, 0, nz).setType(design.mat(rand));
            }
        }
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getHeight() > 12;
    }

}
