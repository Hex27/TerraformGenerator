package org.terraform.structure.pillager.mansion.ground;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Random;

public class MansionEntrancePiece extends JigsawStructurePiece {
    final MansionJigsawBuilder builder;

    public MansionEntrancePiece(MansionJigsawBuilder builder,
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
            w.getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.Pillar(1, rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);

            w.getUp().Pillar(this.getRoom().getHeight(), rand, Material.DARK_OAK_PLANKS);

            w = w.getLeft();
        }

        // Place doorway
        w = w.getRight(5).getUp();

        // Carve empty frame
        w.getRight().Pillar(2, new Random(), Material.AIR);
        w.Pillar(3, new Random(), Material.AIR);
        w.getLeft().Pillar(2, new Random(), Material.AIR);

        // Wall first layer decorations
        w.getFront().getRight(3).Pillar(3, new Random(), Material.DARK_OAK_LOG);
        w.getFront().getLeft(3).Pillar(3, new Random(), Material.DARK_OAK_LOG);

        new StairBuilder(Material.DARK_OAK_STAIRS).setHalf(Half.TOP)
                                                  .setFacing(BlockUtils.getRight(w.getDirection()))
                                                  .apply(w.getFront().getRight(2).getUp(2))
                                                  .apply(w.getFront().getRight(1).getUp(3));

        new StairBuilder(Material.DARK_OAK_STAIRS).setHalf(Half.TOP)
                                                  .setFacing(BlockUtils.getLeft(w.getDirection()))
                                                  .apply(w.getFront().getLeft(2).getUp(2))
                                                  .apply(w.getFront().getLeft(1).getUp(3));

        new OrientableBuilder(Material.DARK_OAK_LOG).setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
                                                    .apply(w.getUp(4).getFront())
                                                    .apply(w.getUp(3).getFront().getLeft(2))
                                                    .apply(w.getUp(3).getFront().getRight(2));

        new DirectionalBuilder(Material.STONE_BUTTON).setFacing(w.getDirection())
                                                     .apply(w.getUp(4).getFront(2))
                                                     .apply(w.getUp(3).getFront(2).getLeft(2))
                                                     .apply(w.getUp(3).getFront(2).getRight(2));

