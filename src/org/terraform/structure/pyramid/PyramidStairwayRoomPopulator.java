package org.terraform.structure.pyramid;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

public class PyramidStairwayRoomPopulator extends RoomPopulatorAbstract {

    public PyramidStairwayRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
        int bfIndex = 0;
        for (int i = 1; i < room.getHeight(); i++) {
            data.setType(room.getX(), room.getY() + i, room.getZ(), Material.CHISELED_SANDSTONE);

            //Two slab stairs

            BlockFace face = BlockUtils.xzPlaneBlockFaces.get(bfIndex);
            Slab bottom = (Slab) Bukkit.createBlockData(Material.SANDSTONE_SLAB);
            bottom.setType(Type.BOTTOM);
            data.setBlockData(room.getX() + face.getModX(), room.getY() + i, room.getZ() + face.getModZ(), bottom);

            bfIndex = getNextIndex(bfIndex);

            face = BlockUtils.xzPlaneBlockFaces.get(bfIndex);
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
