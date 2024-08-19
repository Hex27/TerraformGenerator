package org.terraform.structure.stronghold;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class StairwayRoomPopulator extends RoomPopulatorAbstract {

    public StairwayRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        int bfIndex = 0;
        for (int i = 1; i < room.getHeight(); i++) {
            data.setType(room.getX(), room.getY() + i, room.getZ(), BlockUtils.stoneBrick(rand));

            // Two slab stairs

            BlockFace face = BlockUtils.xzPlaneBlockFaces[bfIndex];
            Slab bottom = (Slab) Bukkit.createBlockData(BlockUtils.stoneBrickSlab(rand));
            bottom.setType(Type.BOTTOM);
            data.setBlockData(room.getX() + face.getModX(), room.getY() + i, room.getZ() + face.getModZ(), bottom);

            bfIndex = getNextIndex(bfIndex);

            face = BlockUtils.xzPlaneBlockFaces[bfIndex];
            Slab top = (Slab) Bukkit.createBlockData(BlockUtils.stoneBrickSlab(rand));
            top.setType(Type.TOP);
            data.setBlockData(room.getX() + face.getModX(), room.getY() + i, room.getZ() + face.getModZ(), top);
            bfIndex = getNextIndex(bfIndex);
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return false;
    }


}
