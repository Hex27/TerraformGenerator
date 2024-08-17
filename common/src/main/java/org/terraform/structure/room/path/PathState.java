package org.terraform.structure.room.path;

import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.RoomLayoutGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public final @NotNull HashSet<PathNode> nodes = new HashSet<>();

    /*
    /* General settings
    */
    public @NotNull PathWriter writer = new CavePathWriter(0,0,0,0,0,0);
    private int pathWidth = 3;
    private int pathHeight = 3;
    private int maxBend = -1;
    //If the path generator is not to run while inside room areas,
    //then set this to true.
    private boolean ignoreWithinRooms = true;
    private final @NotNull RoomLayoutGenerator generator;
    private final @NotNull TerraformWorld tw;

    public PathState(@NotNull RoomLayoutGenerator generator, @NotNull TerraformWorld tw)
    {
        this.generator = generator;
        this.tw = tw;
        if(!generator.genPaths()) return;

        PathNode[] baseNodes = new PathNode[generator.getRooms().size()];
        ArrayList<CubeRoom> rooms = new ArrayList<>(generator.getRooms());

        //One starting node for each room
        for(int i = 0; i < generator.getRooms().size(); i++) {
            CubeRoom room = rooms.get(i);
            baseNodes[i] = new PathNode(new SimpleLocation(room.getX(), room.getY(), room.getZ()), pathWidth, generator.getPathPop());
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
    private void connectNodes(@NotNull PathNode one, @NotNull PathNode two, @NotNull TerraformWorld tw, @NotNull HashSet<PathNode> toAdd)
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
                            new SimpleLocation(one.center.getX(),one.center.getY(),two.center.getZ())
                            : new SimpleLocation(two.center.getX(),one.center.getY(), one.center.getZ()), pathWidth, generator.getPathPop());
            toAdd.add(newNode);
            connectNodes(newNode, one, tw, toAdd);
            connectNodes(newNode, two, tw, toAdd);
            return; //do NOT run the code below
        }

        //Add path nodes that lead from one to two
        for(int i = pathWidth; i < one.center.distance(two.center); i++)
        {
            toAdd.add(new PathNode(one.center.getRelative(oneConn, i), pathWidth, generator.getPathPop(), oneConn));
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

    public static class PathNode{
        public final int pathWidth;
        public final @NotNull SimpleLocation center;
        public final PathPopulatorAbstract populator;
        public final HashSet<BlockFace> connected = new HashSet<>();
        //Assumes input is new
        public PathNode(@NotNull SimpleLocation center, int pathWidth, PathPopulatorAbstract populator, BlockFace... connections) {
            this.pathWidth = pathWidth;
            this.center = center;
            //Lock path nodes to a grid-like structure which will allow
            //path nodes to be spaced properly
            this.center.setX((center.getX() / pathWidth)*pathWidth);
            this.center.setZ((center.getZ() / pathWidth)*pathWidth);
            Collections.addAll(connected, connections);
            this.populator = populator;
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
