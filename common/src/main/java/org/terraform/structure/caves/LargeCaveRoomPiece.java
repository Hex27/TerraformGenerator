package org.terraform.structure.caves;

import com.sun.source.tree.ArrayAccessTree;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.structure.room.CubeRoom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class LargeCaveRoomPiece extends CubeRoom {

    protected int waterLevel = -64;

    //Boolean is true if the location is a boundary.
    protected final HashMap<SimpleLocation, Boolean> toCarve = new HashMap<>();
    protected final HashMap<SimpleLocation, SimpleLocation[]> ceilFloorPairs = new HashMap<>();

    public LargeCaveRoomPiece(int widthX, int widthZ, int height, int x, int y, int z) {
        super(widthX, widthZ, height, x, y, z);
    }
}
