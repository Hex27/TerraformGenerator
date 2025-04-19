package org.terraform.structure.pillager.mansion;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.pillager.mansion.ground.*;
import org.terraform.structure.pillager.mansion.secondfloor.MansionSecondFloorGrandStairwayPopulator;
import org.terraform.structure.pillager.mansion.secondfloor.MansionSecondFloorHandler;
import org.terraform.structure.pillager.mansion.secondfloor.MansionSecondFloorWallPiece;
import org.terraform.structure.pillager.mansion.secondfloor.MansionTowerStairwayPopulator;
import org.terraform.structure.pillager.mansion.tower.MansionTowerPieceHandler;
import org.terraform.structure.room.jigsaw.JigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.ArrayList;
import java.util.Random;

public class MansionJigsawBuilder extends JigsawBuilder {

    public static final int roomHeight = 7;
    public static final int groundFloorRoomWidth = 9;
    private final @NotNull ArrayList<SimpleLocation> roofedLocations = new ArrayList<>();

    private final MansionTowerPieceHandler towerPieceHandler;
    private final MansionSecondFloorHandler secondFloorHandler;

    public MansionJigsawBuilder(int widthX, int widthZ, @NotNull PopulatorDataAbstract data, int x, int y, int z) {
        super(widthX, widthZ, data, x, y, z);
        towerPieceHandler = new MansionTowerPieceHandler(this, data);
        secondFloorHandler = new MansionSecondFloorHandler(this);
        this.pieceWidth = groundFloorRoomWidth;
        this.pieceRegistry = new JigsawStructurePiece[] {
                new MansionGroundRoomPiece(
                        groundFloorRoomWidth,
                        roomHeight,
                        groundFloorRoomWidth,
                        JigsawType.STANDARD,
                        BlockUtils.directBlockFaces
                ),
                new MansionGroundWallPiece(this,
                        groundFloorRoomWidth,
                        roomHeight,
                        groundFloorRoomWidth,
                        JigsawType.END,
                        BlockUtils.directBlockFaces
                ),
                new MansionEntrancePiece(this,
                        groundFloorRoomWidth,
                        roomHeight,
                        groundFloorRoomWidth,
                        JigsawType.ENTRANCE,
                        BlockUtils.directBlockFaces
                )
        };
        this.chanceToAddNewPiece = 90;
        this.minimumPieces = 15;
    }

    @Override
    public JigsawStructurePiece getFirstPiece(@NotNull Random random) {
        return new MansionGroundRoomPiece(
                groundFloorRoomWidth,
                roomHeight,
                groundFloorRoomWidth,
                JigsawType.STANDARD,
                BlockUtils.directBlockFaces
        );
    }

