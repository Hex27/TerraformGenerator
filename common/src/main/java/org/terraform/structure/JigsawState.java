package org.terraform.structure;

import org.terraform.structure.room.RoomLayoutGenerator;

import java.util.ArrayList;

/**
 * This class will hold the runtime state for each unique structure
 * at each unique position. Upon initialisation, calculate path locations
 * and room locations.
 */
public class JigsawState {

    // This will hold the room coordinates.
    // Lowest index generates first.
    public final ArrayList<RoomLayoutGenerator> roomPopulatorStates = new ArrayList<>();
    boolean calculatedRange = false;
    int minChunkX = Integer.MAX_VALUE;
    int minChunkZ = Integer.MAX_VALUE;
    int maxChunkX = Integer.MIN_VALUE;
    int maxChunkZ = Integer.MIN_VALUE;

    /**
     * Called to check if chunkX,chunkZ will contain a piece
     */
    public boolean isInRange(int chunkX, int chunkZ) {
        if (!calculatedRange) {
            calculatedRange = true;
            roomPopulatorStates.forEach((gen) -> {
                gen.getRooms().forEach((room) -> {
                    int[] lowerCorner = room.getLowerCorner();
                    int[] upperCorner = room.getUpperCorner();
                    minChunkX = Math.min(minChunkX, lowerCorner[0] >> 4);
                    maxChunkX = Math.max(maxChunkX, upperCorner[0] >> 4);
                    minChunkZ = Math.min(minChunkZ, lowerCorner[1] >> 4);
                    maxChunkZ = Math.max(maxChunkZ, upperCorner[1] >> 4);
                });

                minChunkX = Math.min(minChunkX, (gen.getCentX() - gen.getRange()) >> 4);
                maxChunkX = Math.max(maxChunkX, (gen.getCentX() + gen.getRange()) >> 4);
                minChunkZ = Math.min(minChunkZ, (gen.getCentZ() - gen.getRange()) >> 4);
                maxChunkZ = Math.max(maxChunkZ, (gen.getCentZ() + gen.getRange()) >> 4);
            });
        }

        return chunkX >= minChunkX && chunkX <= maxChunkX && chunkZ >= minChunkZ && chunkZ <= maxChunkZ;
    }
}
