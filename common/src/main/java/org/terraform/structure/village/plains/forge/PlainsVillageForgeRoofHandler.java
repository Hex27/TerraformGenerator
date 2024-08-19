package org.terraform.structure.village.plains.forge;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.SlabBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

public class PlainsVillageForgeRoofHandler {


    private static final int pieceWidth = 5;

    public static void placeRoof(@NotNull PlainsVillagePopulator plainsVillagePopulator,
                                 @NotNull SimpleBlock core,
                                 @NotNull ArrayList<SimpleLocation> rectangleLocations)
    {
        SimpleLocation lowerBound = null;
        SimpleLocation upperBound = null;

        Material roofCornerMaterial = GenUtils.randChoice(Material.STONE_BRICKS, Material.COBBLESTONE);
        Material roofSlabCornerMaterial = Material.STONE_BRICK_SLAB;
        if (roofCornerMaterial == Material.COBBLESTONE) {
            roofSlabCornerMaterial = Material.COBBLESTONE_SLAB;
        }

        for (SimpleLocation sLoc : rectangleLocations) {
            if (lowerBound == null) {
                lowerBound = sLoc;
            }
            if (upperBound == null) {
                upperBound = sLoc;
            }

            if (lowerBound.getX() >= sLoc.getX() && lowerBound.getZ() >= sLoc.getZ()) {
                lowerBound = sLoc;
            }

            if (upperBound.getX() <= sLoc.getX() && upperBound.getZ() <= sLoc.getZ()) {
                upperBound = sLoc;
            }
        }

        lowerBound = lowerBound.getRelative(-4, 0, -4);
        upperBound = upperBound.getRelative(4, 0, 4);
        Axis roofAxis = Axis.X;
        if (upperBound.getZ() - lowerBound.getZ() > upperBound.getX() - lowerBound.getX()) {
            roofAxis = Axis.Z;
        }

        for (int x = lowerBound.getX(); x <= upperBound.getX(); x++) {
            for (int z = lowerBound.getZ(); z <= upperBound.getZ(); z++) {
                // core.getPopData().setType(x, core.getY()+4, z, Material.RED_WOOL);
                int height;
                double percent;
                if (roofAxis == Axis.X) {
                    // 0 to 1.0
                    percent = ((double) x - (double) lowerBound.getX()) / ((double) upperBound.getX()
                                                                           - (double) lowerBound.getX());
                    // -12(x-0.5)^2+3
                }
                else {
                    percent = ((double) z - (double) lowerBound.getZ()) / ((double) upperBound.getZ()
                                                                           - (double) lowerBound.getZ());
                    // -12(x-0.5)^2+3
                }
                height = (int) ((-12 * Math.pow(percent - 0.5, 2) + 3));
                SimpleBlock target = new SimpleBlock(core.getPopData(),
                        x,
                        core.getY() + 4 + ((int) (((double) height) / 2)),
                        z);
                if (height % 2 == 1) { // Use solid blocks for odd heights
                    if (x == lowerBound.getX()
                        || x == upperBound.getX()
                        || z == lowerBound.getZ()
                        || z == upperBound.getZ())
                    {
                        target.setType(roofCornerMaterial);

                        Wall adj = new Wall(target, BlockUtils.getBlockFaceFromAxis(roofAxis));
                        new SlabBuilder(roofSlabCornerMaterial).setType(Type.TOP)
                                                               .lapply(adj.getFront())
                                                               .lapply(adj.getRear());
                    }
                    else {
                        target.setType(plainsVillagePopulator.woodPlank);
                    }

                    // Log center
                    if (height == 3) {
                        new OrientableBuilder(plainsVillagePopulator.woodLog).setAxis(BlockUtils.getPerpendicularHorizontalPlaneAxis(
                                roofAxis)).apply(target);

                        if ((roofAxis == Axis.X && (z == lowerBound.getZ() + 1 || z == upperBound.getZ() - 1)) || (
                                roofAxis == Axis.Z
                                && (x == lowerBound.getX() + 1 || x == upperBound.getX() - 1)))
                        {

                            // Fill the holes between the walls and the roof
                            if (BlockUtils.isStoneLike(target.getDown(2).getType())
                                || target.getDown(2).getType() == plainsVillagePopulator.woodDoor)
                            {
                                new Wall(target.getDown()).downUntilSolid(new Random(),
                                        Material.STONE,
                                        Material.COBBLESTONE,
                                        Material.ANDESITE
                                );
                            }
                        }
                    }

                }
                else { // Use slabs for even heights
                    if (x == lowerBound.getX()
                        || x == upperBound.getX()
                        || z == lowerBound.getZ()
                        || z == upperBound.getZ())
                    {
                        new SlabBuilder(roofSlabCornerMaterial).apply(target);
                    }
                    else {
                        new SlabBuilder(plainsVillagePopulator.woodSlab).apply(target);
                    }
                }

            }
        }
    }

