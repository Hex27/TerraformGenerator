package org.terraform.structure.stronghold;

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

public class StairwayTopPopulator extends RoomPopulatorAbstract {

    public StairwayTopPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        // Open up the floor
        for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
            data.setType(room.getX() + face.getModX(), room.getY(), room.getZ() + face.getModZ(), Material.CAVE_AIR);
        }

        // Have a pillar from the center to the ceiling
        for (int i = 0; i < room.getHeight(); i++) {
            data.setType(room.getX(), room.getY() + i, room.getZ(), BlockUtils.stoneBrick(rand));
        }

        // Connect to the bottom stairs.
        int bfIndex = 2;
        BlockFace face = BlockUtils.xzPlaneBlockFaces[bfIndex];
        Slab bottom = (Slab) Bukkit.createBlockData(BlockUtils.stoneBrickSlab(rand));
        bottom.setType(Type.BOTTOM);
        data.setBlockData(room.getX() + face.getModX(), room.getY(), room.getZ() + face.getModZ(), bottom);

        bfIndex = getNextIndex(bfIndex);

        face = BlockUtils.xzPlaneBlockFaces[bfIndex];
        Slab top = (Slab) Bukkit.createBlockData(BlockUtils.stoneBrickSlab(rand));
        top.setType(Type.TOP);
        data.setBlockData(room.getX() + face.getModX(), room.getY(), room.getZ() + face.getModZ(), top);
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return false;
    }
}
