package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.structure.room.jigsaw.JigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.ArrayList;
import java.util.Random;

public class PlainsVillageTempleJigsawBuilder extends JigsawBuilder {

    final PlainsVillagePopulator plainsVillagePopulator;

    public PlainsVillageTempleJigsawBuilder(PlainsVillagePopulator plainsVillagePopulator,
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
                new PlainsVillageTempleLoungePiece(
                        plainsVillagePopulator,
                        5,
                        3,
                        5,
                        JigsawType.STANDARD,
                        BlockUtils.directBlockFaces
                ),
                new PlainsVillageTempleRelicPiece(plainsVillagePopulator,
                        5,
                        3,
                        5,
                        JigsawType.STANDARD,
                        true,
                        BlockUtils.directBlockFaces
                ),
                new PlainsVillageTempleLootPiece(plainsVillagePopulator,
                        5,
                        3,
                        5,
                        JigsawType.STANDARD,
                        BlockUtils.directBlockFaces
                ),
                new PlainsVillageTempleWallPiece(5, 3, 5, JigsawType.END, BlockUtils.directBlockFaces),
                new PlainsVillageTempleEntrancePiece(plainsVillagePopulator,
                        5,
                        3,
                        5,
                        JigsawType.ENTRANCE,
                        BlockUtils.directBlockFaces
                )
        };
        this.chanceToAddNewPiece = 50;
    }

    /**
     * Refers to walls that are parallel and directly connected are in the form:
     * __
     */
    protected static boolean hasAdjacentWall(@NotNull JigsawStructurePiece piece,
                                             @NotNull BlockFace face,
                                             @NotNull ArrayList<JigsawStructurePiece> overlapperPieces)
    {
        for (JigsawStructurePiece other : overlapperPieces) {
            if (other.getRoom().getSimpleLocation().equals(piece.getRoom().getSimpleLocation().getRelative(face, 5))) {
                if (other.getRotation() == piece.getRotation()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Refers to walls that are directly connected and perpendicular on the same location.
     * I.e. the wall must turn inwards instead of outwards (NORTH and WEST facing walls connected)
     */
    protected static boolean hasAdjacentInwardWall(@NotNull JigsawStructurePiece piece,
                                                   @NotNull BlockFace face,
                                                   @NotNull ArrayList<JigsawStructurePiece> overlapperPieces)
    {

        for (JigsawStructurePiece other : overlapperPieces) {
            if (other.getRoom().getSimpleLocation().equals(piece.getRoom().getSimpleLocation())) {
                if (other.getRotation() == face.getOppositeFace()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull JigsawStructurePiece getFirstPiece(@NotNull Random random) {
        return new PlainsVillageTempleClericAltarPiece(
                plainsVillagePopulator,
                5,
                3,
                5,
                JigsawType.STANDARD,
                true,
                this,
                BlockUtils.directBlockFaces
        );
        // return getPiece(pieceRegistry, JigsawType.STANDARD, random).getInstance(random, 0);
    }

    @Override
    public void build(@NotNull Random random) {
        if (!TConfig.areStructuresEnabled()) {
            return;
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
                target = new Wall(core.getRelative(-3, 0, -3));
                decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.WEST);
            }
            if (piece.getWalledFaces().contains(BlockFace.NORTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.EAST))
            { // ne
                target = new Wall(core.getRelative(3, 0, -3));
                decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.EAST);
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.WEST))
            { // sw
                target = new Wall(core.getRelative(-3, 0, 3));
                decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.WEST);
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.EAST))
            { // se
                target = new Wall(core.getRelative(3, 0, 3));
                decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.EAST);
            }
        }

        // Declare one of the pieces a tower
        int randIndex = random.nextInt(this.pieces.size());
        int i = 0;
        for (JigsawStructurePiece p : this.pieces.values()) {
            if (i == randIndex) {
                ((PlainsVillageTempleStandardPiece) p).setTower(true);
                // break;
            }
        }

        // Place roofing
        for (JigsawStructurePiece piece : overlapperPieces) {
            PlainsVillageTempleRoofHandler.handleTempleRoof(
                    plainsVillagePopulator,
                    this.core.getPopData(),
                    piece,
                    overlapperPieces
            );
        }

        // Try to place large windows between pairs of walls
        for (JigsawStructurePiece wallPiece : overlapperPieces) {
            for (BlockFace face : BlockUtils.getAdjacentFaces(wallPiece.getRotation())) {
                if (hasAdjacentWall(wallPiece, face, overlapperPieces)) {
                    PlainsVillageTempleWallPiece.setLargeWindow(
                            this.core.getPopData(),
                            wallPiece.getRotation(),
                            wallPiece.getRoom(),
                            face
                    );
                }
            }
        }

        PlainsVillageTempleRoofHandler.placeCeilingTerracotta(this.core.getPopData(), this.pieces.values());

        // Decorate rooms and walls
        for (JigsawStructurePiece piece : this.pieces.values()) {
            piece.postBuildDecoration(random, this.core.getPopData());
        }

    }

    public void decorateAwkwardCorner(@NotNull Wall target,
                                      @NotNull Random random,
                                      @NotNull BlockFace one,
                                      @NotNull BlockFace two)
    {
        Material[] cobblestone = {Material.COBBLESTONE, Material.MOSSY_COBBLESTONE};
        Material[] stoneBricks = {
                Material.STONE_BRICKS,
                Material.STONE_BRICKS,
                Material.STONE_BRICKS,
                Material.CRACKED_STONE_BRICKS
        };

        // Corner and corner spires
        target.Pillar(5, random, BlockUtils.stoneBricks);

        target.getDown().downUntilSolid(random, cobblestone);

        target = target.getUp();

        // Areas next to the corner. Decorative.
        target.getRelative(one).Pillar(3, random, stoneBricks);
        new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(one.getOppositeFace())
                                                     .apply(target.getRelative(one).getUp(3));

        target.getRelative(two).Pillar(3, random, stoneBricks);
        new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(two.getOppositeFace())
                                                     .apply(target.getRelative(two).getUp(3));

        // Solid platform underneath in case of uneven ground.
        target = target.getDown();
        target.getRelative(one).downUntilSolid(random, cobblestone);
        target.getRelative(two).downUntilSolid(random, cobblestone);
    }
}
