package org.terraform.structure.pyramid;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MainEntrancePopulator extends RoomPopulatorAbstract {

    private final BlockFace entranceFace;

    public MainEntrancePopulator(Random rand, boolean forceSpawn, boolean unique, BlockFace entranceFace) {
        super(rand, forceSpawn, unique);
        this.entranceFace = entranceFace;
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {

        int entranceHeightOffsetFromBase = room.getHeight() - 5;
        // Make the entrance pyramid shaped
        int[] upperRoomCorner = room.getUpperCorner();
        int[] lowerRoomCorner = room.getLowerCorner();
        for (int h = 0; h <= 6; h++) {
            int[] upperCorner = room.getUpperCorner(-(6 - h));
            int[] lowerCorner = room.getLowerCorner(-(6 - h));
            for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
                for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                    // Don't place blocks on the inside of the room
                    if ((x > lowerRoomCorner[0]
                         && x < upperRoomCorner[0]
                         && z > lowerRoomCorner[1]
                         && z < upperRoomCorner[1]) || h == 6)
                    {
                        continue;
                    }

                    if (h == 2) {
                        data.setType(x,
                                room.getY() + entranceHeightOffsetFromBase + 2,
                                z,
                                Material.CHISELED_RED_SANDSTONE
                        );
                    }
                    else {
                        data.setType(x,
                                room.getY() + entranceHeightOffsetFromBase + h,
                                z,
                                GenUtils.randChoice(Material.SANDSTONE, Material.SMOOTH_SANDSTONE)
                        );
                    }

                    // Down until solid to cover potential gaps
                    if (h == 0) {
                        BlockUtils.setDownUntilSolid(
                                x,
                                room.getY() + entranceHeightOffsetFromBase - 1,
                                z,
                                data,
                                Material.SANDSTONE
                        );
                    }

                    // Sand Wall Corner Decor
                    if ((x == lowerCorner[0] + 1 || x == upperCorner[0] - 1) && (z == lowerCorner[1] + 1
                                                                                 || z == upperCorner[1] - 1))
                    {
                        if (data.getType(x, room.getY() + entranceHeightOffsetFromBase + h + 1, z) == Material.AIR) {
                            data.setType(
                                    x,
                                    room.getY() + entranceHeightOffsetFromBase + h + 1,
                                    z,
                                    Material.SANDSTONE_WALL
                            );
                        }
                    }
                }
            }
        }

        // Ceiling Decor
        SimpleBlock b = new SimpleBlock(data, room.getX(), room.getY() + room.getHeight(), room.getZ());
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                b.getRelative(nx, 0, nz).setType(Material.ORANGE_TERRACOTTA);
                b.getRelative(nx, 1, nz).setType(Material.CUT_RED_SANDSTONE);
            }
        }
        for (int nx = -2; nx <= 2; nx += 2) {
            for (int nz = -2; nz <= 2; nz += 2) {
                b.getRelative(nx, 0, nz).setType(Material.ORANGE_TERRACOTTA);
            }
        }

        // Carve entrance
        Wall w = new Wall(
                new SimpleBlock(data, room.getX(), room.getY() + entranceHeightOffsetFromBase + 1, room.getZ()),
                entranceFace.getOppositeFace()
        );
        w = w.getFront(3);
        for (int depth = 0; depth <= 6; depth++) {
            w = w.getFront(1);
            w.Pillar(4, rand, Material.AIR);
            w.getLeft().Pillar(3, rand, Material.AIR);
            w.getRight().Pillar(3, rand, Material.AIR);
        }

        // Pillar Stairs
        for (int h = entranceHeightOffsetFromBase; h > 0; h--) {
            w = new Wall(new SimpleBlock(data, room.getX(), room.getY() + h, room.getZ()),
                    BlockUtils.rotateFace(entranceFace.getOppositeFace(), entranceHeightOffsetFromBase - h)
            );

            if (h > 3) {
                for (int i = 1; i <= 3; i++) {
                    w.getFront(i).setType(Material.CUT_SANDSTONE);
                }
            }
            else {
                w.getFront().downUntilSolid(new Random(), Material.CUT_SANDSTONE);
            }

        }

        // Lava fountain
        data.setType(room.getX(), room.getY(), room.getZ(), Material.AIR);
        data.setType(room.getX(), room.getY() + room.getHeight(), room.getZ(), Material.LAVA);

    }


    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() >= 5 && room.getWidthZ() >= 5;
    }
}
