package org.terraform.structure.catacombs;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class CatacombsStairwayBasePopulator extends CatacombsStandardPopulator {

    public CatacombsStairwayBasePopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }


    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);
        SimpleBlock center = room.getCenterSimpleBlock(data).getUp();

        BlockFace stairFace = BlockFace.NORTH;
        for (int relY = 0; relY <= 14; relY++) {
            SimpleBlock target = center.getUp(relY);
            for (BlockFace face : BlockUtils.flatBlockFaces3x3) {
                target.getRelative(face).setType(Material.AIR);
                if (face == stairFace) {
                    if (relY < 8) {
                        target.getRelative(face)
                              .downUntilSolid(new Random(), Material.ANDESITE, Material.COBBLESTONE, Material.STONE);
                    }
                    else if (target.hasAdjacentSolid(BlockUtils.directBlockFaces)) {
                        target.getRelative(face).setType(Material.ANDESITE, Material.COBBLESTONE, Material.STONE);
                    }
                    else {
                        break;
                    }
                }
            }

            stairFace = BlockUtils.rotateXZPlaneBlockFace(stairFace, 1);
        }
    }

}
