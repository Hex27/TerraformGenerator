package org.terraform.structure.pillager.mansion.secondfloor;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class MansionSecondFloorHandler {
    public final @NotNull HashMap<SimpleLocation, JigsawStructurePiece> secondFloorPieces = new HashMap<>();
    public final @NotNull ArrayList<JigsawStructurePiece> secondFloorOverlapperPieces = new ArrayList<>();
    private final MansionJigsawBuilder builder;
    private Random random;

    public MansionSecondFloorHandler(MansionJigsawBuilder builder) {
        this.builder = builder;
        this.random = new Random();
    }

    public void decorateAwkwardCorners()
    {
        // Make sure awkward corners are fixed
        for (JigsawStructurePiece piece : secondFloorPieces.values()) {
            SimpleBlock core = new SimpleBlock(builder.getCore().getPopData(),
                    piece.getRoom().getX(),
                    piece.getRoom().getY(),
                    piece.getRoom().getZ()
            );
            Wall target;

            if (piece.getWalledFaces().contains(BlockFace.NORTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.WEST))
            { // nw
                target = new Wall(core.getRelative(-5, 1, -5));
                decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.WEST);
            }
            if (piece.getWalledFaces().contains(BlockFace.NORTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.EAST))
            { // ne
                target = new Wall(core.getRelative(5, 1, -5));
                decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.EAST);
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.WEST))
            { // sw
                target = new Wall(core.getRelative(-5, 1, 5));
                decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.WEST);
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.EAST))
            { // se
                target = new Wall(core.getRelative(5, 1, 5));
                decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.EAST);
            }
        }
    }

    public void decorateAwkwardCorner(@NotNull Wall target, Random random, BlockFace one, BlockFace two) {

        // Fill in gap in the corner
        target.Pillar(MansionJigsawBuilder.roomHeight, Material.DARK_OAK_LOG);

    }

    public void populateSecondFloorRoomLayout() {
        for (JigsawStructurePiece piece : builder.getPieces().values()) {
            JigsawStructurePiece newPiece = new MansionStandardSecondFloorPiece(builder,
                    MansionJigsawBuilder.groundFloorRoomWidth,
                    MansionJigsawBuilder.roomHeight,
                    MansionJigsawBuilder.groundFloorRoomWidth,
                    JigsawType.STANDARD,
                    BlockUtils.directBlockFaces
            ).getInstance(new Random(), 0);
            newPiece.getRoom().setX(piece.getRoom().getX());
            newPiece.getRoom().setY(piece.getRoom().getY() + MansionJigsawBuilder.roomHeight + 1);
            newPiece.getRoom().setZ(piece.getRoom().getZ());
            ArrayList<BlockFace> faces = new ArrayList<>();
            for (BlockFace face : piece.getWalledFaces()) {
                JigsawStructurePiece newWall = new MansionSecondFloorWallPiece(builder,
                        MansionJigsawBuilder.groundFloorRoomWidth,
                        MansionJigsawBuilder.roomHeight,
                        MansionJigsawBuilder.groundFloorRoomWidth,
                        JigsawType.STANDARD,
                        BlockUtils.directBlockFaces
                ).getInstance(new Random(), 0);

                newWall.getRoom()
                       .setX(piece.getRoom().getX() + face.getModX() * MansionJigsawBuilder.groundFloorRoomWidth);
                newWall.getRoom().setY(piece.getRoom().getY() + MansionJigsawBuilder.roomHeight + 1);
                newWall.getRoom()
                       .setZ(piece.getRoom().getZ() + face.getModZ() * MansionJigsawBuilder.groundFloorRoomWidth);
                newWall.setRotation(face);
                this.secondFloorOverlapperPieces.add(newWall);
                faces.add(face);
            }
            newPiece.setWalledFaces(faces);

            this.secondFloorPieces.put(newPiece.getRoom().getSimpleLocation(), newPiece);
        }
    }

    public void buildSecondFloor(Random random) {
        for (JigsawStructurePiece piece : this.secondFloorPieces.values()) {

            // Force room to be air first
            int[] lowerCorner = piece.getRoom().getLowerCorner(0);
            int[] upperCorner = piece.getRoom().getUpperCorner(0);
            int lowestY = piece.getRoom().getY() + 1;
            int upperY = piece.getRoom().getY() + piece.getRoom().getHeight();

            for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
                for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                    for (int y = lowestY; y <= upperY; y++) {
                        builder.getCore().getPopData().setType(x, y, z, Material.AIR);
                    }
                }
            }

            // Build room
            piece.build(builder.getCore().getPopData(), random);
        }

        ArrayList<JigsawStructurePiece> toRemove = new ArrayList<>();
        // Overlapper pieces are stuff like walls and entrances.
        Collections.shuffle(this.secondFloorOverlapperPieces);
        for (JigsawStructurePiece piece : this.secondFloorOverlapperPieces) {
            // Don't place overlapper objects where rooms have been placed.
            SimpleLocation pieceLoc = new SimpleLocation(
                    piece.getRoom().getX(),
                    piece.getRoom().getY(),
                    piece.getRoom().getZ()
            );
            if (secondFloorPieces.containsKey(pieceLoc)) {
                toRemove.add(piece);
                continue;
            }
            JigsawStructurePiece host = builder.getAdjacentPiece(pieceLoc, piece.getRotation().getOppositeFace());
            if (host != null) {
                host.getWalledFaces().add(piece.getRotation());
            }
            // TerraformGeneratorPlugin.logger.info("Populating at " + piece.getClass().getSimpleName() + "::" + piece.getRoom().getX() + "," + piece.getRoom().getZ() + "," + piece.getRotation());
            piece.build(builder.getCore().getPopData(), random);
        }

        // Remove pieces that weren't placed and replace the unused wall with the entrance.        
        secondFloorOverlapperPieces.removeIf(toRemove::contains);
    }

    public void setRandom(Random random) {
        this.random = random;
    }

}
