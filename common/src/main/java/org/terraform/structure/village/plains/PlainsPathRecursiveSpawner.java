package org.terraform.structure.village.plains;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.SlabBuilder;

import java.util.*;

public class PlainsPathRecursiveSpawner {

    private static final int minPathLength = 25;
    private static final int maxPathLength = 35;
    private final int range;
    private final ArrayList<RoomPopulatorAbstract> validRooms = new ArrayList<>();
    private final @NotNull SimpleBlock core;
    private final HashMap<SimpleLocation, DirectionalCubeRoom> rooms = new HashMap<>();
    private final HashMap<SimpleLocation, BlockFace> path = new HashMap<>();
    private final HashMap<SimpleLocation, CrossRoad> crossRoads = new HashMap<>();
    private int minRoomWidth = 15;
    private int maxRoomWidth = 20;
    /**
     * 1 for max room density, 0 for no rooms.
     */
    private double villageDensity = 1;
    private PathPopulatorAbstract pathPop;

    public PlainsPathRecursiveSpawner(@NotNull SimpleBlock core, int range, BlockFace... faces) {
        SimpleLocation start = new SimpleLocation(core.getX(), 0, core.getZ());
        crossRoads.put(start, new CrossRoad(start, faces));
        this.range = range;
        this.core = core;
    }