    @Override
    public void build(@NotNull Random random) {

        for (JigsawStructurePiece piece : this.pieces.values()) {
            ((MansionStandardGroundRoomPiece) piece).purgeMinimalArea(this.core.getPopData());
        }
        super.build(random);

        // Make sure awkward corners are fixed
        for (JigsawStructurePiece piece : this.pieces.values()) {
            SimpleBlock core = new SimpleBlock(this.core.getPopData(),
                    piece.getRoom().getX(),
                    piece.getRoom().getY(),
                    piece.getRoom().getZ()
            );
            Wall target;

            if (piece.getWalledFaces().contains(BlockFace.NORTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.WEST))
            { // nw
                target = new Wall(core.getRelative(-5, 1, -5));
                decorateAwkwardCorner(target,
                        random,
                        BlockFace.NORTH,
                        BlockFace.WEST,
                        areOtherWallsOverlapping(piece, BlockFace.NORTH) || areOtherWallsOverlapping(
                                piece,
                                BlockFace.WEST
                        )
                );
            }
            if (piece.getWalledFaces().contains(BlockFace.NORTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.EAST))
            { // ne
                target = new Wall(core.getRelative(5, 1, -5));
                decorateAwkwardCorner(target,
                        random,
                        BlockFace.NORTH,
                        BlockFace.EAST,
                        areOtherWallsOverlapping(piece, BlockFace.NORTH) || areOtherWallsOverlapping(
                                piece,
                                BlockFace.EAST
                        )
                );
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.WEST))
            { // sw
                target = new Wall(core.getRelative(-5, 1, 5));
                decorateAwkwardCorner(target,
                        random,
                        BlockFace.SOUTH,
                        BlockFace.WEST,
                        areOtherWallsOverlapping(piece, BlockFace.SOUTH) || areOtherWallsOverlapping(
                                piece,
                                BlockFace.WEST
                        )
                );
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.EAST))
            { // se
                target = new Wall(core.getRelative(5, 1, 5));
                decorateAwkwardCorner(target,
                        random,
                        BlockFace.SOUTH,
                        BlockFace.EAST,
                        areOtherWallsOverlapping(piece, BlockFace.SOUTH) || areOtherWallsOverlapping(
                                piece,
                                BlockFace.EAST
                        )
                );
            }
        }

        // Decorate rooms and walls
        for (JigsawStructurePiece piece : this.overlapperPieces) {
            piece.postBuildDecoration(random, this.core.getPopData());
        }

        for (JigsawStructurePiece piece : this.pieces.values()) {
            piece.postBuildDecoration(random, this.core.getPopData());
        }

        for (JigsawStructurePiece piece : this.pieces.values()) {
            MansionStandardGroundRoomPiece mansionPiece = (MansionStandardGroundRoomPiece) piece;
            mansionPiece.thirdStageDecoration(random, this.core.getPopData());
        }

        // Begin populating second story (place room walls)
        secondFloorHandler.setRandom(random);
        secondFloorHandler.populateSecondFloorRoomLayout();
        secondFloorHandler.buildSecondFloor(random);
        secondFloorHandler.decorateAwkwardCorners();

        // Build the roof
        int[][] bounds = MansionRoofHandler.getLargestRectangle(this);

        // Shrink to change behaviour of rectangles at the roof.
        int[] lowerBounds = new int[] {bounds[0][0], bounds[0][1]};
        int[] upperBounds = new int[] {bounds[1][0], bounds[1][1]};

        // Extend the bounds in the shorter axis.
        if (MansionRoofHandler.getDominantAxis(lowerBounds, upperBounds) == Axis.X) {
            lowerBounds[0] -= 7;
            upperBounds[0] += 7;
            lowerBounds[1] -= 4;
            upperBounds[1] += 4;
        }
        else {
            lowerBounds[1] -= 7;
            upperBounds[1] += 7;
            lowerBounds[0] -= 4;
            upperBounds[0] += 4;
        }

        // Debug code for showing roof bounds.

        for (JigsawStructurePiece piece : secondFloorHandler.secondFloorOverlapperPieces) {
            if (piece instanceof MansionSecondFloorWallPiece) {
                ((MansionSecondFloorWallPiece) piece).buildIndividualRoofs(
                        random,
                        this.core.getPopData(),
                        lowerBounds,
                        upperBounds
                );
            }
        }

        MansionRoofHandler.placeTentRoof(random, this, bounds);

        for (JigsawStructurePiece piece : secondFloorHandler.secondFloorOverlapperPieces) {
            piece.postBuildDecoration(random, this.core.getPopData());
        }

        for (JigsawStructurePiece piece : secondFloorHandler.secondFloorPieces.values()) {
            piece.postBuildDecoration(random, this.core.getPopData());
        }

        for (JigsawStructurePiece piece : secondFloorHandler.secondFloorPieces.values()) {
            if (!getRoofedLocations().contains(piece.getRoom().getSimpleLocation())) {
                SimpleLocation loc = piece.getRoom().getSimpleLocation().getUp(13);
                if (this.core.getPopData().getType(loc.getX(), loc.getY(), loc.getZ()) != Material.COBBLESTONE_SLAB) {
                    int towerHeight = towerPieceHandler.registerTowerPiece(random, piece);
                    ((MansionStandardRoomPiece) piece).setRoomPopulator(new MansionTowerStairwayPopulator(
                            piece.getRoom(),
                            ((MansionStandardRoomPiece) piece).internalWalls,
                            towerHeight
                    ));
                }
            }
        }

        towerPieceHandler.setupWalls();
        towerPieceHandler.buildPieces(random);
        towerPieceHandler.buildOverlapperPieces(random);

        for (JigsawStructurePiece piece : towerPieceHandler.overlapperPieces) {
            piece.postBuildDecoration(random, this.core.getPopData());
        }

        for (JigsawStructurePiece piece : towerPieceHandler.pieces.values()) {
            piece.postBuildDecoration(random, this.core.getPopData());
        }

        towerPieceHandler.buildRoofs(MansionRoofHandler.getDominantBlockFace(lowerBounds, upperBounds), random);

        // Begin decorating internal rooms

        // GROUND FLOOR
        for (JigsawStructurePiece piece : pieces.values()) {
            ((MansionStandardRoomPiece) piece).setupInternalAttributes(core.getPopData(), this.getPieces());
        }

        // Carves out pathways with a maze algorithm 
        // and open random walls to make the maze less strict. 
        MansionMazeAlgoUtil.setupPathways(this.pieces.values(), random);
        MansionMazeAlgoUtil.knockdownRandomWalls(this.pieces.values(), random);
        MansionCompoundRoomDistributor.distributeRooms(this.pieces.values(), random, true);

        for (JigsawStructurePiece piece : pieces.values()) {
            ((MansionStandardRoomPiece) piece).buildWalls(random, this.core.getPopData());
        }

        // Second floor walling
        for (JigsawStructurePiece piece : secondFloorHandler.secondFloorPieces.values()) {
            ((MansionStandardRoomPiece) piece).setupInternalAttributes(core.getPopData(),
                    secondFloorHandler.secondFloorPieces);
        }

        MansionMazeAlgoUtil.setupPathways(secondFloorHandler.secondFloorPieces.values(), random);
        MansionMazeAlgoUtil.knockdownRandomWalls(secondFloorHandler.secondFloorPieces.values(), random);


        MansionStandardRoomPiece secondFloorStairwayCenter;
        // Find the Stairway piece and (it extends to the second floor.)
        for (JigsawStructurePiece piece : pieces.values()) {
            if (((MansionStandardRoomPiece) piece).getRoomPopulator() instanceof MansionGrandStairwayPopulator
                && ((MansionStandardRoomPiece) piece).isPopulating())
            {
                secondFloorStairwayCenter = (MansionStandardRoomPiece) secondFloorHandler.secondFloorPieces.get(piece.getRoom()
                                                                                                                     .getSimpleLocation()
                                                                                                                     .getRelative(
                                                                                                                             0,
                                                                                                                             MansionJigsawBuilder.roomHeight
                                                                                                                             + 1,
                                                                                                                             0));
                MansionRoomPopulator secondFloorGrandStairwayPopulator = new MansionSecondFloorGrandStairwayPopulator(
                        null,
                        null).getInstance(secondFloorStairwayCenter.getRoom(), secondFloorStairwayCenter.internalWalls);
                if (!MansionCompoundRoomDistributor.canRoomSizeFitWithCenter(
                        secondFloorStairwayCenter,
                        secondFloorHandler.secondFloorPieces.values(),
                        new MansionRoomSize(3, 3),
                        secondFloorGrandStairwayPopulator,
                        true
                ))
                {
                    TerraformGeneratorPlugin.logger.info("[!] Failed to allocate second floor grand stairway space!");
                }
                secondFloorStairwayCenter.setRoomPopulator(secondFloorGrandStairwayPopulator);
            }
        }

        // SECOND FLOOR WALLING

        MansionCompoundRoomDistributor.distributeRooms(secondFloorHandler.secondFloorPieces.values(), random, false);

        for (JigsawStructurePiece piece : secondFloorHandler.secondFloorPieces.values()) {
            ((MansionStandardRoomPiece) piece).buildWalls(random, this.core.getPopData());
        }

        // Decorate both floors after all allocations are done.
        // Always re-loop for each one to prevent weird race condition overlaps.
        for (JigsawStructurePiece piece : pieces.values()) {
            ((MansionStandardRoomPiece) piece).decorateInternalRoom(random, this.core.getPopData());
        }
        for (JigsawStructurePiece piece : pieces.values()) {
            ((MansionStandardRoomPiece) piece).decorateWalls(random, core.getPopData());
        }
        for (JigsawStructurePiece piece : secondFloorHandler.secondFloorPieces.values()) {
            ((MansionStandardRoomPiece) piece).decorateInternalRoom(random, this.core.getPopData());
            MansionRoofHandler.atticDecorations(random, this.core.getPopData(), piece);
        }
        for (JigsawStructurePiece piece : secondFloorHandler.secondFloorPieces.values()) {
            ((MansionStandardRoomPiece) piece).decorateWalls(random, core.getPopData());
        }

        // Spawn guards
        MansionStandardRoomPiece.spawnedGuards = 0;
        for (JigsawStructurePiece piece : pieces.values()) {
            ((MansionStandardRoomPiece) piece).spawnGuards(random, core.getPopData());
        }
        for (JigsawStructurePiece piece : secondFloorHandler.secondFloorPieces.values()) {
            ((MansionStandardRoomPiece) piece).spawnGuards(random, core.getPopData());
        }
        TerraformGeneratorPlugin.logger.info("Mansion spawned "
                                             + MansionStandardRoomPiece.spawnedGuards
                                             + " vindicators and evokers");
    }

    /**
     * Used to check if there's a wall with an opposite facing within the same location
     */
    private boolean areOtherWallsOverlapping(@NotNull JigsawStructurePiece piece, @NotNull BlockFace face) {
        SimpleLocation other = new SimpleLocation(piece.getRoom().getSimpleLocation().getX()
                                                  + face.getModX() * pieceWidth,
                piece.getRoom().getSimpleLocation().getY() + face.getModY() * pieceWidth,
                piece.getRoom().getSimpleLocation().getZ() + face.getModZ() * pieceWidth
        );
        for (JigsawStructurePiece wall : this.overlapperPieces) {
            if (wall.getRoom().getSimpleLocation().equals(other)) {
                if (wall.getRotation() == face.getOppositeFace()) {
                    return true;
                }
            }
        }

        return false;
    }

    public void decorateAwkwardCorner(@NotNull Wall target,
                                      @NotNull Random random,
                                      @NotNull BlockFace one,
                                      @NotNull BlockFace two,
                                      boolean isSinkIn)
    {

        // Build a large pillar (supports second floor and provides more depth
        if (!isSinkIn) {
            Wall largePillar = target.getRelative(one, 4).getRelative(two, 4);
            largePillar.Pillar(roomHeight, Material.STONE_BRICKS);
            largePillar.getDown().downUntilSolid(new Random(), Material.COBBLESTONE);
            largePillar.getRelative(one).downUntilSolid(new Random(), Material.COBBLESTONE);
            largePillar.getRelative(two).downUntilSolid(new Random(), Material.COBBLESTONE);

            // Side stone walls
            largePillar.getRelative(one).getUp().Pillar(roomHeight - 2, Material.COBBLESTONE_WALL);
            largePillar.getRelative(one).getUp().CorrectMultipleFacing(roomHeight - 2);
            largePillar.getRelative(two).getUp().Pillar(roomHeight - 2, Material.COBBLESTONE_WALL);
            largePillar.getRelative(two).getUp().CorrectMultipleFacing(roomHeight - 2);

            // Cobblestone at the top
            largePillar.getRelative(one).getRelative(0, roomHeight - 1, 0).Pillar(3, Material.COBBLESTONE);
            largePillar.getRelative(two).getRelative(0, roomHeight - 1, 0).Pillar(3, Material.COBBLESTONE);

            // Decorative upsidedown stairs
            new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(one)
                                                         .setHalf(Half.TOP)
                                                         .apply(largePillar.getRelative(0, roomHeight - 2, 0)
                                                                           .getRelative(one.getOppositeFace()))
                                                         .apply(largePillar.getRelative(0, roomHeight - 1, 0)
                                                                           .getRelative(one.getOppositeFace()))
                                                         .apply(largePillar.getRelative(0, roomHeight - 1, 0)
                                                                           .getRelative(one.getOppositeFace(), 2));

            new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(two)
                                                         .setHalf(Half.TOP)
                                                         .apply(largePillar.getRelative(0, roomHeight - 2, 0)
                                                                           .getRelative(two.getOppositeFace()))
                                                         .apply(largePillar.getRelative(0, roomHeight - 1, 0)
                                                                           .getRelative(two.getOppositeFace()))
                                                         .apply(largePillar.getRelative(0, roomHeight - 1, 0)
                                                                           .getRelative(two.getOppositeFace(), 2));
        }

        // Fill in gap in the corner
        target.Pillar(roomHeight, Material.POLISHED_ANDESITE);

        target.getUp(2).setType(Material.STONE_BRICK_WALL);
        target.getUp(3).setType(Material.POLISHED_DIORITE);
        target.getUp(4).setType(Material.STONE_BRICK_WALL);
        target.getUp(2).CorrectMultipleFacing(3);

        target.getDown()
              .downUntilSolid(random,
                      Material.COBBLESTONE,
                      Material.COBBLESTONE,
                      Material.COBBLESTONE,
                      Material.COBBLESTONE,
                      Material.MOSSY_COBBLESTONE
              );

        // Small stair base
        new StairBuilder(Material.COBBLESTONE_STAIRS).setFacing(one.getOppositeFace()).apply(target.getRelative(one));

        new StairBuilder(Material.COBBLESTONE_STAIRS).setFacing(two.getOppositeFace()).apply(target.getRelative(two));

        new StairBuilder(Material.COBBLESTONE_STAIRS).setFacing(two.getOppositeFace())
                                                     .apply(target.getRelative(two).getRelative(one))
                                                     .correct();

        // Two more slabs at the corner
        new SlabBuilder(Material.COBBLESTONE_SLAB).lapply(target.getRelative(one).getRelative(BlockUtils.getRight(one)))
                                                  .lapply(target.getRelative(one).getRelative(BlockUtils.getLeft(one)));

        new SlabBuilder(Material.COBBLESTONE_SLAB).lapply(target.getRelative(two).getRelative(BlockUtils.getRight(two)))
                                                  .lapply(target.getRelative(two).getRelative(BlockUtils.getLeft(two)));


        // Small stair base
        new StairBuilder(Material.COBBLESTONE_STAIRS).setFacing(one.getOppositeFace())
                                                     .setHalf(Half.TOP)
                                                     .apply(target.getUp(6).getRelative(one));

        new StairBuilder(Material.COBBLESTONE_STAIRS).setFacing(two.getOppositeFace())
                                                     .setHalf(Half.TOP)
                                                     .apply(target.getUp(6).getRelative(two));

        new StairBuilder(Material.COBBLESTONE_STAIRS).setFacing(two.getOppositeFace())
                                                     .setHalf(Half.TOP)
                                                     .apply(target.getUp(6).getRelative(two).getRelative(one))
                                                     .correct();

        // Two more slabs at the corner
        new SlabBuilder(Material.COBBLESTONE_SLAB).setType(Type.TOP)
                                                  .lapply(target.getUp(6)
                                                                .getRelative(one)
                                                                .getRelative(BlockUtils.getRight(one)))
                                                  .lapply(target.getUp(6)
                                                                .getRelative(one)
                                                                .getRelative(BlockUtils.getLeft(one)));

        new SlabBuilder(Material.COBBLESTONE_SLAB).setType(Type.TOP)
                                                  .lapply(target.getUp(6)
                                                                .getRelative(two)
                                                                .getRelative(BlockUtils.getRight(two)))
                                                  .lapply(target.getUp(6)
                                                                .getRelative(two)
                                                                .getRelative(BlockUtils.getLeft(two)));
    }

    @Override
    public boolean canPlaceEntrance(SimpleLocation pieceLoc) {
        // Also disallow 2, as it implies a corner doorway, which is diagonal.
        // I don't want to deal with a diagonal staircase, so fuck that.
        return this.countOverlappingPiecesAtLocation(pieceLoc) != 4
               && this.countOverlappingPiecesAtLocation(pieceLoc) != 2;
    }

    public @NotNull ArrayList<SimpleLocation> getRoofedLocations() {
        return roofedLocations;
    }

    public MansionTowerPieceHandler getTowerPieceHandler() {
        return towerPieceHandler;
    }


}
