package org.terraform.structure.village.plains.forge;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.structure.village.plains.forge.PlainsVillageForgeWallPiece.PlainsVillageForgeWallType;
import org.terraform.utils.BlockUtils;

import java.util.ArrayList;
import java.util.Random;

public class PlainsVillageForgeJigsawBuilder extends JigsawBuilder {

    private final PlainsVillagePopulator plainsVillagePopulator;

    public PlainsVillageForgeJigsawBuilder(PlainsVillagePopulator plainsVillagePopulator,
                                           int widthX,
                                           int widthZ,
                                           @NotNull PopulatorDataAbstract data,
                                           int x,
                                           int y,
                                           int z)
    {
        super(widthX, widthZ, data, x, y, z);
        this.plainsVillagePopulator = plainsVillagePopulator;
        this.pieceRegistry = new JigsawStructurePiece[] {
                new PlainsVillageForgeWeaponSmithPiece(
                        plainsVillagePopulator,
                        5,
                        3,
                        5,
                        JigsawType.STANDARD,
                        BlockUtils.directBlockFaces
                ),
                new PlainsVillageForgeMasonPiece(plainsVillagePopulator,
                        5,
                        3,
                        5,
                        JigsawType.STANDARD,
                        BlockUtils.directBlockFaces
                ),
                new PlainsVillageForgeWallPiece(plainsVillagePopulator,
                        5,
                        3,
                        5,
                        JigsawType.END,
                        BlockUtils.directBlockFaces
                ),
                new PlainsVillageForgeEntrancePiece(plainsVillagePopulator,
                        5,
                        3,
                        5,
                        JigsawType.ENTRANCE,
                        BlockUtils.directBlockFaces
                )
        };
        this.chanceToAddNewPiece = 50;
    }

    @Override
    public @NotNull JigsawStructurePiece getFirstPiece(@NotNull Random random) {
        return new PlainsVillageForgeChimneyPiece(
                plainsVillagePopulator,
                5,
                3,
                5,
                JigsawType.STANDARD,
                BlockUtils.directBlockFaces
        );
        // return getPiece(pieceRegistry, JigsawType.STANDARD, random).getInstance(random, 0);
    }

    @Override
    public void build(@NotNull Random random) {
        super.build(random);
        ArrayList<SimpleLocation> rectanglePieces = PlainsVillageForgeRoofHandler.identifyRectangle(pieces);

        ArrayList<JigsawStructurePiece> builtWalls = new ArrayList<>();

        // Decorate walls and entrance based on the rectangle identified.
        for (JigsawStructurePiece piece : pieces.values()) {
            PlainsVillageForgeWallType wallType = PlainsVillageForgeWallType.FENCE;
            if (rectanglePieces.contains(piece.getRoom().getSimpleLocation())) {
                wallType = PlainsVillageForgeWallType.SOLID;
            }
            ((PlainsVillageForgePiece) piece).setWallType(wallType);
            for (BlockFace face : BlockUtils.directBlockFaces) {
                JigsawStructurePiece wall = getAdjacentWall(piece.getRoom().getSimpleLocation(), face);
                if (wall != null && !builtWalls.contains(wall)) {
                    builtWalls.add(wall);
                    ((PlainsVillageForgePiece) wall).setWallType(wallType);
                }
            }
        }

        // Make sure awkward corners are fixed
        for (JigsawStructurePiece piece : this.pieces.values()) {
            SimpleBlock core = new SimpleBlock(this.core.getPopData(),
                    piece.getRoom().getX(),
                    piece.getRoom().getY(),
                    piece.getRoom().getZ()
            );
            Wall target;
            PlainsVillageForgeWallType type = PlainsVillageForgeWallType.FENCE;
            if (rectanglePieces.contains(piece.getRoom().getSimpleLocation())) {
                type = PlainsVillageForgeWallType.SOLID;
            }
            if (piece.getWalledFaces().contains(BlockFace.NORTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.WEST))
            { // nw
                target = new Wall(core.getRelative(-3, 0, -3));
                decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.WEST, type);
            }
            if (piece.getWalledFaces().contains(BlockFace.NORTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.EAST))
            { // ne
                target = new Wall(core.getRelative(3, 0, -3));
                decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.EAST, type);
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.WEST))
            { // sw
                target = new Wall(core.getRelative(-3, 0, 3));
                decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.WEST, type);
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.EAST))
            { // se
                target = new Wall(core.getRelative(3, 0, 3));
                decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.EAST, type);
            }
        }

        PlainsVillageForgeRoofHandler.placeRoof(this.plainsVillagePopulator, core, rectanglePieces);

        // Decorate rooms and walls
        for (JigsawStructurePiece piece : this.overlapperPieces) {
            piece.postBuildDecoration(random, this.core.getPopData());
        }

        for (JigsawStructurePiece piece : this.pieces.values()) {
            piece.postBuildDecoration(random, this.core.getPopData());
        }


    }

    public void decorateAwkwardCorner(@NotNull Wall target,
                                      @NotNull Random random,
                                      BlockFace one,
                                      BlockFace two,
                                      PlainsVillageForgeWallType wallType)
    {
        if (wallType == PlainsVillageForgeWallType.SOLID) {
            // Corner logs
            target.Pillar(4, random, plainsVillagePopulator.woodLog);

            target.getDown().downUntilSolid(random, plainsVillagePopulator.woodLog);

            target.getUp();
        }
        else {
            // Fence stubs
            target.Pillar(2, random, plainsVillagePopulator.woodLog);
            target.getUp(2).setType(Material.STONE_SLAB, Material.COBBLESTONE_SLAB, Material.ANDESITE_SLAB);
            target.getDown().downUntilSolid(random, plainsVillagePopulator.woodLog);
        }

    }
}
