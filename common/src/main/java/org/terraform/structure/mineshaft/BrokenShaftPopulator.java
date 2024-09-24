package org.terraform.structure.mineshaft;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class BrokenShaftPopulator extends RoomPopulatorAbstract {
    public BrokenShaftPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        int[] lowerCorner = room.getLowerCorner(3);
        int[] upperCorner = room.getUpperCorner(3);

        // Spawn a solid stone platform
        int y = room.getY();
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                SimpleBlock b = new SimpleBlock(data, x, y, z);
                if (b.getType() == Material.CAVE_AIR
                    || b.getType() == Material.OAK_PLANKS
                    || b.getType() == Material.OAK_SLAB
                    || b.getType() == Material.GRAVEL)
                {
                    b.setType(GenUtils.randChoice(
                            Material.STONE_BRICKS,
                            Material.CRACKED_STONE_BRICKS,
                            Material.MOSSY_STONE_BRICKS,
                            Material.MOSSY_COBBLESTONE,
                            Material.COBBLESTONE,
                            Material.CAVE_AIR
                    ));
                }
            }
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return false;
    }
}
