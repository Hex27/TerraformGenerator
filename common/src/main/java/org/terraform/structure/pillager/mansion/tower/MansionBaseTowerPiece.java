package org.terraform.structure.pillager.mansion.tower;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Random;

public class MansionBaseTowerPiece extends MansionStandardTowerPiece {

    public MansionBaseTowerPiece(MansionJigsawBuilder builder,
                                 int widthX,
                                 int height,
                                 int widthZ,
                                 JigsawType type,
                                 BlockFace[] validDirs)
    {
        super(builder, widthX, height, widthZ, type, validDirs);
    }

    @Override
    public void decorateAwkwardCorner(@NotNull Wall target,
                                      Random random,
                                      @NotNull BlockFace one,
                                      @NotNull BlockFace two)
    {
        // Fill in gap in the corner
        target.Pillar(MansionJigsawBuilder.roomHeight, Material.STONE_BRICKS);
        target.getRelative(one).getRelative(two).setType(Material.COBBLESTONE_SLAB);

        new SlabBuilder(Material.COBBLESTONE_SLAB).setType(Type.TOP)
                                                  .apply(target.getRelative(one).getRelative(two).getUp(3));

        new StairBuilder(Material.COBBLESTONE_STAIRS).setFacing(one.getOppositeFace())
                                                     .apply(target.getRelative(two).getUp(4))
                                                     .setFacing(two.getOppositeFace())
                                                     .apply(target.getRelative(one).getUp(4));

        target.getRelative(one).Pillar(4, Material.COBBLESTONE_WALL);
        target.getRelative(one).CorrectMultipleFacing(4);
        target.getRelative(two).Pillar(4, Material.COBBLESTONE_WALL);
        target.getRelative(two).CorrectMultipleFacing(4);

        target.getRelative(one).setType(Material.COBBLESTONE);
        target.getRelative(two).setType(Material.COBBLESTONE);
        if (target.getRelative(two).getDown(2).isSolid()) {
            target.getRelative(two).getDown().setType(Material.COBBLESTONE);
        }
        if (target.getRelative(one).getDown(2).isSolid()) {
            target.getRelative(one).getDown().setType(Material.COBBLESTONE);
        }

    }

}