        new OrientableBuilder(Material.DARK_OAK_LOG).setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getRight(w.getDirection())))
                                                    .apply(w.getUp(4).getFront().getLeft())
                                                    .apply(w.getUp(4).getFront().getRight());

        new StairBuilder(Material.DARK_OAK_STAIRS).setHalf(Half.BOTTOM)
                                                  .setFacing(BlockUtils.getLeft(w.getDirection()))
                                                  .apply(w.getFront().getRight(3).getUp(3))
                                                  .apply(w.getFront().getRight(2).getUp(4));

        new StairBuilder(Material.DARK_OAK_STAIRS).setHalf(Half.BOTTOM)
                                                  .setFacing(BlockUtils.getRight(w.getDirection()))
                                                  .apply(w.getFront().getLeft(3).getUp(3))
                                                  .apply(w.getFront().getLeft(2).getUp(4));

        // Wall second layer decorations
        new StairBuilder(Material.DARK_OAK_STAIRS).setHalf(Half.TOP)
                                                  .setFacing(w.getDirection().getOppositeFace())
                                                  .apply(w.getUp(3));

        new StairBuilder(Material.DARK_OAK_STAIRS).setHalf(Half.TOP)
                                                  .setFacing(BlockUtils.getRight(w.getDirection()))
                                                  .apply(w.getRight().getUp(2));

        new StairBuilder(Material.DARK_OAK_STAIRS).setHalf(Half.TOP)
                                                  .setFacing(BlockUtils.getLeft(w.getDirection()))
                                                  .apply(w.getLeft().getUp(2));

        // Stone decorations on the outside
        w.getFront(2).getRight(3).setType(Material.COBBLESTONE);
        w.getFront(2).getLeft(3).setType(Material.COBBLESTONE);
        w.getFront(2).getRight(3).getUp().setType(Material.COBBLESTONE_WALL);
        w.getFront(2).getLeft(3).getUp().setType(Material.COBBLESTONE_WALL);
        w.getFront(2).getRight(3).getUp(2).setType(Material.LANTERN);
        w.getFront(2).getLeft(3).getUp(2).setType(Material.LANTERN);

        // At the entrance, place a circle to give the entrance a slightly special look
        w = new Wall(new SimpleBlock(data, this.getRoom().getX(), this.getRoom().getY(), this.getRoom().getZ()),
                w.getDirection());

        ArrayList<BlockFace> directions = new ArrayList<>();
        for (JigsawStructurePiece p : this.builder.getOverlapperPieces()) {
            if (p.getRoom().getSimpleLocation().equals(this.getRoom().getSimpleLocation())) {
                directions.add(p.getRotation());
            }
        }

        // Door is in an awkward sink-in.
        // Redetermine entrance direction and move the circle outwards
        if (directions.size() == 3) {
            for (BlockFace face : directions) {
                // Identify the outward direction, then remake w with it.
                if (directions.contains(face.getOppositeFace())) {
                    continue;
                }

                w = new Wall(w.get(), face);
                // after remaking the wall, move it outwards to allow the circle
                // to protrude out better
                w = w.getFront(4);
            }
        }
        else {
            w = w.getRear(3); // Push backwards if this is a direct entrance
        }

        int radius = 10;
        int radiusSquared = radius * radius;
        for (int nx = -radius; nx <= radius; nx++) {
            for (int nz = -radius; nz <= radius; nz++) {
                Wall rel = w.getRelative(nx, 0, nz);
                if (rel.get().distanceSquared(w.get()) < radiusSquared) {
                    if (rel.getType() != Material.STONE_BRICKS) {
                        rel.setType(Material.COBBLESTONE);
                    }
                    rel.getDown().downUntilSolid(new Random(), Material.COBBLESTONE);
                    rel.getRelative(0, this.getRoom().getHeight() + 1, 0).setType(Material.STONE_BRICKS);
                }
            }
        }

        // Stairway from the front door
        Wall stairway = w.getFront(10);
        BlockUtils.stairwayUntilSolid(stairway.get(),
                stairway.getDirection(),
                new Material[] {Material.COBBLESTONE},
                Material.COBBLESTONE_STAIRS
        );
        BlockUtils.stairwayUntilSolid(stairway.getLeft().get(),
                stairway.getDirection(),
                new Material[] {Material.COBBLESTONE},
                Material.COBBLESTONE_STAIRS
        );
        BlockUtils.stairwayUntilSolid(stairway.getRight().get(),
                stairway.getDirection(),
                new Material[] {Material.COBBLESTONE},
                Material.COBBLESTONE_STAIRS
        );
        BlockUtils.stairwayUntilSolid(stairway.getLeft(2).get(),
                stairway.getDirection(),
                new Material[] {Material.COBBLESTONE},
                Material.COBBLESTONE_STAIRS
        );
        BlockUtils.stairwayUntilSolid(stairway.getRight(2).get(),
                stairway.getDirection(),
                new Material[] {Material.COBBLESTONE},
                Material.COBBLESTONE_STAIRS
        );

        // Primary upper portion decorations
        Wall ceilingCenter = stairway.getRear().getRelative(0, 1 + MansionJigsawBuilder.roomHeight, 0);
        ceilingCenter.setType(Material.POLISHED_DIORITE);

        new StairBuilder(Material.COBBLESTONE_STAIRS).setHalf(Half.TOP)
                                                     .setFacing(BlockUtils.getLeft(ceilingCenter.getDirection()))
                                                     .apply(ceilingCenter.getLeft())
                                                     .setFacing(BlockUtils.getRight(ceilingCenter.getDirection()))
                                                     .apply(ceilingCenter.getRight())
                                                     .setFacing(ceilingCenter.getDirection().getOppositeFace())
                                                     .apply(ceilingCenter.getDown());

        ceilingCenter.getUp().setType(Material.COBBLESTONE_SLAB);
        for (int i = 1; i <= 3; i++) {
            ceilingCenter.getUp().getLeft(i).setType(Material.STONE_BRICK_WALL);
            ceilingCenter.getUp().getLeft(i).CorrectMultipleFacing(1);
            ceilingCenter.getUp().getRight(i).setType(Material.STONE_BRICK_WALL);
            ceilingCenter.getUp().getRight(i).CorrectMultipleFacing(1);
        }

        // Replace corners with stone bricks instead of cobblestone
        // Place decorative pillars
        for (BlockFace face : BlockUtils.getAdjacentFaces(stairway.getDirection())) {
            Wall target = stairway.getRear().getRelative(face, 3);
            for (int i = 0; i < 6; i++) {
                int maxRecursion = 3;
                while (maxRecursion > 0 && target.getType() != Material.COBBLESTONE) {
                    target = target.getRear();
                    maxRecursion--;
                }
                if (maxRecursion <= 0) {
                    break; // Wtf
                }

                // Ceiling decor
                if (i > 1) {
                    target.getRelative(0, 2 + MansionJigsawBuilder.roomHeight, 0).setType(Material.COBBLESTONE_WALL);
                    target.getRelative(0, 2 + MansionJigsawBuilder.roomHeight, 0).CorrectMultipleFacing(1);
                }

                if (i % 2 == 0 || i > 4) { // Just a short stone brick stub
                    if (i == 0 || i > 4) {
                        target.Pillar(2, Material.STONE_BRICKS);
                    }
                    else {
                        target.getRear().Pillar(2, Material.STONE_BRICKS);
                    }
                }
                else {
                    // Pillar
                    target.getRear().Pillar(1 + MansionJigsawBuilder.roomHeight, Material.DARK_OAK_LOG);

                    target.getUp().Pillar(MansionJigsawBuilder.roomHeight, Material.COBBLESTONE_WALL);
                    target.getUp().CorrectMultipleFacing(MansionJigsawBuilder.roomHeight);

                    target.getUp(5).Pillar(5, Material.COBBLESTONE);

                    new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(face)
                                                                 .setHalf(Half.TOP)
                                                                 .apply(target.getRear()
                                                                              .getRelative(0,
                                                                                      MansionJigsawBuilder.roomHeight
                                                                                      - 1,
                                                                                      0)
                                                                              .getRelative(face.getOppositeFace()))
                                                                 .apply(target.getRear()
                                                                              .getRelative(0,
                                                                                      MansionJigsawBuilder.roomHeight,
                                                                                      0)
                                                                              .getRelative(face.getOppositeFace()))
                                                                 .apply(target.getRear()
                                                                              .getRelative(0,
                                                                                      MansionJigsawBuilder.roomHeight,
                                                                                      0)
                                                                              .getRelative(face.getOppositeFace(), 2));

                    new StairBuilder(Material.COBBLESTONE_STAIRS).setFacing(face)
                                                                 .setHalf(Half.TOP)
                                                                 .apply(target.getRelative(0,
                                                                         MansionJigsawBuilder.roomHeight,
                                                                         0).getRelative(face.getOppositeFace()));
                }

                target = target.getRelative(face);
            }
        }
    }

}
