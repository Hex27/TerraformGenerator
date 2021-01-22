package org.terraform.structure.stronghold;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class TrapChestRoomPopulator extends RoomPopulatorAbstract {

    public TrapChestRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
        //Mines
        for(int i = 0; i < GenUtils.randInt(rand, 0, 5); i++) {
            int x = room.getX() + GenUtils.randInt(rand, -room.getWidthX() / 2, room.getWidthX() / 2);
            int z = room.getZ() + GenUtils.randInt(rand, -room.getWidthZ() / 2, room.getWidthZ() / 2);
            data.setType(x, room.getY() + 1, z, Material.STONE_PRESSURE_PLATE);

            if(GenUtils.chance(rand, 4, 5))
                data.setType(x, room.getY() - 1, z, Material.TNT);
        }

        int y = room.getY() + 1;
        int x = room.getX();
        int z = room.getZ();

        if(GenUtils.chance(rand, 1, 2))
            data.setType(x, y, z, Material.TNT);
        else
            data.setType(x, y, z, Material.SMOOTH_STONE);

        SimpleBlock core = new SimpleBlock(data, x, y, z);
        for(BlockFace face : BlockUtils.directBlockFaces) {
            core.getRelative(face).setType(Material.SMOOTH_STONE);
            core.getRelative(face).getRelative(0, 1, 0).setType(Material.STONE_BRICK_STAIRS);
            Directional rot = (Directional) Bukkit.createBlockData(Material.STONE_BRICK_STAIRS);
            rot.setFacing(face.getOppositeFace());
            core.getRelative(face).getRelative(0, 1, 0).setBlockData(rot);

            for(BlockFace f : BlockUtils.directBlockFaces) {
                core.getRelative(face).getRelative(f).lsetType(Material.SMOOTH_STONE_SLAB);
            }
        }

        core.getRelative(0, 1, 0).setType(Material.CHISELED_STONE_BRICKS);

        y = core.getRelative(0, 2, 0).getY();
        org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) Bukkit.createBlockData(Material.TRAPPED_CHEST);
        chest.setFacing(BlockUtils.getDirectBlockFace(rand));
        data.setBlockData(x, y, z, chest);
        data.lootTableChest(x, y, z, TerraLootTable.STRONGHOLD_CROSSING);

    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return !room.isBig();
    }
}
