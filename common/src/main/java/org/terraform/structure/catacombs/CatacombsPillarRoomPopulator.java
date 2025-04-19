package org.terraform.structure.catacombs;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Random;

public class CatacombsPillarRoomPopulator extends CatacombsStandardPopulator {

    public CatacombsPillarRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }


    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);
        SimpleBlock center = room.getCenterSimpleBlock(data).getUp();
        center.LPillar(room.getHeight(), new Random(), Material.BONE_BLOCK);

        if (rand.nextBoolean()) {
            center.getUp(2).setType(Material.GOLD_BLOCK);
        }

        for (BlockFace face : BlockUtils.directBlockFaces) {
            Wall target = new Wall(center.getRelative(face), face);
            new StairBuilder(Material.STONE_BRICK_STAIRS, Material.COBBLESTONE_STAIRS).setFacing(face.getOppositeFace())
                                                                                      .apply(target);

            target.getUp(2).setType(Material.ANDESITE_WALL);
            target.getUp(2).CorrectMultipleFacing(1);
            target.getUp(3).getFront().setType(Material.ANDESITE_WALL);
            target.getUp(3).getFront().CorrectMultipleFacing(1);

            target.getUp(3).setType(Material.BONE_BLOCK);
            target.getUp(4).getFront().LPillar(room.getHeight() - 4, new Random(), Material.BONE_BLOCK);

        }

        super.spawnHangingChains(data, room);
    }

}