    private void advanceCrossRoad(@NotNull Random random, @NotNull CrossRoad target, @NotNull BlockFace direction) {
        target.satisfiedFaces.add(direction);

        boolean cull = false;
        SimpleLocation loc = new SimpleLocation(target.loc).getRelative(direction);
        int lastCrossroad = 0;
        int edgeTurns = 0;
        while (!cull) {

            // Advance the path forward in this arbitrary direction.
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
                            int smallRoomChance = 10;
                            if (GenUtils.chance(random, smallRoomChance, 100)) {
                                minRoomWidth = 7;
                                maxRoomWidth = 10;
                            }

                            int roomWidthX = GenUtils.randInt(minRoomWidth, maxRoomWidth);
                            int roomWidthZ = GenUtils.randInt(minRoomWidth, maxRoomWidth);

                            DirectionalCubeRoom room = new DirectionalCubeRoom(rF,
                                    roomWidthX,
                                    roomWidthZ,
                                    20,
                                    loc.getX() + (adjDir.getModX() * (2 + roomWidthX / 2)),
                                    loc.getY(),
                                    loc.getZ() + (adjDir.getModZ() * (2 + roomWidthZ / 2))
                            );
                            // TerraformGeneratorPlugin.logger.info("ROOM: [" + (loc.getX() + adjDir.getModX()*11) + "] : [" + room.getX() + "], [" + (loc.getZ() + adjDir.getModZ()*11) + "] : [" + room.getZ() + "]");

                            if (!this.registerRoom(room)) { // Roll crossroads
                                if (GenUtils.chance(random, lastCrossroad, 20)) {
                                    crossRoads.put(loc, new CrossRoad(loc, BlockUtils.getAdjacentFaces(direction)));
                                    lastCrossroad = 0;
                                }
                            }
                        }
                    }
                    else {
                        if (GenUtils.chance(random, lastCrossroad, 20)) {
                            crossRoads.put(loc, new CrossRoad(loc, BlockUtils.getAdjacentFaces(direction)));
                            lastCrossroad = 0;
                        }
                    }
                    loc = loc.getRelative(direction);
                }
                else if (loc.distanceSqr(core.getX(), core.getY(), core.getZ()) > Math.pow(range, 2)) {
                    loc = loc.getRelative(direction.getOppositeFace());
                    direction = BlockUtils.getTurnBlockFace(random, direction);
                    loc = loc.getRelative(direction);
                    edgeTurns++;
                    if (edgeTurns > 3) {
                        cull = true;
                        // TerraformGeneratorPlugin.logger.info("Death by edgeTurns .+ 3");
                    }
                }
                else {
                    // TerraformGeneratorPlugin.logger.info("Death by ");
                    cull = true;
                }
            }
            edgeTurns = 0;
            direction = BlockUtils.getTurnBlockFace(random, direction);
            loc = loc.getRelative(direction);
        }
    }

    public void registerRoomPopulator(RoomPopulatorAbstract roomPop) {
        validRooms.add(roomPop);
    }


    /**
     * @return whether or not a location can hold a cuberoom (no overlaps, not too far etc)
     */
    private boolean isLocationValid(@NotNull SimpleLocation loc) {
        for (DirectionalCubeRoom room : rooms.values()) {
            if (room.isPointInside(loc)) {
                // TerraformGeneratorPlugin.logger.info("Point was inside room. Dying.");
                return false;
            }
        }
        if (loc.distanceSqr(core.getX(), core.getY(), core.getZ()) > Math.pow(range, 2)) {
            // TerraformGeneratorPlugin.logger.info("Point was beyond range. Dying.");
            return false;
        }
        // TerraformGeneratorPlugin.logger.info("Point already exists in the path registry");
        return !path.containsKey(loc);
    }

    public boolean registerRoom(@NotNull DirectionalCubeRoom room) {
        // Cannot be below sea level
        if (core.getPopData()
                .getType(room.getX(),
                        GenUtils.getHighestGround(core.getPopData(), room.getX(), room.getZ()) + 1,
                        room.getZ()) == Material.WATER)
        {
            return false;
        }

        // Cannot overlap another room
        for (DirectionalCubeRoom other : rooms.values()) {
            if (other.isOverlapping(room)) {
                return false;
            }
        }

        // Don't be inside a pathway
        for (SimpleLocation loc : path.keySet()) {
            if (room.isPointInside(loc)) {
                return false;
            }
        }
        rooms.put(new SimpleLocation(room.getX(), 0, room.getZ()), room);
        return true;
    }

    public void forceRegisterRoom(@NotNull DirectionalCubeRoom room) {
        rooms.put(new SimpleLocation(room.getX(), 0, room.getZ()), room);
    }

    public void generate(@NotNull Random random) {
        while (getFirstUnsatisfiedCrossRoad() != null) {
            CrossRoad target = getFirstUnsatisfiedCrossRoad();
            BlockFace direction = target.getFirstUnsatisfiedDirection();
            advanceCrossRoad(random, target, direction);
        }
    }

    @SuppressWarnings("unchecked")
    public void build(@NotNull Random random) {

        // place pathways
        for (SimpleLocation loc : path.keySet()) {
            Wall w = new Wall(new SimpleBlock(core.getPopData(), loc.getX(), loc.getY(), loc.getZ()), path.get(loc));
            w = w.getGround();
            if (BlockUtils.isWet(w.getUp().get())) {

                // Paths underwater are wood planks.            	
                if (BlockUtils.isWet(w.getAtY(TerraformGenerator.seaLevel))) {
                    w = w.getAtY(TerraformGenerator.seaLevel);
                }
                else {
                    w = w.getGroundOrDry().getDown();
                }

                new SlabBuilder(Material.OAK_SLAB).setWaterlogged(true)
                                                  .setType(Type.TOP)
                                                  .apply(w)
                                                  .lapply(w.getRelative(0, 0, 1))
                                                  .lapply(w.getRelative(0, 0, -1))
                                                  .lapply(w.getRelative(1, 0, 1))
                                                  .lapply(w.getRelative(1, 0, -1))
                                                  .lapply(w.getRelative(-1, 0, 1))
                                                  .lapply(w.getRelative(-1, 0, -1))
                                                  .lapply(w.getRelative(1, 0, 0))
                                                  .lapply(w.getRelative(-1, 0, 0));
                // Bukkit.getLogger().info("Underwater path at " + w.get().getVector() + " of type " + w.getType().toString());

                continue;
            }

            // Remove foilage before placement
            if (!w.getUp(2).isSolid() && w.getUp(2).getType() != Material.AIR) {
                w.getUp(2).setType(Material.AIR);
            }

            if (!w.getUp().isSolid() && w.getUp().getType() != Material.AIR) {
                w.getUp().setType(Material.AIR);
            }


            w.setType(Material.DIRT_PATH);

            for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
                Wall target = w.getRelative(face).getGround();
                // Remove foilage before placement
                if (random.nextInt(3) != 0) {
                    if (!target.getUp(2).isSolid() && target.getUp(2).getType() != Material.AIR) {
                        target.getUp(2).setType(Material.AIR);
                    }

                    if (!target.getUp().isSolid() && target.getUp().getType() != Material.AIR) {
                        target.getUp().setType(Material.AIR);
                    }

                    target.setType(Material.DIRT_PATH);
                }
            }
        }
        if (validRooms.isEmpty()) {
            return;
        }

        // Allocate room populators
        Iterator<RoomPopulatorAbstract> it = validRooms.iterator();
        while (it.hasNext()) {
            RoomPopulatorAbstract pops = it.next();
            if (pops.isForceSpawn()) {
                for (CubeRoom room : rooms.values()) {
                    if (room.getPop() == null && pops.canPopulate(room)) {
                        room.setRoomPopulator(pops);
                        if (pops.isUnique()) {
                            it.remove();
                        }
                        break;
                    }
                }
            }
        }

        if (validRooms.isEmpty()) {
            return;
        }

        // Apply room populators
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
                TerraformGeneratorPlugin.logger.info("Registered: "
                                                     + room.getPop().getClass().getName()
                                                     + " at "
                                                     + room.getX()
                                                     + " "
                                                     + room.getY()
                                                     + " "
                                                     + room.getZ()
                                                     + " in a room of size "
                                                     + room.getWidthX()
                                                     + "x"
                                                     + room.getWidthZ());
                room.populate(core.getPopData());
            }
        }

        // Populate pathways
        for (SimpleLocation loc : path.keySet()) {
            if (pathPop != null) {
                pathPop.populate(new PathPopulatorData(new SimpleBlock(
                        core.getPopData(),
                        loc.getX(),
                        loc.getY(),
                        loc.getZ()
                ), path.get(loc), 3, false));
            }
        }

    }

    private @Nullable CrossRoad getFirstUnsatisfiedCrossRoad() {
        for (CrossRoad road : crossRoads.values()) {
            if (!road.isSatisfied()) {
                return road;
            }
        }
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

    public @NotNull HashMap<SimpleLocation, DirectionalCubeRoom> getRooms() {
        return rooms;
    }

    public double getVillageDensity() {
        return villageDensity;
    }

    public void setVillageDensity(double villageDensity) {
        this.villageDensity = villageDensity;
    }

    private static class CrossRoad {
        public final SimpleLocation loc;
        public final BlockFace[] faces;
        public final @NotNull ArrayList<BlockFace> satisfiedFaces = new ArrayList<>();

        public CrossRoad(SimpleLocation loc, BlockFace[] faces) {
            this.loc = loc;
            this.faces = faces;
        }

        public boolean isSatisfied() {
            return getFirstUnsatisfiedDirection() == null;
        }

        public @Nullable BlockFace getFirstUnsatisfiedDirection() {
            for (BlockFace face : faces) {
                if (!satisfiedFaces.contains(face)) {
                    return face;
                }
            }
            return null;
        }
    }

}
