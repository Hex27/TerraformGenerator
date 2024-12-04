package org.terraform.structure.warmoceanruins;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.V_1_20;
import org.terraform.utils.version.Version;

import java.util.Random;

/**
 * Handles spawning suspicious sand and anchoring structures to the ground
 * by setting CubeRoom.y
 */
public abstract class WarmOceanBaseRoom extends RoomPopulatorAbstract {
    public WarmOceanBaseRoom(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    // Partially sink the structure into the ground.
    // Set some suspicious sand around too
    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        room.setY(GenUtils.getHighestGround(data, room.getX(), room.getZ()) + 1
                // - rand.nextInt(2)
        );
        // Spawn suspicious sand relative to room size
        // Sprinkle some magma blocks too
        for (int i = 0; i < (room.getWidthX() * room.getWidthZ()) / 70; i++) {
            int[] coords = room.randomCoords(rand);
            coords[1] = GenUtils.getHighestGround(data, coords[0], coords[2]);
            if (data.getType(coords[0], coords[1], coords[2]) == Material.SAND
                || data.getType(coords[0], coords[1], coords[2]) == Material.GRAVEL)
            {
                if (Version.isAtLeast(20) && GenUtils.chance(rand, 3, 4)) {
                    if (data.getType(coords[0], coords[1], coords[2]) == Material.SAND) {
                        data.setType(coords[0], coords[1], coords[2], V_1_20.SUSPICIOUS_SAND);
                        data.lootTableChest(coords[0],
                                coords[1],
                                coords[2],
                                TerraLootTable.OCEAN_RUIN_WARM_ARCHAEOLOGY
                        );
                    }
                    else {
                        data.setType(coords[0], coords[1], coords[2], V_1_20.SUSPICIOUS_GRAVEL);
                        data.lootTableChest(coords[0],
                                coords[1],
                                coords[2],
                                TerraLootTable.OCEAN_RUIN_COLD_ARCHAEOLOGY
                        );
                    }
                }
            }
            else {
                data.setType(coords[0], coords[1], coords[2], Material.MAGMA_BLOCK);
            }
        }
    }

}
