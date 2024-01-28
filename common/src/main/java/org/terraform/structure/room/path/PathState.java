package org.terraform.structure.room.path;

import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayoutGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Responsible for generating a list of locations
 * which represents a specific path.
 */
public class PathState {

    /*
    /* Generation state
    */

    //This will contain some path nodes
    public HashSet<PathNode> nodes = new HashSet<>();

    /*
    /* General settings
    */
    public PathWriter writer = new CavePathWriter();
    private int pathWidth = 3;
    private int pathHeight = 3;
    private int maxBend = -1;
    //If the path generator is not to run while inside room areas,
    //then set this to true.
    private boolean ignoreWithinRooms = true;
    private final RoomLayoutGenerator generator;
    private final TerraformWorld tw;

    public PathState(RoomLayoutGenerator generator, TerraformWorld tw)
    {
        this.generator = generator;
        this.tw = tw;
        PathNode[] baseNodes = new PathNode[generator.getRooms().size()];
        ArrayList<CubeRoom> rooms = new ArrayList<>(generator.getRooms());

        //One starting node for each room
        for(int i = 0; i < generator.getRooms().size(); i++) {
            CubeRoom room = rooms.get(i);
            baseNodes[i] = new PathNode(new SimpleLocation(room.getX(), room.getY(), room.getZ()), pathWidth);
        }

        //For every node, connect it to the next node
        //With a poor correlation between array order and
        // actual XZ position, this would look random.
        assert(baseNodes.length >= 2);
        for(int i = 0; i < baseNodes.length-1; i++)
            connectNodes(baseNodes[i], baseNodes[i+1], tw, nodes);

        nodes.addAll(Arrays.asList(baseNodes));
    }

    //Connects the two nodes with new nodes added to toAdd
    private void connectNodes(PathNode one, PathNode two, TerraformWorld tw, HashSet<PathNode> toAdd)
    {
        BlockFace oneConn;
        //Set connected state
        if(one.center.getX() - two.center.getX() == 0)
        {
            oneConn = one.center.getZ() > two.center.getZ() ? BlockFace.NORTH : BlockFace.SOUTH;
            one.connected.add(oneConn);
            two.connected.add(oneConn.getOppositeFace());
        }
        else if(one.center.getZ() - two.center.getZ() == 0)
        {
            oneConn = one.center.getX() > two.center.getX() ? BlockFace.WEST : BlockFace.EAST;
            one.connected.add(oneConn);
            two.connected.add(oneConn.getOppositeFace());
        }
        else //Add a new node perpendicularly to both
        {
            //Either add a node varying the X of one or the Z of two
            PathNode newNode = new PathNode(
                    tw.getHashedRand(one.center.getX(), two.center.getZ(), 1890341).nextBoolean() ?
                    one.center.getRelative(BlockFace.NORTH, one.center.getX() - two.center.getX())
                    :two.center.getRelative(BlockFace.WEST, one.center.getZ() - two.center.getZ()), pathWidth);

            connectNodes(newNode, one, tw, toAdd);
            connectNodes(newNode, two, tw, toAdd);
            return; //do NOT run the code below
        }

        //Add path nodes that lead from one to two
        for(int i = pathWidth; i < one.center.distance(two.center); i++)
        {
            toAdd.add(new PathNode(one.center.getRelative(oneConn, i), pathWidth));
        }
    }

    public int getPathWidth() {
        return pathWidth;
    }

    public void setPathWidth(int pathWidth) {
        this.pathWidth = pathWidth;
    }

    public int getPathHeight() {
        return pathHeight;
    }

    public void setPathHeight(int pathHeight) {
        this.pathHeight = pathHeight;
    }

    public int getMaxBend() {
        return maxBend;
    }

    public void setMaxBend(int maxBend) {
        this.maxBend = maxBend;
    }

    public class PathNode{
        public final int pathWidth;
        public final SimpleLocation center;
        public final HashSet<BlockFace> connected = new HashSet<>();
        //Assumes input is new
        public PathNode(SimpleLocation center, int pathWidth) {
            this.pathWidth = pathWidth;
            this.center = center;
            //Lock path nodes to a grid-like structure which will allow
            //path nodes to be spaced properly
            this.center.setX((center.getX() / pathWidth)*pathWidth);
        }

        //Equality is based on the simple location
        public boolean equals(Object o){
            if(o instanceof PathNode pn)
                return pn.center.equals(center);
            return false;
        }

        public int hashCode(){
            return center.hashCode();
        }
    }
}
