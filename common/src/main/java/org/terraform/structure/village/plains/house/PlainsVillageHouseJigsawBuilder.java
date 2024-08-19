package org.terraform.structure.village.plains.house;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class PlainsVillageHouseJigsawBuilder extends JigsawBuilder {
    final PlainsVillageHouseVariant var;
    final PlainsVillagePopulator plainsVillagePopulator;

    public PlainsVillageHouseJigsawBuilder(PlainsVillagePopulator plainsVillagePopulator,
                                           int widthX,
                                           int widthZ,
                                           @NotNull PopulatorDataAbstract data,
                                           int x,
                                           int y,
                                           int z)
    {
        super(widthX, widthZ, data, x, y, z);
        this.plainsVillagePopulator = plainsVillagePopulator;
        this.var = PlainsVillageHouseVariant.roll(new Random());
        this.pieceRegistry = new JigsawStructurePiece[] {
                new PlainsVillageBedroomPiece(
                        plainsVillagePopulator,
                        var,
                        5,
                        3,
                        5,
                        JigsawType.STANDARD,
                        BlockUtils.directBlockFaces
                ),
                new PlainsVillageKitchenPiece(plainsVillagePopulator,
                        var,
                        5,
                        3,
                        5,
                        JigsawType.STANDARD,
                        BlockUtils.directBlockFaces
                ),
                new PlainsVillageLibraryPiece(plainsVillagePopulator,
                        var,
                        5,
                        3,
                        5,
                        JigsawType.STANDARD,
                        BlockUtils.directBlockFaces
                ),
                new PlainsVillageWallPiece(plainsVillagePopulator,
                        var,
                        5,
                        3,
                        5,
                        JigsawType.END,
                        BlockUtils.directBlockFaces
                ),
                new PlainsVillageEntrancePiece(plainsVillagePopulator,
                        var,
                        5,
                        3,
                        5,
                        JigsawType.ENTRANCE,
                        BlockUtils.directBlockFaces
                )
        };
        this.chanceToAddNewPiece = 30;
    }

    @Override
    public @NotNull JigsawStructurePiece getFirstPiece(@NotNull Random random) {
        return new PlainsVillageBedroomPiece(
                plainsVillagePopulator,
                var,
                5,
                3,
                5,
                JigsawType.STANDARD,
                BlockUtils.directBlockFaces
        );
    }

    @Override
    public void build(@NotNull Random random) {
        super.build(random);

        // Make sure awkward corners are fixed
        for (JigsawStructurePiece piece : this.pieces.values()) {
            SimpleBlock core = new SimpleBlock(this.core.getPopData(),
                    piece.getRoom().getX(),
                    piece.getRoom().getY(),
                    piece.getRoom().getZ()
            );
            Wall target;
            Material[] fenceType = {plainsVillagePopulator.woodFence};
            Material cornerType = plainsVillagePopulator.woodLog;
            if (this.var == PlainsVillageHouseVariant.COBBLESTONE) {
                fenceType = new Material[] {Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL};
            }
            else if (this.var == PlainsVillageHouseVariant.CLAY) {
                fenceType = new Material[] {Material.STONE_BRICK_WALL, Material.MOSSY_STONE_BRICK_WALL};
                cornerType = plainsVillagePopulator.woodStrippedLog;
            }
            if (piece.getWalledFaces().contains(BlockFace.NORTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.WEST))
            { // nw
                target = new Wall(core.getRelative(-3, 0, -3));
                decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.WEST, cornerType, fenceType);
            }
            if (piece.getWalledFaces().contains(BlockFace.NORTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.EAST))
            { // ne
                target = new Wall(core.getRelative(3, 0, -3));
                decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.EAST, cornerType, fenceType);
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.WEST))
            { // sw
                target = new Wall(core.getRelative(-3, 0, 3));
                decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.WEST, cornerType, fenceType);
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH) && piece.getWalledFaces()
                                                                         .contains(BlockFace.EAST))
            { // se
                target = new Wall(core.getRelative(3, 0, 3));
                decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.EAST, cornerType, fenceType);
            }
        }

        // Place the roof
        if (!PlainsVillageRoofHandler.isRectangle(this)) {
            PlainsVillageRoofHandler.placeStandardRoof(plainsVillagePopulator, this);
        }
        else {
            PlainsVillageRoofHandler.placeTentRoof(plainsVillagePopulator, random, this);
        }

        // Decorate rooms and walls
        for (JigsawStructurePiece piece : this.pieces.values()) {
            piece.postBuildDecoration(random, this.core.getPopData());
        }

    }

    public void decorateAwkwardCorner(@NotNull Wall target,
                                      @NotNull Random random,
                                      @NotNull BlockFace one,
                                      @NotNull BlockFace two,
                                      Material cornerType,
                                      Material[] fenceType)
    {
        target.Pillar(4, random, cornerType);
        target.getDown().downUntilSolid(random, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        target = target.getUp();
        target.getRelative(one).Pillar(3, random, fenceType);
        target.getRelative(two).Pillar(3, random, fenceType);
        target.getRelative(one).CorrectMultipleFacing(3);
        target.getRelative(two).CorrectMultipleFacing(3);
        target = target.getDown();
        target.getRelative(one).downUntilSolid(random, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        target.getRelative(two).downUntilSolid(random, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
    }

    public PlainsVillageHouseVariant getVariant() {
        return var;
    }

}
