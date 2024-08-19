package org.terraform.structure.pillager.mansion.ground;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.small_items.PlantBuilder;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.blockdata.TrapdoorBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class MansionGroundWallPiece extends JigsawStructurePiece {

    private final MansionJigsawBuilder builder;

    public MansionGroundWallPiece(MansionJigsawBuilder builder,
                                  int widthX,
                                  int height,
                                  int widthZ,
                                  JigsawType type,
                                  BlockFace[] validDirs)
    {
        super(widthX, height, widthZ, type, validDirs);
        this.builder = builder;

    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, @NotNull Random rand) {

        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getDown();

        for (int i = 0; i < entry.getValue(); i++) {

            // Primary Wall and ground beneath wall
            w.getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.Pillar(1, rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.getUp().Pillar(this.getRoom().getHeight(), rand, Material.DARK_OAK_PLANKS);

            w = w.getLeft();
        }
    }

    /**
     * Extra decorations like windows or walls, depending on the surrounding walls
     */
    @Override
    public void postBuildDecoration(@NotNull Random rand, @NotNull PopulatorDataAbstract data) {
        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        MansionWallType type = MansionWallType.THIN_WINDOWS;
        if (rand.nextBoolean()) {
            type = MansionWallType.LARGE_WINDOW;
        }

        for (JigsawStructurePiece otherPiece : builder.getOverlapperPieces()) {
            int[] center = otherPiece.getRoom().getCenter();
            if (otherPiece instanceof MansionEntrancePiece
                && center[0] == this.getRoom().getCenter()[0]
                && center[1] == this.getRoom().getCenter()[1]
                && center[2] == this.getRoom().getCenter()[2])
            {
                type = MansionWallType.PLAIN;
            }
        }

        Wall w = entry.getKey().getDown();

        for (int i = 0; i < entry.getValue(); i++) {

            switch (type) {
                case LARGE_WINDOW:
                    if (i == 1 || i == entry.getValue() - 2) // Side decoration
                    {
                        w.getUp().Pillar(this.getRoom().getHeight(), new Random(), Material.DARK_OAK_LOG);
                    }

                    if (i == 3 || i == 4 || i == 5) { // Window Panes and decorations at the base
                        w.getUp(2).Pillar(4, new Random(), Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                        w.getUp(2).CorrectMultipleFacing(4);

                        new OrientableBuilder(Material.DARK_OAK_LOG).setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
                                                                    .apply(w.getUp().getFront());
                        new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR).setFacing(w.getDirection())
                                                                       .setOpen(true)
                                                                       .apply(w.getUp().getFront(2));
                        new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR).setFacing(w.getDirection())
                                                                       .apply(w.getUp().getFront().getUp());
                    }

                    if (i == 2 || i == entry.getValue() - 3) {// Supporting Pillars
                        w.getFront().getUp().setType(Material.COBBLESTONE);
                        w.getFront().getUp(2).Pillar(3, new Random(), Material.STONE_BRICK_WALL);
                        w.getFront().getUp(2).CorrectMultipleFacing(3);
                        new OrientableBuilder(Material.DARK_OAK_LOG).setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
                                                                    .apply(w.getFront().getUp(5));
                    }

                    if (i == 4) {
                        // Place Main window decoration
                        spawnWindowOverhang(w.getFront().getUp(6));
                    }

                    break;
                case PLAIN:
                    break;
                case THIN_WINDOWS:
                    if (i == 2 || i == entry.getValue() - 3) // Side decoration
                    {
                        w.getUp().Pillar(this.getRoom().getHeight(), new Random(), Material.DARK_OAK_LOG);

                        new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setHalf(Half.TOP)
                                                                           .setFacing(w.getDirection()
                                                                                       .getOppositeFace())
                                                                           .apply(w.getUp(2).getFront());


                        w.getUp(3).getFront().setType(Material.STONE_BRICK_WALL);
                        w.getUp(4).getFront().setType(Material.STONE_BRICK_WALL);
                        w.getUp(5).getFront().setType(Material.COBBLESTONE_SLAB);
                    }
                    if (i % 2 == 1) {
                        w.getUp(2).Pillar(4, new Random(), Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                        w.getUp(2).CorrectMultipleFacing(4);
                    }

                    if (i == (entry.getValue() / 2)) {
                        spawnWallSupportingPillar(w.getFront().getUp(), this.getRoom().getHeight());
                    }
                    break;
            }

            if (type != MansionWallType.PLAIN && i == 4) {
                // Frontal Decorations - Pillars
                // Only spawn if the wall is not a sink-in
                int overlaps = 0;
                for (JigsawStructurePiece p : this.builder.getOverlapperPieces()) {
                    if (p.getRoom().getSimpleLocation().equals(this.getRoom().getSimpleLocation())) {
                        overlaps++;
                    }
                }
                if (overlaps == 1) {
                    // Front-facing pillar with some shrubbery and grass
                    Wall target = w.getFront(4).getUp();
                    target.Pillar(MansionJigsawBuilder.roomHeight, Material.STONE_BRICKS);
                    target.getFront().Pillar(MansionJigsawBuilder.roomHeight, Material.COBBLESTONE_WALL);
                    target.getFront().CorrectMultipleFacing(MansionJigsawBuilder.roomHeight);

                    target.getFront().setType(Material.COBBLESTONE);
                    target.getFront()
                          .getRelative(0, MansionJigsawBuilder.roomHeight - 1, 0)
                          .setType(Material.COBBLESTONE);
                    target.getFront().getDown().downUntilSolid(new Random(), Material.COBBLESTONE);

                    new SlabBuilder(Material.COBBLESTONE_SLAB).setType(Type.TOP)
                                                              .apply(target.getFront()
                                                                           .getRelative(0,
                                                                                   MansionJigsawBuilder.roomHeight - 1,
                                                                                   0)
                                                                           .getLeft())
                                                              .apply(target.getFront()
                                                                           .getRelative(0,
                                                                                   MansionJigsawBuilder.roomHeight - 1,
                                                                                   0)
                                                                           .getRight());

                    target.getRight().setType(Material.COBBLESTONE);
                    target.getLeft().setType(Material.COBBLESTONE);

                    PlantBuilder.DARK_OAK_LEAVES.build(target.getFront().getRight());
                    PlantBuilder.DARK_OAK_LEAVES.build(target.getFront().getLeft());
                    target.getUp().getRight().setType(Material.GRASS_BLOCK);
                    PlantBuilder.GRASS.build(target.getUp(2).getRight());
                    target.getUp().getLeft().setType(Material.GRASS_BLOCK);
                    PlantBuilder.GRASS.build(target.getUp(2).getLeft());
                    target.getFront().getRight().getDown().downUntilSolid(new Random(), Material.COBBLESTONE);
                    target.getFront().getLeft().getDown().downUntilSolid(new Random(), Material.COBBLESTONE);

                    new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR).setFacing(target.getDirection())
                                                                   .setOpen(true)
                                                                   .apply(target.getUp().getFront().getRight())
                                                                   .apply(target.getUp().getFront().getLeft())
                                                                   .setFacing(target.getDirection().getOppositeFace())
                                                                   .apply(target.getUp().getRear().getRight())
                                                                   .apply(target.getUp().getRear().getLeft());

                    new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(target.getDirection().getOppositeFace())
                                                                 .apply(target.getRight(2).getFront())
                                                                 .apply(target.getLeft(2).getFront());
                    target.getFront().getRight(2).getDown().downUntilSolid(new Random(), Material.COBBLESTONE);
                    target.getFront().getLeft(2).getDown().downUntilSolid(new Random(), Material.COBBLESTONE);

                    target.getRight(2).setType(Material.STONE_BRICKS);
                    target.getLeft(2).setType(Material.STONE_BRICKS);

                    new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(BlockUtils.getLeft(target.getDirection()))
                                                                 .apply(target.getRight(2).getUp())
                                                                 .setFacing(BlockUtils.getRight(target.getDirection()))
                                                                 .apply(target.getLeft(2).getUp());

                    // underhanging stair decorations

                    // Two more slabs at the corner
                    new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(BlockUtils.getRight(target.getDirection()))
                                                                 .setHalf(Half.TOP)
                                                                 .apply(target.getRelative(0,
                                                                         MansionJigsawBuilder.roomHeight - 2,
                                                                         0).getLeft())
                                                                 .apply(target.getRelative(0,
                                                                         MansionJigsawBuilder.roomHeight - 1,
                                                                         0).getLeft())
                                                                 .apply(target.getRelative(0,
                                                                         MansionJigsawBuilder.roomHeight - 1,
                                                                         0).getLeft(2));

                    new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(BlockUtils.getLeft(target.getDirection()))
                                                                 .setHalf(Half.TOP)
                                                                 .apply(target.getRelative(0,
                                                                         MansionJigsawBuilder.roomHeight - 2,
                                                                         0).getRight())
                                                                 .apply(target.getRelative(0,
                                                                         MansionJigsawBuilder.roomHeight - 1,
                                                                         0).getRight())
                                                                 .apply(target.getRelative(0,
                                                                         MansionJigsawBuilder.roomHeight - 1,
                                                                         0).getRight(2));

                }
                else if (overlaps == 2) { // Corner pillar
                    Wall target = w.getFront(4).getUp();
                    target.Pillar(MansionJigsawBuilder.roomHeight, Material.STONE_BRICKS);

                    new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(target.getDirection().getOppositeFace())
                                                                 .setHalf(Half.TOP)
                                                                 .apply(target.getRelative(0,
                                                                         MansionJigsawBuilder.roomHeight - 2,
                                                                         0).getFront())
                                                                 .apply(target.getRelative(0,
                                                                         MansionJigsawBuilder.roomHeight - 1,
                                                                         0).getFront())
                                                                 .apply(target.getRelative(0,
                                                                         MansionJigsawBuilder.roomHeight - 1,
                                                                         0).getFront(2));


                }
            }

            w = w.getLeft();
        }
    }

    private void spawnWallSupportingPillar(@NotNull Wall w, int height) {
        w.Pillar(height, new Random(), Material.POLISHED_ANDESITE);

        new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setFacing(w.getDirection().getOppositeFace())
                                                           .apply(w.getFront());

        new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setFacing(BlockUtils.getRight(w.getDirection()))
                                                           .apply(w.getRelative(BlockUtils.getLeft(w.getDirection())));

        new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setFacing(BlockUtils.getLeft(w.getDirection()))
                                                           .apply(w.getRelative(BlockUtils.getRight(w.getDirection())));


        new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setHalf(Half.TOP)
                                                           .setFacing(w.getDirection().getOppositeFace())
                                                           .apply(w.getRelative(0, height - 1, 0).getFront());

        new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setHalf(Half.TOP)
                                                           .setFacing(BlockUtils.getRight(w.getDirection()))
                                                           .apply(w.getRelative(0, height - 1, 0)
                                                                   .getRelative(BlockUtils.getLeft(w.getDirection())));

        new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setHalf(Half.TOP)
                                                           .setFacing(BlockUtils.getLeft(w.getDirection()))
                                                           .apply(w.getRelative(0, height - 1, 0)
                                                                   .getRelative(BlockUtils.getRight(w.getDirection())));

        w.getUp(2).setType(Material.STONE_BRICK_WALL);
        w.getUp(3).setType(Material.POLISHED_DIORITE);
        w.getUp(4).setType(Material.STONE_BRICK_WALL);
        w.getUp(2).CorrectMultipleFacing(3);
    }

    private void spawnWindowOverhang(@NotNull Wall w) {
        // log row
        new OrientableBuilder(Material.DARK_OAK_LOG).setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getRight(w.getDirection())))
                                                    .apply(w)
                                                    .apply(w.getLeft())
                                                    .apply(w.getRight());

        // Upsidedown overhang in front of log row.
        new StairBuilder(Material.COBBLESTONE_STAIRS).setHalf(Half.TOP)
                                                     .setFacing(w.getDirection().getOppositeFace())
                                                     .apply(w.getFront());
        new SlabBuilder(Material.COBBLESTONE_SLAB).setType(Type.TOP)
                                                  .apply(w.getFront().getLeft())
                                                  .apply(w.getFront().getRight());


        // Inner upside down stairs
        new StairBuilder(Material.COBBLESTONE_STAIRS).setHalf(Half.TOP)
                                                     .setFacing(BlockUtils.getLeft(w.getDirection()))
                                                     .apply(w.getDown().getLeft())
                                                     .setFacing(BlockUtils.getRight(w.getDirection()))
                                                     .apply(w.getDown().getRight());

        // Stairs at the top
        new StairBuilder(Material.COBBLESTONE_STAIRS).setFacing(BlockUtils.getLeft(w.getDirection()))
                                                     .apply(w.getRight(2))
                                                     .apply(w.getRight().getUp());

        new StairBuilder(Material.COBBLESTONE_STAIRS).setFacing(BlockUtils.getRight(w.getDirection()))
                                                     .apply(w.getLeft(2))
                                                     .apply(w.getLeft().getUp());

        new OrientableBuilder(Material.DARK_OAK_LOG).setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
                                                    .apply(w.getUp());
    }

    private enum MansionWallType {
        PLAIN, THIN_WINDOWS, LARGE_WINDOW
    }

}
