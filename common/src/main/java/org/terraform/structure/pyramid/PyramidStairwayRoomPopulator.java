package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class PyramidStairwayRoomPopulator extends RoomPopulatorAbstract {

    public PyramidStairwayRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        int bfIndex = 0;
        for (int i = 1; i < room.getHeight(); i++) {
            data.setType(room.getX(), room.getY() + i, room.getZ(), Material.CHISELED_SANDSTONE);

            // Two slab stairs

            BlockFace face = BlockUtils.xzPlaneBlockFaces[bfIndex];
            Slab bottom = (Slab) Bukkit.createBlockData(Material.SANDSTONE_SLAB);
            bottom.setType(Type.BOTTOM);
            data.setBlockData(room.getX() + face.getModX(), room.getY() + i, room.getZ() + face.getModZ(), bottom);

            bfIndex = getNextIndex(bfIndex);

            face = BlockUtils.xzPlaneBlockFaces[bfIndex];
            Slab top = (Slab) Bukkit.createBlockData(Material.SANDSTONE_SLAB);
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
