package org.terraform.structure.pillager.mansion;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.TrapdoorBuilder;
import org.terraform.utils.version.V_1_19;
import org.terraform.utils.version.Version;

import java.util.Random;

public class MansionRoofHandler {

    /**
     * Gets the largest possible rectangle that the house's shape can offer
     * Doesn't seem to work all the time though
     */
    public static int[][] getLargestRectangle(@NotNull MansionJigsawBuilder builder) {
        int[] lowestCoords = null;
        int[] highestCoords = null;

        for (JigsawStructurePiece piece : builder.getPieces().values()) {
            if (lowestCoords == null) {
                lowestCoords = new int[] {piece.getRoom().getX(), piece.getRoom().getZ()};
            }
            if (highestCoords == null) {
                highestCoords = new int[] {piece.getRoom().getX(), piece.getRoom().getZ()};
            }
            if (piece.getRoom().getX() < lowestCoords[0]) {
                lowestCoords[0] = piece.getRoom().getX();
            }
            if (piece.getRoom().getZ() < lowestCoords[1]) {
                lowestCoords[1] = piece.getRoom().getZ();
            }

            if (piece.getRoom().getX() > highestCoords[0]) {
                highestCoords[0] = piece.getRoom().getX();
            }
            if (piece.getRoom().getZ() > highestCoords[1]) {
                highestCoords[1] = piece.getRoom().getZ();
            }
        }

        int previousNotInRect = getNumberOfPiecesNotInRectangle(builder, lowestCoords, highestCoords);
        int i = 0;
        int stall = 0;
        // Shrink the rectangle one side at a time until it is a rectangle
        // If the shrink operation did not change the number of pieces not in the rectangle,
        // then undo the shrink.
        while (previousNotInRect != 0) {
            int piecesInRect = 0;
            switch (i % 4) {
                case 0:
                    lowestCoords[0] += MansionJigsawBuilder.groundFloorRoomWidth;
                    piecesInRect = getNumberOfPiecesNotInRectangle(builder, lowestCoords, highestCoords);
                    if (piecesInRect == previousNotInRect) {
                        stall++;
                        if (stall < 4) {
                            lowestCoords[0] -= MansionJigsawBuilder.groundFloorRoomWidth;
                        }
                        else {
                            stall = 0;
                        }
                    }
                    break;
                case 1:
                    lowestCoords[1] += MansionJigsawBuilder.groundFloorRoomWidth;
                    piecesInRect = getNumberOfPiecesNotInRectangle(builder, lowestCoords, highestCoords);
                    if (piecesInRect == previousNotInRect) {
                        stall++;
                        if (stall < 4) {
                            lowestCoords[1] -= MansionJigsawBuilder.groundFloorRoomWidth;
                        }
                        else {
                            stall = 0;
                        }
                    }
                    break;
                case 2:
                    highestCoords[0] -= MansionJigsawBuilder.groundFloorRoomWidth;
                    piecesInRect = getNumberOfPiecesNotInRectangle(builder, lowestCoords, highestCoords);
                    if (piecesInRect == previousNotInRect) {
                        stall++;
                        if (stall < 4) {
                            highestCoords[0] += MansionJigsawBuilder.groundFloorRoomWidth;
                        }
                        else {
                            stall = 0;
                        }
                    }
                    break;
                case 3:
                    highestCoords[1] -= MansionJigsawBuilder.groundFloorRoomWidth;
                    piecesInRect = getNumberOfPiecesNotInRectangle(builder, lowestCoords, highestCoords);
                    if (piecesInRect == previousNotInRect) {
                        stall++;
                        if (stall < 4) {
                            highestCoords[1] += MansionJigsawBuilder.groundFloorRoomWidth;
                        }
                        else {
                            stall = 0;
                        }
                    }
                    break;
            }
            previousNotInRect = piecesInRect;
            i++;
        }
        int y = builder.getCore().getY();
        for (int x = lowestCoords[0]; x <= highestCoords[0]; x += builder.getPieceWidth()) {
            for (int z = lowestCoords[1]; z <= highestCoords[1]; z += builder.getPieceWidth()) {
                if (builder.getPieces().containsKey(new SimpleLocation(x, y, z))) {
                    builder.getRoofedLocations().add(new SimpleLocation(x, y + MansionJigsawBuilder.roomHeight + 1, z));
                }
            }
        }
        return new int[][] {lowestCoords, highestCoords};
    }

