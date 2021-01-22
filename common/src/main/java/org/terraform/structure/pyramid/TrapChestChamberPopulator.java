package org.terraform.structure.pyramid;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class TrapChestChamberPopulator extends RoomPopulatorAbstract {

    public TrapChestChamberPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {

        //Delete all surface pressure plates in this room placed by the pyramid dungeon path populator
        //We don't want any mobs stepping on bombs here. The whole room explodes.
        int[] lowerCorner = room.getLowerCorner(1);
        int[] upperCorner = room.getUpperCorner(1);
        for(int x = lowerCorner[0]; x <= upperCorner[0]; x++)
            for(int z = lowerCorner[1]; z <= upperCorner[1]; z++)
                data.setType(x, room.getY() + 1, z, Material.AIR);

        //Classic Pyramid interior look
        SimpleBlock center = new SimpleBlock(data, room.getX(), room.getY(), room.getZ());
        center.setType(Material.BLUE_TERRACOTTA);
        for(BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            center.getRelative(face).setType(Material.ORANGE_TERRACOTTA);
            new Wall(center.getRelative(face).getRelative(face).getRelative(0, 1, 0))
                    .Pillar(room.getHeight(), rand, Material.CUT_SANDSTONE);
        }
        for(BlockFace face : BlockUtils.directBlockFaces)
            center.getRelative(face).getRelative(face)
                    .setType(Material.ORANGE_TERRACOTTA);

        center.getRelative(0, 1, 0).setType(Material.TRAPPED_CHEST);
        data.lootTableChest(center.getX(), center.getY() + 1, center.getZ(), TerraLootTable.DESERT_PYRAMID);
        center = center.getRelative(0, -1, 0);

        //Underground tnt network

        for(int nx = -1; nx <= 1; nx++) {
            for(int nz = -1; nz <= 1; nz++) {
                data.setType(nx + center.getX(), center.getY(), center.getZ() + nz, Material.REDSTONE_WIRE);
                data.setType(nx + center.getX(), center.getY() - 1, center.getZ() + nz, Material.TNT);
            }
        }
        //Ensure that center tnt is stone.
        center.getRelative(0, -1, 0).setType(Material.STONE);
    }


    @Override
    public boolean canPopulate(CubeRoom room) {
        return room.getWidthX() >= 5 && room.getWidthZ() >= 5;
    }


}
