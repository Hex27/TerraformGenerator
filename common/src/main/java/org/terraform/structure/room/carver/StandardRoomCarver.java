package org.terraform.structure.room.carver;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.GenUtils;

/**
 * This one will carve a cube room with the specified wall material
 */
public class StandardRoomCarver extends RoomCarver {
    final int tile;
    final Material fillMat;

    public StandardRoomCarver(int tile, Material fillMat) {
        this.tile = tile;
        this.fillMat = fillMat;
    }

    @Override
    public void carveRoom(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room, Material @NotNull ... mat) {
        // 30/1/2024 God you really were a shit programmer what the fuck is this
        int tileIndex = 0;
        if (mat[0] != Material.BARRIER)
        // Create a solid block with the specified width
        {
            for (int nx = room.getX() - room.getWidthX() / 2; nx <= room.getX() + room.getWidthX() / 2; nx++) {
                for (int ny = room.getY(); ny <= room.getY() + room.getHeight(); ny++) {
                    for (int nz = room.getZ() - room.getWidthZ() / 2; nz <= room.getZ() + room.getWidthZ() / 2; nz++) {
                        if (data.getType(nx, ny, nz) == Material.CAVE_AIR) {
                            continue;
                        }
                        if (tile == -1) {
                            data.setType(nx, ny, nz, GenUtils.randChoice(mat));
                        }
                        else {
                            data.setType(
                                    nx,
                                    ny,
                                    nz,
                                    mat[(Math.abs(nz + room.getWidthZ() / 2 + ny + nx + room.getWidthX() / 2
                                                  - tileIndex)) % mat.length]
                            );
                            tileIndex += 1;
                            if (tileIndex == 2) {
                                tileIndex = 0;
                            }
                        }
                    }
                }
            }
        }

        // Hollow out the room
        for (int nx = room.getX() - room.getWidthX() / 2 + 1; nx <= room.getX() + room.getWidthX() / 2 - 1; nx++) {
            for (int ny = room.getY() + 1; ny <= room.getY() + room.getHeight() - 1; ny++) {
                for (int nz = room.getZ() - room.getWidthZ() / 2 + 1;
                     nz <= room.getZ() + room.getWidthZ() / 2 - 1;
                     nz++) {
                    data.setType(nx, ny, nz, fillMat);
                }
            }
        }
    }
}