    private static int getNumberOfPiecesNotInRectangle(@NotNull MansionJigsawBuilder builder,
                                                       int @NotNull [] lowestCoords,
                                                       int @NotNull [] highestCoords)
    {
        int y = builder.getCore().getY();
        int notInRect = 0;
        for (int x = lowestCoords[0]; x <= highestCoords[0]; x += builder.getPieceWidth()) {
            for (int z = lowestCoords[1]; z <= highestCoords[1]; z += builder.getPieceWidth()) {
                if (!builder.getPieces().containsKey(new SimpleLocation(x, y, z))) {
                    notInRect++;
                }
            }
        }

        // If all pieces accounted for by looping the coords, this is a rectangle.
        return notInRect;
    }

    public static @NotNull Axis getDominantAxis(int @NotNull [] lowestCoords, int @NotNull [] highestCoords) {
        Axis superiorAxis;
        // Longer axis is the superior one
        if (highestCoords[0] - lowestCoords[0] > highestCoords[1] - lowestCoords[1]) {
            superiorAxis = Axis.X;
        }
        else if (highestCoords[0] - lowestCoords[0] < highestCoords[1] - lowestCoords[1]) {
            superiorAxis = Axis.Z;
        }
        else // Square house
        {
            superiorAxis = Axis.X;
        }

        return superiorAxis;
    }

    public static @NotNull BlockFace getDominantBlockFace(int @NotNull [] lowestCoords, int @NotNull [] highestCoords) {
        BlockFace superiorAxis;
        // Longer axis is the superior one
        if (highestCoords[0] - lowestCoords[0] > highestCoords[1] - lowestCoords[1]) {
            superiorAxis = BlockFace.WEST;
        }
        else if (highestCoords[0] - lowestCoords[0] < highestCoords[1] - lowestCoords[1]) {
            superiorAxis = BlockFace.NORTH;
        }
        else // Square house
        {
            superiorAxis = BlockFace.WEST;
        }

        return superiorAxis;
    }

