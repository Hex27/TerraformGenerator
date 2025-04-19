package org.terraform.utils.datastructs;

import org.terraform.data.CoordPair;
import org.terraform.data.SimpleLocation;

import java.util.HashMap;

/**
 * A data structure which trades compute time for a heavily compressed
 * HashMap<SimpleLocation, Boolean> storage
 */
public class CompressedCoordBools {
    HashMap<CoordPair, CompressedChunkBools> chunks = new HashMap<>();

    public void set(SimpleLocation loc, boolean val){
        if(val) set(loc);
        else unSet(loc);
    }
    //One int can store 256 states (8*4 bits)
    // Each byte in compressed[] stores one 16x16 slice.
    // The index is x + 16*z
    public void set(SimpleLocation loc){
        getOrCreate(loc).set(loc.getX() & 0b1111, loc.getY(), loc.getZ() & 0b1111);
    }
    public void unSet(SimpleLocation loc){
        if(!chunks.containsKey(new CoordPair(loc.getX()>>4,loc.getZ()>>4)))
            return;
        getOrCreate(loc).unSet(loc.getX() & 0b1111, loc.getY(), loc.getZ() & 0b1111);
    }

    public boolean isSet(SimpleLocation loc){
        if(!chunks.containsKey(new CoordPair(loc.getX()>>4,loc.getZ()>>4)))
            return false;
        return getOrCreate(loc).isSet(loc.getX() & 0b1111, loc.getY(), loc.getZ() & 0b1111);
    }

    private CompressedChunkBools getOrCreate(SimpleLocation loc){
        CoordPair key = new CoordPair(loc.getX()>>4,loc.getZ()>>4);
        CompressedChunkBools compressed = chunks.get(key);
        if(compressed == null) {
            compressed = new CompressedChunkBools();
            chunks.put(key, compressed);
        }
        return compressed;
    }
}
