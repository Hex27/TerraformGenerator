package org.terraform.structure.pillager.mansion;

import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.*;
import java.util.Map.Entry;

public class MansionMazeAlgoUtil {

    private static @Nullable MansionStandardRoomPiece getStartingPiece(@NotNull Collection<JigsawStructurePiece> pieces) {
        for (JigsawStructurePiece p : pieces) {
            return (MansionStandardRoomPiece) p;
        }
        return null;
    }

    public static void setupPathways(@NotNull Collection<JigsawStructurePiece> pieces, @NotNull Random rand) {
        // Total number of cells
        int n = pieces.size();

        Stack<MansionStandardRoomPiece> cellStack = new Stack<>();
        MansionStandardRoomPiece currentCell = getStartingPiece(pieces);
        // Total number of visited cells during maze construction
        int nv = 1;

        // Knock down walls until all cells have been visited before
        while (nv < n) {
            // Bukkit.getLogger().info("CurrentCell: " + currentCell.x + "," + currentCell.z);
            Map<BlockFace, MansionStandardRoomPiece> neighbours = getValidNeighbours(pieces, currentCell);

            if (neighbours.isEmpty()) {
                // Dead end. Go backwards.

                // No items in stack, break out.
                if (cellStack.isEmpty()) {
                    break;
                }
                currentCell = cellStack.pop();
                continue;
            }

            // choose a random neighbouring cell and move into it.
            @SuppressWarnings("unchecked") Entry<BlockFace, MansionStandardRoomPiece> entry = (Entry<BlockFace, MansionStandardRoomPiece>) neighbours.entrySet()
                                                                                                                                                     .toArray()[rand.nextInt(
                    neighbours.size())];

            // currentCell.knockDownWall(entry.getValue(), entry.getKey());
            if (currentCell.internalWalls.get(entry.getKey()) == MansionInternalWallState.SOLID) {
                currentCell.internalWalls.put(entry.getKey(), MansionInternalWallState.ROOM_ENTRANCE);
                MansionStandardRoomPiece otherPiece = currentCell.adjacentPieces.get(entry.getKey());
                if (otherPiece.internalWalls.containsKey(entry.getKey().getOppositeFace())) {
                    otherPiece.internalWalls.put(entry.getKey().getOppositeFace(),
                            MansionInternalWallState.ROOM_ENTRANCE
                    );
                }

            }

            cellStack.push(currentCell);
            currentCell = entry.getValue();
            nv++;
        }
    }

    /**
     * Based on chance, randomly open a few walls
     */
    public static void knockdownRandomWalls(@NotNull Collection<JigsawStructurePiece> pieces, @NotNull Random rand) {
        for (JigsawStructurePiece piece : pieces) {
            MansionStandardRoomPiece spiece = (MansionStandardRoomPiece) piece;
            for (BlockFace face : spiece.getShuffledInternalWalls()) {
                if (spiece.internalWalls.get(face) == MansionInternalWallState.WINDOW
                    || spiece.internalWalls.get(face) == MansionInternalWallState.EXIT)
                {
                    continue;
                }

                if (GenUtils.chance(rand, 1, 10)) {
                    spiece.adjacentPieces.get(face).internalWalls.put(
                            face.getOppositeFace(),
                            MansionInternalWallState.ROOM_ENTRANCE
                    );
                    spiece.internalWalls.put(face, MansionInternalWallState.ROOM_ENTRANCE);
                }
            }
        }
    }

    /**
     * Returns a map of adjacent mansion pieces aren't connected to anything.
     */
    private static @NotNull Map<BlockFace, MansionStandardRoomPiece> getValidNeighbours(Collection<JigsawStructurePiece> pieces,
                                                                                        @NotNull MansionStandardRoomPiece piece)
    {
        Map<BlockFace, MansionStandardRoomPiece> neighbours = new EnumMap<>(BlockFace.class);

        // Loop NSEW
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (!piece.adjacentPieces.containsKey(face)) {
                continue;
            }
            MansionStandardRoomPiece neighbour = piece.adjacentPieces.get(face);
            if (neighbour != null && neighbour.areInternalWallsFullyBlocked()) {
                neighbours.put(face, neighbour);
            }
        }

        return neighbours;
    }

}
