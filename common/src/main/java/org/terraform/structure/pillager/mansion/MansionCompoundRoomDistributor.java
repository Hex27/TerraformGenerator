package org.terraform.structure.pillager.mansion;

import com.google.common.collect.Maps;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleLocation;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.*;

/**
 * Mansions will distribute room populators long after JigsawStructureBuilder's main
 * operations are done.
 * <br>
 * This is because we want to merge some of the rooms into larger rooms.
 * These rooms will be referred to as Compound Rooms.
 * <br>
 * Mansions will always have 1 3x3 room that extends to the second story. This is the
 * stairway.
 * <br>
 * Depending on RNG and size, mansions will also try to generate some 2x2 rooms
 * and 2x1 (or 1x2) rooms.
 */
public class MansionCompoundRoomDistributor {

    // A map of populators and their respective room areas
    public static final @NotNull HashMap<MansionRoomSize, ArrayList<MansionRoomPopulator>> groundFloorPopulators =
            Maps.newHashMap(Map.of(
        new MansionRoomSize(3, 3), MansionRoomPopulatorRegistry.GROUND_3_3.getPopulators(),
        new MansionRoomSize(2, 2), MansionRoomPopulatorRegistry.GROUND_2_2.getPopulators(),
        new MansionRoomSize(1, 2), MansionRoomPopulatorRegistry.GROUND_1_2.getPopulators(),
        new MansionRoomSize(2, 1), MansionRoomPopulatorRegistry.GROUND_2_1.getPopulators(),
        new MansionRoomSize(1, 1), MansionRoomPopulatorRegistry.GROUND_1_1.getPopulators()
    ));

    public static final @NotNull HashMap<MansionRoomSize, ArrayList<MansionRoomPopulator>> secondFloorPopulators =
            Maps.newHashMap(Map.of(
        new MansionRoomSize(3, 3), MansionRoomPopulatorRegistry.SECOND_3_3.getPopulators(),
        new MansionRoomSize(2, 2), MansionRoomPopulatorRegistry.SECOND_2_2.getPopulators(),
        new MansionRoomSize(1, 2), MansionRoomPopulatorRegistry.SECOND_1_2.getPopulators(),
        new MansionRoomSize(2, 1), MansionRoomPopulatorRegistry.SECOND_2_1.getPopulators(),
        new MansionRoomSize(1, 1), MansionRoomPopulatorRegistry.SECOND_1_1.getPopulators()
    ));

    public static void distributeRooms(@NotNull Collection<JigsawStructurePiece> pieces,
                                       @NotNull Random random,
                                       boolean isGround)
    {

        Map<MansionRoomSize, ArrayList<MansionRoomPopulator>> activeRoomPool;
        ArrayList<JigsawStructurePiece> shuffledList = new ArrayList<>(pieces);

        ArrayList<MansionRoomSize> potentialRoomSizes = new ArrayList<>();
        int occupiedCells = 13;
        if (isGround) {
            activeRoomPool = groundFloorPopulators;
            potentialRoomSizes.add(new MansionRoomSize(3, 3)); // Stairway Room
        }
        else {
            activeRoomPool = secondFloorPopulators;
        }
        potentialRoomSizes.add(new MansionRoomSize(2, 2)); // At least one 2x2 room

        while ((double) occupiedCells / pieces.size() < 0.7 || GenUtils.chance(
                random,
                pieces.size() - occupiedCells / 4,
                pieces.size()
        )) {
            if ((double) occupiedCells / pieces.size() < 0.5 && GenUtils.chance(random, 1, 3)) {
                occupiedCells += 4;
                potentialRoomSizes.add(new MansionRoomSize(2, 2));
            }
            else {
                occupiedCells += 2;
                if (random.nextBoolean()) {
                    potentialRoomSizes.add(new MansionRoomSize(2, 1));
                }
                else {
                    potentialRoomSizes.add(new MansionRoomSize(1, 2));
                }
            }
        }

        // Iterate this way because index 0 is the 3x3 room which we want.
        for (MansionRoomSize roomSize : potentialRoomSizes) {
            Collections.shuffle(shuffledList);
            for (JigsawStructurePiece piece : shuffledList) {
                // Force every room to generate at least once before generating duplicate types
                Collections.shuffle(activeRoomPool.get(roomSize), random);
                ArrayList<MansionRoomPopulator> populators = activeRoomPool.get(roomSize);
                if (populators.isEmpty()) {
                    activeRoomPool.put(
                            roomSize,
                            MansionRoomPopulatorRegistry.getByRoomSize(roomSize, isGround).getPopulators()
                    );
                    populators = activeRoomPool.get(roomSize);
                }
                MansionRoomPopulator populator = populators.get(0)
                                                           .getInstance(
                                                                   piece.getRoom(),
                                                                   ((MansionStandardRoomPiece) piece).internalWalls
                                                           );
                if (canRoomSizeFitWithCenter((MansionStandardRoomPiece) piece, pieces, roomSize, populator, false)) {
                    // Shuffle and distribute populator
                    TerraformGeneratorPlugin.logger.info(populator.getClass().getSimpleName()
                                                         + " generating at "
                                                         + piece.getRoom().getSimpleLocation());
                    ((MansionStandardRoomPiece) piece).setRoomPopulator(populator); // set the populator;

                    // If successful, remove the populator from the active pool.
                    populators.remove(0);
                    break;
                }
            }
        }

        // Fill the rest of the rooms with 1x1 rooms
        for (JigsawStructurePiece piece : pieces) {
            MansionRoomSize roomSize = new MansionRoomSize(1, 1);
            if (((MansionStandardRoomPiece) piece).getRoomPopulator() == null) {
                Collections.shuffle(activeRoomPool.get(roomSize), random);
                MansionRoomPopulator populator = activeRoomPool.get(roomSize)
                                                               .get(0)
                                                               .getInstance(
                                                                       piece.getRoom(),
                                                                       ((MansionStandardRoomPiece) piece).internalWalls
                                                               );
                TerraformGeneratorPlugin.logger.info(populator.getClass().getSimpleName()
                                                     + " generating at "
                                                     + piece.getRoom().getSimpleLocation());
                ((MansionStandardRoomPiece) piece).setRoomPopulator(populator); // set the populator;
            }

        }
    }