    /**
     * @return a list of structure piece simplelocations that are contained within a rectangle.
     * This rectangle may not be the largest in the provided hashmap.
     */
    public static @NotNull ArrayList<SimpleLocation> identifyRectangle(@NotNull HashMap<SimpleLocation, JigsawStructurePiece> pieces) {
        ArrayList<SimpleLocation> rectangleList = new ArrayList<>();
        SimpleLocation cornerLoc = null;
        for (SimpleLocation loc : pieces.keySet()) {
            cornerLoc = loc;
            break;
        }

        // Find a corner of the pieces
        BlockFace sideToMove = BlockUtils.getDirectBlockFace(new Random());
        JigsawStructurePiece target = getAdjacentPiece(pieces, cornerLoc, sideToMove);
        while (target != null) {
            cornerLoc = target.getRoom().getSimpleLocation();
            target = getAdjacentPiece(pieces, cornerLoc, sideToMove);
        }
        target = pieces.get(cornerLoc);

        // Begin moving towards the opposite side. Add all relevant entries to the list.
        while (target != null) {
            cornerLoc = target.getRoom().getSimpleLocation();
            rectangleList.add(target.getRoom().getSimpleLocation());
            target = getAdjacentPiece(pieces, cornerLoc, sideToMove.getOppositeFace());
        }

        // For all entries in the list, find the maximum expansion towards one side.
        sideToMove = BlockUtils.getTurnBlockFace(new Random(), sideToMove);
        int shortestLength = 99;
        for (SimpleLocation pLoc : rectangleList) {
            JigsawStructurePiece piece = pieces.get(pLoc);
            int expansionLength = 0;
            piece = getAdjacentPiece(pieces, piece.getRoom().getSimpleLocation(), sideToMove);
            while (piece != null) {
                expansionLength++;
                piece = getAdjacentPiece(pieces, piece.getRoom().getSimpleLocation(), sideToMove);
            }
            if (expansionLength < shortestLength) {
                shortestLength = expansionLength;
            }
            if (expansionLength == 0) {
                break; // Shortest side is 0. Break out.
            }
        }

        // Add all associated pieces to the list.
        Collection<SimpleLocation> toAdd = new ArrayList<>();
        for (SimpleLocation pLoc : rectangleList) {
            JigsawStructurePiece piece = pieces.get(pLoc);
            piece = getAdjacentPiece(pieces, piece.getRoom().getSimpleLocation(), sideToMove);
            for (int i = 0; i < shortestLength; i++) {
                toAdd.add(piece.getRoom().getSimpleLocation());
                piece = getAdjacentPiece(pieces, piece.getRoom().getSimpleLocation(), sideToMove);
            }
        }

        rectangleList.addAll(toAdd);
        return rectangleList;
    }

    private static JigsawStructurePiece getAdjacentPiece(@NotNull HashMap<SimpleLocation, JigsawStructurePiece> pieces,
                                                         @NotNull SimpleLocation loc,
                                                         @NotNull BlockFace face)
    {
        SimpleLocation other = new SimpleLocation(loc.getX() + face.getModX() * pieceWidth,
                loc.getY() + face.getModY() * pieceWidth,
                loc.getZ() + face.getModZ() * pieceWidth
        );
        return pieces.get(other);
    }
}
