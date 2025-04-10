package org.terraform.structure.caves;

import org.terraform.data.CoordPair;
import org.terraform.data.SimpleLocation;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.datastructs.CompressedCoordBools;

import java.util.HashMap;

public class LargeCaveRoomPiece extends CubeRoom {

    protected SimpleLocation startingLoc = null;
    // Boolean is true if the location is a boundary.
    protected final CompressedCoordBools toCarve = new CompressedCoordBools();
    protected final CompressedCoordBools boundaries = new CompressedCoordBools();
    protected final HashMap<CoordPair,CoordPair> ceilFloorPairs = new HashMap<>();
    protected int waterLevel = -64;

    public LargeCaveRoomPiece(int widthX, int widthZ, int height, int x, int y, int z) {
        super(widthX, widthZ, height, x, y, z);
    }
}