    /**
     * Also sets the needed rooms to empty room populator and knocks down relevant walls
     * if the return value is true.
     *
     * @param pieces NOT TO BE MODIFIED
     */
    public static boolean canRoomSizeFitWithCenter(@NotNull MansionStandardRoomPiece piece,
                                                   @NotNull Collection<JigsawStructurePiece> pieces,
                                                   @NotNull MansionRoomSize roomSize,
                                                   @NotNull MansionRoomPopulator defaultPopulator,
                                                   boolean force)
    {

        SimpleLocation center = piece.getRoom().getSimpleLocation();

        ArrayList<SimpleLocation> relevantLocations = new ArrayList<>();
        relevantLocations.add(center);
        // Positive X
        if (roomSize.getWidthX() == 2) {
            relevantLocations.add(center.getRelative(BlockFace.EAST, MansionJigsawBuilder.groundFloorRoomWidth));
        }

        // Positive Z
        if (roomSize.getWidthZ() == 2) {
            relevantLocations.add(center.getRelative(BlockFace.SOUTH, MansionJigsawBuilder.groundFloorRoomWidth));
        }

        // Corner for 2x2 rooms
        if (roomSize.getWidthZ() == 2 && roomSize.getWidthX() == 2) {
            relevantLocations.add(center.getRelative(BlockFace.SOUTH_EAST, MansionJigsawBuilder.groundFloorRoomWidth));
        }

        // 3x3 room
        if (roomSize.getWidthX() == 3 && roomSize.getWidthZ() == 3) {
            for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
                relevantLocations.add(center.getRelative(face, MansionJigsawBuilder.groundFloorRoomWidth));
            }
        }

        int hits = 0;
        // First pass, if any rooms are occupied, return false.
        for (JigsawStructurePiece p : pieces) {
            if (relevantLocations.contains(p.getRoom().getSimpleLocation())) {
                // If force is on, only care if the piece exists in pieces, not if the
                // piece is occupied.
                if (!force && ((MansionStandardRoomPiece) p).getRoomPopulator() != null) {
                    return false;
                }
                hits++;
            }
        }

        // Should not return false when force is true
        // Caller should ensure that.
        if (hits < relevantLocations.size()) {
            return false;
        }

        // Second pass, knock down walls and set all rooms to occupied.
        // Center room will be set by calling code.
        for (JigsawStructurePiece p : pieces) {
            if (relevantLocations.contains(p.getRoom().getSimpleLocation())) {
                MansionStandardRoomPiece spiece = ((MansionStandardRoomPiece) p);
                spiece.setRoomPopulator(defaultPopulator.getInstance(p.getRoom(), spiece.internalWalls), false);
                for (BlockFace face : spiece.adjacentPieces.keySet()) {
                    if (relevantLocations.contains(spiece.adjacentPieces.get(face).getRoom().getSimpleLocation())) {
                        spiece.internalWalls.remove(face); // Knock down walls to join rooms
                        spiece.adjacentPieces.get(face).internalWalls.remove(face.getOppositeFace());
                    }
                }
            }
        }

        return true;
    }
}
