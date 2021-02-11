package org.terraform.structure.village.plains;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.SlabBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PlainsPathRecursiveSpawner {

    private static final int minPathLength = 15;
    private static final int maxPathLength = 25;
    private final int range;
    private int minRoomWidth = 15;
    private int maxRoomWidth = 20;
    private int smallRoomChance = 10;
    private int minSmallRoomWidth = 7;
    private int maxSmallRoomWidth = 10;
    
    /** 
     * 1 for max room density, 0 for no rooms.
     */
    private double villageDensity = 1;
    private PathPopulatorAbstract pathPop;
    private final ArrayList<RoomPopulatorAbstract> validRooms = new ArrayList<RoomPopulatorAbstract>();

    private final SimpleBlock core;
    private final HashMap<SimpleLocation, DirectionalCubeRoom> rooms = new HashMap<>();
    private final HashMap<SimpleLocation, BlockFace> path = new HashMap<>();
    private final HashMap<SimpleLocation, CrossRoad> crossRoads = new HashMap<>();

    public PlainsPathRecursiveSpawner(SimpleBlock core, int range, BlockFace... faces) {
        SimpleLocation start = new SimpleLocation(core.getX(), 0, core.getZ());
        crossRoads.put(start, new CrossRoad(start, faces));
        this.range = range;
        this.core = core;
    }

    private void advanceCrossRoad(Random random, CrossRoad target, BlockFace direction) {
        target.satisfiedFaces.add(direction);

        boolean cull = false;
        SimpleLocation loc = new SimpleLocation(target.loc).getRelative(direction);
        int lastCrossroad = 0;
        while (!cull) {

            //Advance the path forward in this arbitrary direction.
            for (int i = 0; i < GenUtils.randInt(random, minPathLength, maxPathLength); i++) {

                if (isLocationValid(loc)) {
                    path.put(loc, direction);

                    if (GenUtils.chance(random, (int) (villageDensity * 10000), 10000)) {
                        BlockFace adjDir = BlockUtils.getAdjacentFaces(direction)[random.nextInt(2)];
                        SimpleLocation adj = loc.getRelative(adjDir);
                        if (isLocationValid(adj)) {
                            BlockFace rF = adjDir.getOppositeFace();
                            int minRoomWidth = this.minRoomWidth;
                            int maxRoomWidth = this.maxRoomWidth;
                            if(GenUtils.chance(random, smallRoomChance, 100)) {
                            	minRoomWidth = this.minSmallRoomWidth;
                            	maxRoomWidth = this.maxSmallRoomWidth;
                            }
                            
                            int roomWidthX = GenUtils.randInt(minRoomWidth, maxRoomWidth);
                            int roomWidthZ = GenUtils.randInt(minRoomWidth, maxRoomWidth);
                            
                            DirectionalCubeRoom room = new DirectionalCubeRoom(
                                    rF, roomWidthX, roomWidthZ, 20, 
                                    loc.getX() + (adjDir.getModX() * (2 + roomWidthX/2)),
                                    loc.getY(),
                                    loc.getZ() + (adjDir.getModZ() * (2 + roomWidthZ/2)));
                            //TerraformGeneratorPlugin.logger.info("ROOM: [" + (loc.getX() + adjDir.getModX()*11) + "] : [" + room.getX() + "], [" + (loc.getZ() + adjDir.getModZ()*11) + "] : [" + room.getZ() + "]");
                            
                            if (!this.registerRoom(room)) { //Roll crossroads
                                if (GenUtils.chance(random, lastCrossroad, 20)) {
                                    crossRoads.put(loc, new CrossRoad(loc, BlockUtils.getAdjacentFaces(direction)));
                                    lastCrossroad = 0;
                                }
                            }
                        }
                    } else {
                        if (GenUtils.chance(random, lastCrossroad, 20)) {
                            crossRoads.put(loc, new CrossRoad(loc, BlockUtils.getAdjacentFaces(direction)));
                            lastCrossroad = 0;
                        }
                    }
                    loc = loc.getRelative(direction);
                } else
                    cull = true;
            }
            BlockFace[] valid = new BlockFace[3];
            valid[0] = BlockUtils.getLeft(direction);
            valid[1] = BlockUtils.getRight(direction);
            valid[2] = direction;
            direction = valid[random.nextInt(3)];
            loc = loc.getRelative(direction);
        }
    }

    public void registerRoomPopulator(RoomPopulatorAbstract roomPop) {
        validRooms.add(roomPop);
    }

    
    /**
     * 
     * @param loc
     * @return whether or not a location can hold a cuberoom (no overlaps, not too far etc)
     */
    private boolean isLocationValid(SimpleLocation loc) {
        for (DirectionalCubeRoom room : rooms.values()) {
            if (room.isPointInside(loc)) {
                //TerraformGeneratorPlugin.logger.info("Point was inside room. Dying.");
                return false;
            }
        }
        if (loc.distanceSqr(core.getX(), core.getY(), core.getZ()) > Math.pow(range, 2)) {
            //TerraformGeneratorPlugin.logger.info("Point was beyond range. Dying.");
            return false;
        }
        //TerraformGeneratorPlugin.logger.info("Point already exists in the path registry");
        return !path.containsKey(loc);
    }

    public boolean registerRoom(DirectionalCubeRoom room) {
        //Cannot be below sea level
        if (core.getPopData().getType(room.getX(),GenUtils.getHighestGround(core.getPopData(), room.getX(), room.getZ())+1,room.getZ()) == Material.WATER) {
            return false;
        }

        //Cannot overlap another room
        for (DirectionalCubeRoom other : rooms.values()) {
            if (other.isOverlapping(room)) return false;
        }

        //Don't be inside a pathway
        for (SimpleLocation loc : path.keySet()) {
            if (room.isPointInside(loc)) return false;
        }
        rooms.put(new SimpleLocation(room.getX(), 0, room.getZ()), room);
        return true;
    }

    public void forceRegisterRoom(DirectionalCubeRoom room) {
        rooms.put(new SimpleLocation(room.getX(), 0, room.getZ()), room);
    }

    public void generate(Random random) {
        while (getFirstUnsatisfiedCrossRoad() != null) {
            CrossRoad target = getFirstUnsatisfiedCrossRoad();
            BlockFace direction = target.getFirstUnsatisfiedDirection();
            advanceCrossRoad(random, target, direction);
        }
    }

    @SuppressWarnings("unchecked")
    public void build(Random random) {

        //place pathways
        for (SimpleLocation loc : path.keySet()) {
            Wall w = new Wall(new SimpleBlock(core.getPopData(), loc.getX(), loc.getY(), loc.getZ()), path.get(loc));
            w = w.getGround();
            if (BlockUtils.isWet(w.getRelative(0,1,0).get())) {
            	
                //Paths underwater are wood planks.

            	//TODO: This is the most optimised way now, 
            	//but if local water levels are implemented, this must change.
            	w = w.getAtY(TerraformGenerator.seaLevel);
				new SlabBuilder(Material.OAK_SLAB)
				 .setWaterlogged(true).setType(Type.TOP)
				 .apply(w)
				 .lapply(w.getRelative(0,0,1))
				 .lapply(w.getRelative(0,0,-1))
				 .lapply(w.getRelative(1,0,1))
				 .lapply(w.getRelative(1,0,-1))
				 .lapply(w.getRelative(-1,0,1))
				 .lapply(w.getRelative(-1,0,-1))
				 .lapply(w.getRelative(1,0,0))
				 .lapply(w.getRelative(-1,0,0))
				 ;
				//Bukkit.getLogger().info("Underwater path at " + w.get().getVector() + " of type " + w.getType().toString());
            	
                continue;
            }
            //if (BlockUtils.isDirtLike(w.getType()))
            w.setType(Material.GRASS_PATH);

            for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
                Wall target = w.getRelative(face).getGround();
                if (random.nextInt(3) != 0)
                    target.setType(Material.GRASS_PATH);
            }
        }
        if (validRooms.isEmpty()) return;

        //Allocate room populators
        Iterator<RoomPopulatorAbstract> it = validRooms.iterator();
        while (it.hasNext()) {
            RoomPopulatorAbstract pops = it.next();
            if (pops.isForceSpawn()) {
                for (CubeRoom room : rooms.values()) {
                    if (room.getPop() == null && pops.canPopulate(room)) {
                        room.setRoomPopulator(pops);
                        if (pops.isUnique()) it.remove();
                        break;
                    }
                }
            }
        }

        if (validRooms.isEmpty()) return;

        //Apply room populators
        for (CubeRoom room : rooms.values()) {
            if (room.getPop() == null) {
                List<RoomPopulatorAbstract> shuffled = (List<RoomPopulatorAbstract>) validRooms.clone();
                Collections.shuffle(shuffled, random);
                for (RoomPopulatorAbstract roomPop : shuffled) {
                    if (roomPop.canPopulate(room)) {
                        room.setRoomPopulator(roomPop);
                        if (roomPop.isUnique()) {
                            validRooms.remove(roomPop);
                        }

                        break;
                    }
                }
            }
            if (room.getPop() != null) {
                Bukkit.getLogger().info("Registered: " + room.getPop().getClass().getName() + " at " + room.getX() + " " + room.getY() + " " + room.getZ() + " in a room of size "
                        + room.getWidthX() + "x" + room.getWidthZ());
                room.populate(core.getPopData());
            } else {
                //Bukkit.getLogger().info("Registered: plain room at " + room.getX() + " " + room.getY() + " " + room.getZ() + " in a room of size " + room.getWidthX() + "x" + room.getWidthZ());
            }
        }

        //Populate pathways
        for (SimpleLocation loc : path.keySet()) {
            if (pathPop != null) {
                pathPop.populate(new PathPopulatorData(new SimpleBlock(core.getPopData(), loc.getX(), loc.getY(), loc.getZ()), path.get(loc), 3));
            }
        }

    }

    private CrossRoad getFirstUnsatisfiedCrossRoad() {
        for (CrossRoad road : crossRoads.values())
            if (!road.isSatisfied()) return road;
        return null;
    }

    public int getMinRoomWidth() {
        return minRoomWidth;
    }

    public void setMinRoomWidth(int minRoomWidth) {
        this.minRoomWidth = minRoomWidth;
    }

    public int getMaxRoomWidth() {
        return maxRoomWidth;
    }

    public void setMaxRoomWidth(int maxRoomWidth) {
        this.maxRoomWidth = maxRoomWidth;
    }

    public PathPopulatorAbstract getPathPop() {
        return pathPop;
    }

    public void setPathPop(PathPopulatorAbstract pathPop) {
        this.pathPop = pathPop;
    }

    public HashMap<SimpleLocation, DirectionalCubeRoom> getRooms() {
        return rooms;
    }

    public double getVillageDensity() {
        return villageDensity;
    }

    public void setVillageDensity(double villageDensity) {
        this.villageDensity = villageDensity;
    }

    private class CrossRoad {
        public SimpleLocation loc;
        public BlockFace[] faces;
        public ArrayList<BlockFace> satisfiedFaces = new ArrayList<>();

        public CrossRoad(SimpleLocation loc, BlockFace[] faces) {
            this.loc = loc;
            this.faces = faces;
        }

        public boolean isSatisfied() {
            return getFirstUnsatisfiedDirection() == null;
        }

        public BlockFace getFirstUnsatisfiedDirection() {
            for (BlockFace face : faces) {
                if (!satisfiedFaces.contains(face))
                    return face;
            }
            return null;
        }
    }

}
