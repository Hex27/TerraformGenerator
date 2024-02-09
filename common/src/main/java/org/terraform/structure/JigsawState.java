package org.terraform.structure;

import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.SimpleLocation;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.room.RoomPopulatorAbstract;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class will hold the runtime state for each unique structure
 * at each unique position. Upon initialisation, calculate path locations
 * and room locations.
 */
public class JigsawState {

    //This will hold the room coordinates.
    //Lowest index generates first.
    public final ArrayList<RoomLayoutGenerator> roomPopulatorStates = new ArrayList<>();
    boolean calculatedRange = false;
    int minChunkX = Integer.MAX_VALUE;
    int minChunkZ = Integer.MAX_VALUE;
    int maxChunkX = Integer.MIN_VALUE;
    int maxChunkZ = Integer.MIN_VALUE;

    /**
     * Called to check if chunkX,chunkZ will contain a piece
     */
    public boolean isInRange(int chunkX, int chunkZ){
        if(!calculatedRange)
        {
            calculatedRange = true;
            roomPopulatorStates.forEach((gen)->{
                gen.getRooms().forEach((room)->{
                    int[] lowerCorner = room.getLowerCorner();
                    int[] upperCorner = room.getUpperCorner();
                    minChunkX = Math.min(minChunkX, lowerCorner[0]>>4);
                    maxChunkX = Math.max(minChunkX, upperCorner[0]>>4);
                    minChunkZ = Math.min(minChunkX, lowerCorner[1]>>4);
                    maxChunkZ = Math.max(minChunkX, upperCorner[1]>>4);
                });

                minChunkX = Math.min(minChunkX, (gen.getCentX()-gen.getRange())>>4);
                maxChunkX = Math.max(minChunkX, (gen.getCentX()+gen.getRange())>>4);
                minChunkZ = Math.min(minChunkZ, (gen.getCentZ()-gen.getRange())>>4);
                maxChunkZ = Math.max(minChunkZ, (gen.getCentZ()+gen.getRange())>>4);
            });
        }

        return chunkX >= minChunkX
                && chunkX <= maxChunkX
                && chunkZ >= minChunkZ
                && chunkZ <= maxChunkZ;
    }
}
