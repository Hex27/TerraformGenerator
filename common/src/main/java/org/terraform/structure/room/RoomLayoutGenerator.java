package org.terraform.structure.room;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;
import org.terraform.utils.MazeSpawner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RoomLayoutGenerator {
    private final ArrayList<CubeRoom> rooms = new ArrayList<>();
    private final int[] upperBound;
    private final int[] lowerBound;
    private final ArrayList<RoomPopulatorAbstract> roomPops = new ArrayList<>();
    private final RoomLayout layout;
    boolean genPaths = true;
    boolean allowOverlaps = false;
    private int numRooms;
    private int centX;
    private int centY;
    private int centZ;
    private Random rand;
    private int range;
    private PathPopulatorAbstract pathPop;
    private boolean carveRooms = false;
    private boolean pyramidish = false;
    private MazeSpawner mazePathGenerator;
    private int tile = -1;
    private int roomMaxHeight = 7;
    private int roomMinHeight = 5;
    private int roomMaxX = 15;
    private int roomMinX = 10;
    private int roomMaxZ = 15;
    private int roomMinZ = 10;

    public RoomLayoutGenerator(Random random, RoomLayout layout, int numRooms, int centX, int centY, int centZ, int range) {
        this.numRooms = numRooms;
        this.layout = layout;
        this.centX = centX;
        this.centY = centY;
        this.centZ = centZ;
        this.rand = random;
        this.range = range;
        this.upperBound = new int[]{centX + range / 2, centZ + range / 2};
        this.lowerBound = new int[]{centX - range / 2, centZ - range / 2};
    }

    public int[] getCenter() {
        return new int[]{centX, centY, centZ};
    }

    public void setPathPopulator(PathPopulatorAbstract pop) {
        this.pathPop = pop;
    }

    public void setCarveRooms(boolean carve) {
        this.carveRooms = carve;
    }

    public void registerRoomPopulator(RoomPopulatorAbstract pop) {
        this.roomPops.add(pop);
    }

    public void reset() {
        rooms.clear();
    }

    public void generate() {
        generate(true);
    }

    public void setPyramid(boolean pyramidish) {
        this.pyramidish = pyramidish;
    }

    public CubeRoom forceAddRoom(int widthX, int widthZ, int heightY) {
        if (layout == RoomLayout.RANDOM_BRUTEFORCE) {
            CubeRoom room = new CubeRoom(widthX, widthZ, heightY,
                    centX + GenUtils.randInt(rand, -range / 2, range / 2),
                    centY,
                    centZ + GenUtils.randInt(rand, -range / 2, range / 2));

            boolean canAdd = false;
            while (!canAdd) {
                canAdd = true;
                if (!allowOverlaps)
                    for (CubeRoom other : rooms) {
                        if (other.isOverlapping(room)) {
                            canAdd = false;
                            room = new CubeRoom(widthX, widthZ, heightY,
                                    centX + GenUtils.randInt(rand, -range / 2, range / 2),
                                    centY,
                                    centZ + GenUtils.randInt(rand, -range / 2, range / 2));
                            break;
                        }
                    }
            }

            rooms.add(room);
            return room;
        } else {
            //Todo: make force spawn for corner budding
            return null;
        }
    }

    /**
     * Populate with room data.
     * @param normalise
     */
    public void generate(boolean normalise) {
        for (int i = 0; i < numRooms; i++) {
            int widthX = GenUtils.randInt(rand, roomMinX, roomMaxX);
            int widthZ = GenUtils.randInt(rand, roomMinZ, roomMaxZ);
            int nx = GenUtils.randInt(rand, -range / 2, range / 2);
            int nz = GenUtils.randInt(rand, -range / 2, range / 2);

            //Normalise room sizes to prevent strange shapes (Like narrow & tall etc)
            if (normalise) {
                if (widthX < widthZ / 2) widthX = widthZ + GenUtils.randInt(rand, -2, 2);
                if (widthZ < widthX / 2) widthZ = widthX + GenUtils.randInt(rand, -2, 2);
            }
            int heightY = GenUtils.randInt(rand, roomMinHeight, roomMaxHeight);

            if (pyramidish) {
                double heightRange = 1 - ((Math.pow(nx, 2) + Math.pow(nz, 2)) / Math.pow(range / 2f, 2));
                if (heightRange * roomMaxHeight < roomMinHeight) heightRange = roomMinHeight / (float) roomMaxHeight;
                //TerraformGeneratorPlugin.logger.info("Original h: " + heightY + "; heightRange: " + heightRange);
                heightY = GenUtils.randInt(rand,
                        roomMinHeight,
                        (int) (roomMaxHeight * heightRange));
            }

            if (normalise) {
                if (heightY > widthX) heightY = widthX + GenUtils.randInt(rand, -2, 2);
                if (heightY < widthX / 3) heightY = widthX / 3 + GenUtils.randInt(rand, -2, 2);
            }
            CubeRoom room = new CubeRoom(widthX, widthZ, heightY,
                    nx + centX,
                    centY,
                    nz + centZ);
            if (layout == RoomLayout.RANDOM_BRUTEFORCE) {
                boolean canAdd = true;
                if (!allowOverlaps)
                    for (CubeRoom other : rooms) {
                        if (other.isOverlapping(room)) {
                            canAdd = false;
                            break;
                        }
                    }
                if (canAdd)
                    rooms.add(room);
            } else if (layout == RoomLayout.OVERLAP_CONNECTED) {
                room.setX(centX + GenUtils.randInt(rand, -4, 4));
                room.setZ(centZ + GenUtils.randInt(rand, -4, 4));

                boolean canAdd = true;

                for (CubeRoom other : rooms) {
                    if (other.envelopesOrIsInside(room)) {
                        canAdd = false;
                        break;
                    }
                }
                for (CubeRoom other : rooms) {
                    if (!other.isOverlapping(room)) {
                        canAdd = false;
                        break;
                    }
                }
                if (canAdd)
                    rooms.add(room);
            }
        }
    }

    public boolean anyOverlaps() {
        if (allowOverlaps) return false;
        for (CubeRoom room : rooms) {
            for (CubeRoom other : rooms) {
                if (other.isClone(room)) continue;
                if (room.isOverlapping(other))
                    return true;
            }
        }
        return false;
    }

    /**
     * Please supply 2d (x,z) coordinates
     * @param coords
     * @return
     */
    public boolean isInRoom(int[] coords) {
        for (CubeRoom room : rooms) {
            if (room.isPointInside(coords))
                return true;
        }
        return false;
    }

    public void setGenPaths(boolean genPaths) {
        this.genPaths = genPaths;
    }

    public void setAllowOverlaps(boolean allowOverlaps) {
        this.allowOverlaps = allowOverlaps;
    }

    /**
     * Links room populators to empty rooms and applies paths and room to the actual world.
     * @param data
     * @param tw
     * @param mat
     */
    @SuppressWarnings("unchecked")
    public void fill(PopulatorDataAbstract data, TerraformWorld tw, Material... mat) {
        ArrayList<PathGenerator> pathGens = new ArrayList<>();
        //Carve Pathways
        if (genPaths) {
            if (mazePathGenerator != null) {
                //MazeSpawner spawner = new MazeSpawner(new Random(), new SimpleBlock(data,this.getCentX(),this.getCentY()+1,this.getCentZ()), range, range);
                mazePathGenerator.setRand(rand);
                mazePathGenerator.setCore(new SimpleBlock(data, this.getCentX(), this.getCentY() + 1, this.getCentZ()));
                if (mazePathGenerator.getWidthX() == -1) {
                    mazePathGenerator.setWidthX(range);
                }
                if (mazePathGenerator.getWidthZ() == -1) {
                    mazePathGenerator.setWidthZ(range);
                }
                mazePathGenerator.prepareMaze();
                mazePathGenerator.carveMaze(false, mat);
            } else
                for (CubeRoom room : rooms) {
                    SimpleBlock base = new SimpleBlock(data, room.getX(), room.getY(), room.getZ());
                    PathGenerator gen = new PathGenerator(base, mat, rand, upperBound, lowerBound);
                    if (pathPop != null) gen.setPopulator(pathPop);
                    while (!gen.isDead()) {
                        gen.next();
                    }
                    pathGens.add(gen);
                }
        }

        //Create empty rooms
        for (CubeRoom room : rooms) {
            if (carveRooms) room = new CarvedRoom(room);

            if (allowOverlaps)
                room.fillRoom(data, tile, mat, Material.CAVE_AIR);
            else
                room.fillRoom(data, tile, mat, Material.AIR);
        }

        //Populate pathways
        if (mazePathGenerator != null && this.pathPop != null) {
            for (PathPopulatorData pPData : mazePathGenerator.pathPopDatas) {
                if (!this.isInRoom(new int[]{pPData.base.getX(), pPData.base.getZ()}))
                    this.pathPop.populate(pPData);
            }
        } else {
            for (PathGenerator pGen : pathGens) {
                pGen.populate();
            }
        }

        if (roomPops.isEmpty()) return;

        //Allocate room populators
        Iterator<RoomPopulatorAbstract> it = roomPops.iterator();
        while (it.hasNext()) {
            RoomPopulatorAbstract pops = it.next();
            if (pops.isForceSpawn()) {
                for (CubeRoom room : rooms) {
                    if (room.pop == null && pops.canPopulate(room)) {
                        //Bukkit.getLogger().info("Set down forced populator of " + pops.getClass().getName());
                        room.setRoomPopulator(pops);
                        if (pops.isUnique()) it.remove();
                        break;
                    }
                }
            }
        }

        if (roomPops.isEmpty()) return;

        //Apply room populators
        for (CubeRoom room : rooms) {
            if (room.pop == null) {
                List<RoomPopulatorAbstract> shuffled = (List<RoomPopulatorAbstract>) roomPops.clone();
                Collections.shuffle(shuffled, rand);
                for (RoomPopulatorAbstract roomPop : shuffled) {
                    if (roomPop.canPopulate(room)) {
                        room.setRoomPopulator(roomPop);
                        if (roomPop.isUnique()) {
                            roomPops.remove(roomPop);
                        }

                        break;
                    }
                }
            }
            if (room.pop != null) {
                Bukkit.getLogger().info("Registered: " + room.pop.getClass().getName() + " at " + room.getX() + " " + room.getY() + " " + room.getZ() + " in a room of size " + room.getWidthX() + "x" + room.getWidthZ());
                room.populate(data);
            } else {
                Bukkit.getLogger().info("Registered: plain room at " + room.getX() + " " + room.getY() + " " + room.getZ() + " in a room of size " + room.getWidthX() + "x" + room.getWidthZ());
            }
        }

    }

    public void fillPathsOnly(PopulatorDataAbstract data, TerraformWorld tw, Material... mat) {
        ArrayList<PathGenerator> pathGens = new ArrayList<>();
        if (genPaths)
            for (CubeRoom room : rooms) {
                SimpleBlock base = new SimpleBlock(data, room.getX(), room.getY(), room.getZ());
                PathGenerator gen = new PathGenerator(base, mat, rand, upperBound, lowerBound);
                if (pathPop != null) gen.setPopulator(pathPop);
                while (!gen.isDead()) {
                    gen.next();
                }
                pathGens.add(gen);
            }

        //Populate pathways
        for (PathGenerator pGen : pathGens) {
            pGen.populate();
        }
    }

    /**
     * @return the numRooms
     */
    public int getNumRooms() {
        return numRooms;
    }

    /**
     * @param numRooms the numRooms to set
     */
    public void setNumRooms(int numRooms) {
        this.numRooms = numRooms;
    }

    /**
     * @return the range
     */
    public int getRange() {
        return range;
    }

    /**
     * @param range the range to set
     */
    public void setRange(int range) {
        this.range = range;
    }

    /**
     * @return the roomMaxHeight
     */
    public int getRoomMaxHeight() {
        return roomMaxHeight;
    }

    /**
     * @param roomMaxHeight the roomMaxHeight to set
     */
    public void setRoomMaxHeight(int roomMaxHeight) {
        this.roomMaxHeight = roomMaxHeight;
    }

    /**
     * @return the roomMinHeight
     */
    public int getRoomMinHeight() {
        return roomMinHeight;
    }

    /**
     * @param roomMinHeight the roomMinHeight to set
     */
    public void setRoomMinHeight(int roomMinHeight) {
        this.roomMinHeight = roomMinHeight;
    }

    /**
     * @return the rooms
     */
    public ArrayList<CubeRoom> getRooms() {
        return rooms;
    }

    /**
     * @return the roomMaxX
     */
    public int getRoomMaxX() {
        return roomMaxX;
    }

    /**
     * @param roomMaxX the roomMaxX to set
     */
    public void setRoomMaxX(int roomMaxX) {
        this.roomMaxX = roomMaxX;
    }

    /**
     * @return the roomMinX
     */
    public int getRoomMinX() {
        return roomMinX;
    }

    /**
     * @param roomMinX the roomMinX to set
     */
    public void setRoomMinX(int roomMinX) {
        this.roomMinX = roomMinX;
    }

    /**
     * @return the roomMaxZ
     */
    public int getRoomMaxZ() {
        return roomMaxZ;
    }

    /**
     * @param roomMaxZ the roomMaxZ to set
     */
    public void setRoomMaxZ(int roomMaxZ) {
        this.roomMaxZ = roomMaxZ;
    }

    /**
     * @return the roomMinZ
     */
    public int getRoomMinZ() {
        return roomMinZ;
    }

    /**
     * @param roomMinZ the roomMinZ to set
     */
    public void setRoomMinZ(int roomMinZ) {
        this.roomMinZ = roomMinZ;
    }

    /**
     * @return the centX
     */
    public int getCentX() {
        return centX;
    }

    /**
     * @param centX the centX to set
     */
    public void setCentX(int centX) {
        this.centX = centX;
    }

    /**
     * @return the centY
     */
    public int getCentY() {
        return centY;
    }

    /**
     * @param centY the centY to set
     */
    public void setCentY(int centY) {
        this.centY = centY;
    }

    /**
     * @return the centZ
     */
    public int getCentZ() {
        return centZ;
    }

    /**
     * @param centZ the centZ to set
     */
    public void setCentZ(int centZ) {
        this.centZ = centZ;
    }

    /**
     * @return the rand
     */
    public Random getRand() {
        return rand;
    }

    /**
     * @param rand the rand to set
     */
    public void setRand(Random rand) {
        this.rand = rand;
    }

    /**
     * @return the pathPop
     */
    public PathPopulatorAbstract getPathPop() {
        return pathPop;
    }

    public void setTile(int tile) {
        this.tile = tile;
    }

    public void setMazePathGenerator(MazeSpawner mazePathGenerator) {
        this.mazePathGenerator = mazePathGenerator;
    }


}
