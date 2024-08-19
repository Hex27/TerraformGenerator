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

public class PyramidStairwayTopPopulator extends RoomPopulatorAbstract {

    public PyramidStairwayTopPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        int bfIndex = 6;

        // Open up the floor
        for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
            data.setType(room.getX() + face.getModX(), room.getY(), room.getZ() + face.getModZ(), Material.CAVE_AIR);
        }

        // Have a pillar from the center to the ceiling
        for (int i = 0; i < room.getHeight(); i++) {
            data.setType(room.getX(), room.getY() + i, room.getZ(), Material.CHISELED_SANDSTONE);
        }

        // Connect to the bottom stairs.
        BlockFace face = BlockUtils.xzPlaneBlockFaces[bfIndex];
        Slab bottom = (Slab) Bukkit.createBlockData(Material.SANDSTONE_SLAB);
        bottom.setType(Type.BOTTOM);
        data.setBlockData(room.getX() + face.getModX(), room.getY(), room.getZ() + face.getModZ(), bottom);

        bfIndex = getNextIndex(bfIndex);

        face = BlockUtils.xzPlaneBlockFaces[bfIndex];
        Slab top = (Slab) Bukkit.createBlockData(Material.SANDSTONE_SLAB);
        top.setType(Type.TOP);
        data.setBlockData(room.getX() + face.getModX(), room.getY(), room.getZ() + face.getModZ(), top);
        bfIndex = getNextIndex(bfIndex);

        face = BlockUtils.xzPlaneBlockFaces[bfIndex];
        data.setBlockData(room.getX() + face.getModX(), room.getY(), room.getZ() + face.getModZ(), top);
        bfIndex = getNextIndex(bfIndex);

        face = BlockUtils.xzPlaneBlockFaces[bfIndex];
        data.setBlockData(room.getX() + face.getModX(), room.getY(), room.getZ() + face.getModZ(), top);
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return false;
    }
}