    /**
     *
     */
    public static void placeTentRoof(Random rand, @NotNull MansionJigsawBuilder builder, int[][] bounds) {
        Axis superiorAxis;
        PopulatorDataAbstract data = builder.getCore().getPopData();

        int highestY = -1;

        int[] lowestCoords = bounds[0];
        int[] highestCoords = bounds[1];

        // RoofY
        // Lol idk why 4
        int y = builder.getCore().getY() + 2 * MansionJigsawBuilder.roomHeight + 4;

        superiorAxis = getDominantAxis(lowestCoords, highestCoords);

        lowestCoords[0] -= 5;
        lowestCoords[1] -= 5;
        highestCoords[0] += 5;
        highestCoords[1] += 5;

        Wall w;
        int length;
        int breadth;
        if (superiorAxis == Axis.X) {
            length = highestCoords[0] - lowestCoords[0] + 5;
            breadth = (highestCoords[1] - lowestCoords[1]) + 3;
            w = new Wall(new SimpleBlock(data, highestCoords[0] + 2, y - 1, lowestCoords[1] - 1), BlockFace.WEST);
        }
        else {
            length = highestCoords[1] - lowestCoords[1] + 5;
            breadth = (highestCoords[0] - lowestCoords[0]) + 3;
            w = new Wall(new SimpleBlock(data, lowestCoords[0] - 1, y - 1, lowestCoords[1] - 2), BlockFace.SOUTH);
        }

        for (int i = 0; i < length; i++) {
            Wall target = w;
            boolean ascendBlock = false;
            for (int right = 0; right < breadth - 1; right++) {

                // Place logs at the sides
                if (right != 0 && right != breadth - 1) {
                    if (i == 0 || i == length - 1) {
                        // Sandwiched by trapdoors
                        new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR).setHalf(Half.TOP)
                                                                       .setOpen(true)
                                                                       .setFacing(i == 0
                                                                                  ? target.getDirection()
                                                                                          .getOppositeFace()
                                                                                  : target.getDirection())
                                                                       .lapply(target.getDown());
                    }
                    else {
                        if (target.getDown(2).getType() != Material.DARK_OAK_PLANKS) {
                            new OrientableBuilder(Material.DARK_OAK_LOG).setAxis(superiorAxis)
                                                                        .apply(target.getDown().get());
                        }

                        // Connect the roof to the walls below
                        if (i == 2 || i == length - 3) {
                            // Lower Walls
                            Wall bottom = target.getAtY(builder.getCore().getY()
                                                        + 2 * MansionJigsawBuilder.roomHeight
                                                        + 2);
                            if (BlockUtils.isAir(bottom.getType())
                                || Tag.STAIRS.isTagged(bottom.getType())
                                || Tag.SLABS.isTagged(bottom.getType()))
                            {
                                bottom.setType(Material.DARK_OAK_PLANKS);
                            }
                            target.getDown(2)
                                  .downPillar(new Random(), target.getY() - bottom.getY() - 2, bottom.getType());
                        }
                        else if (i != 1 && i != length - 2)// Force set air for things below the roof within the walls
                        {
                            target.getDown(2).downPillar(new Random(), target.getY() - y + 1, Material.AIR);
                        }
                    }
                }

                Material slabType = Material.DARK_OAK_SLAB;

                if (right == 0 || right == breadth - 2 || i == 0 || i == length - 1) {
                    slabType = Material.COBBLESTONE_SLAB;
                }

                if (breadth % 2 == 1) { // For odd breadth.
                    if (right > breadth / 2) {
                        // Slope down
                        attemptReplaceSlab(slabType, target, ascendBlock ? Type.BOTTOM : Type.DOUBLE);
                        if (ascendBlock) {
                            target = target.getRight().getDown();
                            ascendBlock = false;
                        }
                        else {
                            target = target.getRight();
                            ascendBlock = true;
                        }
                    }
                    else if (right < breadth / 2) {
                        // Slope up
                        attemptReplaceSlab(slabType, target, ascendBlock ? Type.DOUBLE : Type.BOTTOM);
                        if (ascendBlock) {
                            target = target.getRight().getUp();
                            ascendBlock = false;
                        }
                        else {
                            target = target.getRight();
                            ascendBlock = true;
                        }
                    }
                    else {
                        // Top (Only exists when the breadth is odd.
                        highestY = target.getY();
                        target.setType(slabType);
                        if (ascendBlock) {
                            target = target.getRight().getDown();
                            ascendBlock = false;
                        }
                        else {
                            target = target.getRight();
                            ascendBlock = true;
                        }
                    }
                }
                else { // For even breadth
                    if (right == breadth / 2 - 1) {
                        highestY = target.getY();
                        target.setType(Material.DARK_OAK_PLANKS);
                        if (slabType == Material.COBBLESTONE_SLAB) {
                            target.setType(Material.COBBLESTONE);
                        }
                        target = target.getRight();
                    }
                    else if (right >= breadth / 2) {
                        // Slope down
                        attemptReplaceSlab(slabType, target, ascendBlock ? Type.BOTTOM : Type.DOUBLE);
                        if (ascendBlock) {
                            target = target.getRight().getDown();
                            ascendBlock = false;
                        }
                        else {
                            target = target.getRight();
                            ascendBlock = true;
                        }
                    }
                    else if (right < breadth / 2) {
                        // Slope up
                        attemptReplaceSlab(slabType, target, ascendBlock ? Type.DOUBLE : Type.BOTTOM);
                        if (ascendBlock) {
                            target = target.getRight().getUp();
                            ascendBlock = false;
                        }
                        else {
                            target = target.getRight();
                            ascendBlock = true;
                        }
                    }
                }
            }
            w = w.getFront();
        }

    }

    private static void attemptReplaceSlab(@NotNull Material slabType, @NotNull Wall w, @NotNull Type type) {
        if (!w.isSolid()) {
            if (w.findCeiling(5) != null) {
                return;
            }
            new SlabBuilder(slabType).setType(type).lapply(w);
        }
        else if (Tag.STAIRS.isTagged(w.getType()) || Tag.SLABS.isTagged(w.getType())) {
            w.setType(Material.DARK_OAK_PLANKS);
        }
    }

    public static void atticDecorations(@NotNull Random rand,
                                        @NotNull PopulatorDataAbstract data,
                                        @NotNull JigsawStructurePiece piece)
    {
        SimpleBlock core = piece.getRoom().getCenterSimpleBlock(data).getUp(8);

        if (!core.isSolid()) {
            Wall ceiling = new Wall(core).getUp().findCeiling(15);
            if (ceiling == null) {
                return;
            }
            if (ceiling.getType().toString().contains("DARK_OAK")) {
                ceiling = ceiling.getDown();
                int chainLength = ceiling.getY() - core.getY() - 2 - rand.nextInt(3);
                if (chainLength < 0) {
                    chainLength = 0;
                }
                ceiling.downPillar(chainLength, Material.CHAIN);
                Lantern lantern = (Lantern) Bukkit.createBlockData(Material.LANTERN);
                lantern.setHanging(true);
                ceiling.getDown(chainLength).setBlockData(lantern);
                if (ceiling.getY() - core.getY() > 5) {
                    // If height is high enough, spawn a couple of cave spiders and spiders
                    if (GenUtils.chance(rand, 1, 2)) {
                        data.addEntity(core.getX(), core.getY(), core.getZ(), EntityType.SPIDER);
                    }
                    if (GenUtils.chance(rand, 1, 2)) {
                        data.addEntity(core.getX(), core.getY(), core.getZ(), EntityType.CAVE_SPIDER);
                    }

                    // If lucky, spawn allays
                    if (Version.isAtLeast(19) && rand.nextBoolean()) {
                        for (int i = 0; i < 1 + rand.nextInt(3); i++) {
                            data.addEntity(core.getX(), core.getY(), core.getZ(), V_1_19.ALLAY);
                        }
                    }


                }
            }
        }

        for (int[] loc : piece.getRoom().getAllCorners(2)) {
            SimpleBlock target = new SimpleBlock(data, loc[0], core.getY(), loc[1]);
            Wall ceiling = new Wall(target).findCeiling(15);
            // Height more than 1
            if (ceiling != null && ceiling.getY() > target.getY() + 1) {
                ceiling.getDown().downUntilSolid(new Random(), Material.DARK_OAK_LOG);
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    // Webs
                    if (GenUtils.chance(rand, 1, 8)) {
                        ceiling.getDown().getRelative(face).get().lsetType(Material.COBWEB);
                    }
                }

                // Small chance for chests against pillars
                if (GenUtils.chance(rand, 1, 20)) {
                    BlockFace f = BlockUtils.getDirectBlockFace(rand);
                    if (!target.getRelative(f).isSolid()) {
                        new ChestBuilder(Material.CHEST).setFacing(f)
                                                        .setLootTable(TerraLootTable.WOODLAND_MANSION)
                                                        .apply(target.getRelative(f));
                    }
                }

            }
        }
    }

}
