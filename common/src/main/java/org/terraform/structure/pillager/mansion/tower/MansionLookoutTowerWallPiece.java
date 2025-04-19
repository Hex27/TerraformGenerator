package org.terraform.structure.pillager.mansion.tower;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class MansionLookoutTowerWallPiece extends MansionTowerWallPiece {

    public MansionLookoutTowerWallPiece(MansionJigsawBuilder builder,
                                        int widthX,
                                        int height,
                                        int widthZ,
                                        JigsawType type,
                                        BlockFace[] validDirs)
    {
        super(builder, widthX, height, widthZ, type, validDirs);
    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, @NotNull Random rand) {
        super.build(data, rand);
    }

    @Override
    public void postBuildDecoration(Random rand, @NotNull PopulatorDataAbstract data) {

        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);

        Wall w = entry.getKey().getDown();


        // Carving
        for (int i = 0; i < entry.getValue(); i++) {

            // sides
            if (i == 0 || i == entry.getValue() - 1) {
                w.getUp().Pillar(8, Material.DARK_OAK_LOG);
                new SlabBuilder(Material.STONE_BRICK_SLAB).setType(Type.TOP).apply(w.getFront().getUp(4));

            }
            else if (i == 1 || i == entry.getValue() - 2) {
                w.getUp().Pillar(3, Material.AIR);

                new StairBuilder(Material.STONE_BRICK_STAIRS).setHalf(Half.TOP)
                                                             .setFacing(w.getDirection().getOppositeFace())
                                                             .apply(w.getFront());

                new OrientableBuilder(Material.STRIPPED_DARK_OAK_LOG).setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
                                                                     .apply(w.getUp())
                                                                     .apply(w.getUp().getFront());

                w.getUp(2).getFront().Pillar(3, Material.STONE_BRICK_WALL);
                w.getUp(2).getFront().CorrectMultipleFacing(3);

                w.getUp(5).getFront().setType(Material.STONE_BRICK_SLAB);

            }
            else if (i == 2 || i == entry.getValue() - 3) {
                w.getUp().Pillar(4, Material.AIR);

                new StairBuilder(Material.STONE_BRICK_STAIRS).setHalf(Half.TOP)
                                                             .setFacing(w.getDirection().getOppositeFace())
                                                             .apply(w.getUp().getFront());
                new DirectionalBuilder(Material.DARK_OAK_FENCE_GATE).setFacing(w.getDirection())
                                                                    .apply(w.getUp(2).getFront());
                w.getUp(2).getFront().CorrectMultipleFacing(1);

                new SlabBuilder(Material.STONE_BRICK_SLAB).setType(Type.TOP).apply(w.getFront().getUp(5));

                new StairBuilder(Material.DARK_OAK_STAIRS).setFacing(w.getDirection()).apply(w.getUp());
            }
            else { // center
                w.getUp().Pillar(5, Material.AIR);

                new StairBuilder(Material.STONE_BRICK_STAIRS).setHalf(Half.TOP)
                                                             .setFacing(w.getDirection().getOppositeFace())
                                                             .apply(w.getUp().getFront());
                w.getUp(2).getFront().setType(Material.STONE_BRICK_WALL);
                w.getUp(2).getFront().CorrectMultipleFacing(1);

                new StairBuilder(Material.DARK_OAK_STAIRS).setFacing(w.getDirection()).apply(w.getUp());

                // Edit stairs on the left and right

                new StairBuilder(Material.DARK_OAK_STAIRS).setHalf(Half.TOP)
                                                          .setFacing(BlockUtils.getLeft(w.getDirection()))
                                                          .apply(w.getUp(5).getLeft())
                                                          .apply(w.getUp(4).getLeft(2))
                                                          .setFacing(BlockUtils.getRight(w.getDirection()))
                                                          .apply(w.getUp(5).getRight())
                                                          .apply(w.getUp(4).getRight(2));

                w.getUp(6).getFront().setType(Material.STONE_BRICK_SLAB);
            }


            w = w.getLeft();
        }
    }


}
