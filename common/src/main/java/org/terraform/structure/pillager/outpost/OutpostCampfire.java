package org.terraform.structure.pillager.outpost;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class OutpostCampfire extends RoomPopulatorAbstract {

    public OutpostCampfire(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        SimpleBlock core = new SimpleBlock(data, room.getX(), room.getY(), room.getZ()).getGroundOrSeaLevel();

        BlockUtils.replaceCircularPatch(rand.nextInt(12322),
                3f,
                core,
                Material.COAL_ORE,
                Material.STONE,
                Material.COARSE_DIRT,
                Material.COARSE_DIRT,
                Material.COARSE_DIRT,
                Material.COARSE_DIRT
        );

        core = core.getUp();
        unitCampfire(core);
        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            unitCampfire(core.getRelative(face).getGround().getUp());
        }
    }

    private void unitCampfire(@NotNull SimpleBlock block) {
        switch (rand.nextInt(3)) {
            case 0:
                block.setType(Material.CAMPFIRE);
                break;
            case 1:
                block.setType(Material.CAMPFIRE);
                block.getDown().setType(Material.HAY_BLOCK);
                break;
            case 2:
                block.setType(Material.HAY_BLOCK);
                block.getUp().setType(Material.CAMPFIRE);
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    SimpleBlock target = block.getRelative(face).getGround().getUp();
                    if (!target.isSolid()) {
                        target.setType(Material.CAMPFIRE);
                    }
                }
                break;
        }
    }


    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}