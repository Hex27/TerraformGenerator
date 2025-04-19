package org.terraform.structure.pillager.mansion.tower;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleLocation;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MansionTowerPieceHandler {

    public static final int towerPieceWidth = 7; // 2 less than groundFloorPiece
    // Contains rooms in 3d.
    public final @NotNull HashMap<SimpleLocation, JigsawStructurePiece> pieces = new HashMap<>();
    public final @NotNull ArrayList<JigsawStructurePiece> overlapperPieces = new ArrayList<>();
    private final MansionJigsawBuilder builder;
    private final PopulatorDataAbstract data;

    public MansionTowerPieceHandler(MansionJigsawBuilder builder, PopulatorDataAbstract data) {
        super();
        this.builder = builder;
        this.data = data;
    }

    public int registerTowerPiece(@NotNull Random rand, @NotNull JigsawStructurePiece piece) {

        int height = GenUtils.randInt(rand, 1, 2);

        for (int i = 1; i <= height; i++) {
            JigsawStructurePiece newPiece;
            if (i == 1) {
                newPiece = new MansionBaseTowerPiece(
                        builder,
                        towerPieceWidth,
                        MansionJigsawBuilder.roomHeight,
                        towerPieceWidth,
                        JigsawType.STANDARD,
                        BlockUtils.directBlockFaces
                ).getInstance(new Random(), 0);
            }
            else {
                newPiece = new MansionStandardTowerPiece(
                        builder,
                        towerPieceWidth,
                        MansionJigsawBuilder.roomHeight,
                        towerPieceWidth,
                        JigsawType.STANDARD,
                        BlockUtils.directBlockFaces
                ).getInstance(new Random(), 0);
            }

            if (i == height) {
                ((MansionStandardTowerPiece) newPiece).setHighestPieceInTower(true);
            }
            newPiece.getRoom().setX(piece.getRoom().getX());
            newPiece.getRoom().setY(piece.getRoom().getY() + i * (MansionJigsawBuilder.roomHeight));
            newPiece.getRoom().setZ(piece.getRoom().getZ());

            this.pieces.put(newPiece.getRoom().getSimpleLocation(), newPiece);
        }
        return height;
    }

    public void setupWalls() {
        for (JigsawStructurePiece piece : this.pieces.values()) {
            ArrayList<BlockFace> faces = new ArrayList<>();
            for (BlockFace face : BlockUtils.directBlockFaces) {
                // Only wall off this area if there are no adjacent rooms there.
                // Place the wall regardless.
                if (!this.pieces.containsKey(piece.getRoom().getSimpleLocation().getRelative(face, towerPieceWidth))) {
                    JigsawStructurePiece newWall;
                    if (piece instanceof MansionBaseTowerPiece) {
                        newWall = new MansionBaseTowerWallPiece(
                                builder,
                                towerPieceWidth,
                                MansionJigsawBuilder.roomHeight,
                                towerPieceWidth,
                                JigsawType.STANDARD,
                                BlockUtils.directBlockFaces
                        ).getInstance(new Random(), 0);
                    }
                    else {
                        newWall = new MansionLookoutTowerWallPiece(
                                builder,
                                towerPieceWidth,
                                MansionJigsawBuilder.roomHeight,
                                towerPieceWidth,
                                JigsawType.STANDARD,
                                BlockUtils.directBlockFaces
                        ).getInstance(new Random(), 0);
                    }

                    newWall.getRoom().setX(piece.getRoom().getX() + face.getModX() * towerPieceWidth);
                    newWall.getRoom().setY(piece.getRoom().getY());
                    newWall.getRoom().setZ(piece.getRoom().getZ() + face.getModZ() * towerPieceWidth);
                    newWall.setRotation(face);
                    this.overlapperPieces.add(newWall);
                    faces.add(face);
                }
            }
            piece.setWalledFaces(faces);
        }
    }

    public void buildPieces(Random rand) {
        for (JigsawStructurePiece piece : this.pieces.values()) {

            // Force room to be air first
            int[] lowerCorner = piece.getRoom().getLowerCorner(0);
            int[] upperCorner = piece.getRoom().getUpperCorner(0);
            int lowestY = piece.getRoom().getY() + 1;
            int upperY = piece.getRoom().getY() + piece.getRoom().getHeight();

            for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
                for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                    for (int y = lowestY; y < upperY; y++) {
                        builder.getCore().getPopData().setType(x, y, z, Material.AIR);
                    }
                }
            }

            // Build room
            if (piece instanceof MansionStandardTowerPiece) {
                ((MansionStandardTowerPiece) piece).decorateAwkwardCorners(rand);
            }
            piece.build(builder.getCore().getPopData(), rand);
        }
    }

    public void buildRoofs(BlockFace roofFacing, Random rand) {
        for (JigsawStructurePiece piece : this.pieces.values()) {
            // Build roof.
            if (((MansionStandardTowerPiece) piece).isHighestPieceInTower()) {
                ((MansionStandardTowerPiece) piece).placeTentRoof(data, roofFacing, rand);
            }
        }
    }

    public void buildOverlapperPieces(Random rand) {
        for (JigsawStructurePiece piece : this.overlapperPieces) {
            // Build room
            piece.build(builder.getCore().getPopData(), rand);
        }
    }


}
