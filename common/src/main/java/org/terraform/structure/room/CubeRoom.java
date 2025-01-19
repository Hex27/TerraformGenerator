package org.terraform.structure.room;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.LimitedRegion;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.carver.StandardRoomCarver;
import org.terraform.utils.GenUtils;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Random;

public class CubeRoom {
    int widthX, widthZ, height;
    int x, y, z;

    RoomPopulatorAbstract pop;
    boolean isActivated = false;

    public CubeRoom(int widthX, int widthZ, int height, int x, int y, int z) {
        this.widthX = widthX;
        this.widthZ = widthZ;
        this.height = height;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @param face    Direction of the wall relative to the center
     * @param padding padding of 0 refers to the room's wall.
     *                1 refers to one layer inside the room and so on.
     * @return The walls of one side of the room
     */
    public @NotNull SimpleEntry<Wall, Integer> getWall(@NotNull PopulatorDataAbstract data,
                                                       @NotNull BlockFace face,
                                                       int padding)
    {
        int[] lowerBounds = getLowerCorner();
        int[] upperBounds = getUpperCorner();
        Wall wall;
        int length = 0;
        switch (face) {
            case SOUTH -> {
                wall = new Wall(new SimpleBlock(data, lowerBounds[0] + padding, y + 1, upperBounds[1] - padding),
                        BlockFace.NORTH);
                length = widthX - 2 * padding;
            }
            case NORTH -> {
                wall = new Wall(new SimpleBlock(data, upperBounds[0] - padding, y + 1, lowerBounds[1] + padding),
                        BlockFace.SOUTH);
                length = widthX - 2 * padding;
            }
            case WEST -> {
                wall = new Wall(new SimpleBlock(data, lowerBounds[0] + padding, y + 1, lowerBounds[1] + padding),
                        BlockFace.EAST);
                length = widthZ - 2 * padding;
            }
            case EAST -> {
                wall = new Wall(new SimpleBlock(data, upperBounds[0] - padding, y + 1, upperBounds[1] - padding),
                        BlockFace.WEST);
                length = widthZ - 2 * padding;
            }
            default -> {
                wall = null;
                TerraformGeneratorPlugin.logger.error("Invalid wall direction requested!");
            }
        }

        return new SimpleEntry<>(wall, length);
    }

    public @NotNull HashMap<Wall, Integer> getFourWalls(@NotNull PopulatorDataAbstract data, int padding) {
        int[] lowerBounds = getLowerCorner();
        int[] upperBounds = getUpperCorner();
        HashMap<Wall, Integer> walls = new HashMap<>();
        Wall north = new Wall(new SimpleBlock(data, lowerBounds[0] + padding, y + 1, upperBounds[1] - padding),
                BlockFace.NORTH);
        Wall south = new Wall(new SimpleBlock(data, upperBounds[0] - padding, y + 1, lowerBounds[1] + padding),
                BlockFace.SOUTH);
        Wall east = new Wall(new SimpleBlock(data, lowerBounds[0] + padding, y + 1, lowerBounds[1] + padding),
                BlockFace.EAST);
        Wall west = new Wall(new SimpleBlock(data, upperBounds[0] - padding, y + 1, upperBounds[1] - padding),
                BlockFace.WEST);

        walls.put(north, widthX - 2 * padding);
        walls.put(south, widthX - 2 * padding);
        walls.put(east, widthZ - 2 * padding);
        walls.put(west, widthZ - 2 * padding);
        return walls;
    }

    public void setRoomPopulator(RoomPopulatorAbstract pop) {
        this.pop = pop;
    }

    public void populate(PopulatorDataAbstract data) {
        if (pop != null) {
            pop.populate(data, this);
        }
    }

    public void fillRoom(@NotNull PopulatorDataAbstract data, Material... mat) {
        fillRoom(data, -1, mat, Material.AIR);
    }

    /**
     * Carves a hollow cube room.
     *
     * @param data    populator data to use for writing
     * @param tile    whether to treat mat as a pattern to tile with. If this is -1,
     *                no repetition will occur and instead mat will be sampled randomly
     * @param mat     the array of materials for the walls, floor and ceiling of the room
     * @param fillMat material to use for the hollow area. Typically a fluid like air or water
     */
    public void fillRoom(@NotNull PopulatorDataAbstract data, int tile, Material[] mat, Material fillMat) {

        new StandardRoomCarver(tile, fillMat).carveRoom(data, this, mat);
    }

    public int[] getCenter() {
        return new int[] {x, y, z};
    }

    public @NotNull SimpleBlock getCenterSimpleBlock(@NotNull PopulatorDataAbstract data) {
        return new SimpleBlock(data, x, y, z);
    }

    public double centralDistanceSquared(int @NotNull [] other) {
        return Math.pow(x - other[0], 2) + Math.pow(y - other[1], 2) + Math.pow(z - other[2], 2);
    }

    public @NotNull CubeRoom getCloneSubsetRoom(int paddingX, int paddingZ)
    {
        return new CubeRoom(
                this.widthX - paddingX * 2,
                this.widthZ - paddingZ * 2,
                this.height,
                this.x,
                this.y,
                this.z
        );
    }

    public boolean isClone(@NotNull CubeRoom other) {
        return this.x == other.x
               && this.y == other.y
               && this.z == other.z
               && this.widthX == other.widthX
               && this.height == other.height
               && this.widthZ == other.widthZ;
    }

    public boolean isOverlapping(@NotNull CubeRoom room) {
        return Math.abs(room.x - this.x) < (Math.abs(room.widthX + this.widthX) / 2) && (Math.abs(room.z - this.z) < (
                Math.abs(room.widthZ + this.widthZ)
                / 2));
    }

    /**
     * @return random 3d coordinates from inside the room.
     */
    public int[] randomCoords(@NotNull Random rand) {
        return randomCoords(rand, 0);
    }

    /**
     * @return random 3d (xyz) coordinates from inside the room.
     */
    public int[] randomCoords(@NotNull Random rand, int pad) {
        return GenUtils.randomCoords(rand,
                new int[] {x - widthX / 2 + pad, y + pad, z - widthZ / 2 + pad},
                new int[] {x + widthX / 2 - pad, y + height - 1 - pad, z + widthZ / 2 - pad}
        );
    }


    /**
     * @param point 2d point (size 2 int array)
     */
    public boolean isPointInside(int @NotNull [] point, int pad) {
        int[] boundOne = getUpperCorner(pad);
        int[] boundTwo = getLowerCorner(pad);

        if (boundOne[0] >= point[0] && boundOne[1] >= point[1]) {
            return boundTwo[0] <= point[0] && boundTwo[1] <= point[1];
        }

        return false;
    }

    /**
     * @param point 2d point (size 2 int array)
     */
    public boolean isPointInside(int @NotNull [] point) {
        int[] boundOne = getUpperCorner();
        int[] boundTwo = getLowerCorner();

        if (boundOne[0] >= point[0] && boundOne[1] >= point[1]) {
            return boundTwo[0] <= point[0] && boundTwo[1] <= point[1];
        }

        return false;
    }

    /**
     * 2D comparison.
     */
    public boolean isPointInside(@NotNull SimpleLocation loc) {
        int[] boundOne = getUpperCorner();
        int[] boundTwo = getLowerCorner();

        if (boundOne[0] >= loc.getX() && boundOne[1] >= loc.getZ()) {
            return boundTwo[0] <= loc.getX() && boundTwo[1] <= loc.getZ();
        }

        return false;
    }

    public @NotNull SimpleLocation getSimpleLocation() {
        return new SimpleLocation(getX(), getY(), getZ());
    }

    /**
     * IGNORES Y AXIS.
     *
     * @param point 2d point (size 2 int array)
     */
    public boolean isPointInside(@NotNull SimpleBlock point) {
        int[] boundOne = getUpperCorner();
        int[] boundTwo = getLowerCorner();

        if (boundOne[0] >= point.getX() && boundOne[1] >= point.getZ()) {
            return boundTwo[0] <= point.getX() && boundTwo[1] <= point.getZ();
        }

        return false;
    }

    public boolean isInside(@NotNull CubeRoom other) {
        for (int[] corner : getAllCorners()) {
            if (!other.isPointInside(corner)) {
                return false;
            }
        }
        return true;
    }

    public boolean envelopesOrIsInside(@NotNull CubeRoom other) {
        return isInside(other) || other.isInside(this);
    }

    /**
     * @return the isActivated
     */
    public boolean isActivated() {
        return isActivated;
    }

    /**
     * @param isActivated the isActivated to set
     */
    public void setActivated(boolean isActivated) {
        this.isActivated = isActivated;
    }

    /**
     * @return 2d X,Z corners
     */
    public int[][] getAllCorners() {
        return getAllCorners(0);
    }

    /**
     * @return 2d X,Z corners
     */
    public int[][] getAllCorners(int padding) {
        int[][] corners = new int[4][2];

        corners[0] = new int[] {x + widthX / 2 - padding, z + widthZ / 2 - padding}; // ++
        corners[1] = new int[] {x - widthX / 2 + padding, z + widthZ / 2 - padding}; // -+
        corners[2] = new int[] {x + widthX / 2 - padding, z - widthZ / 2 + padding}; // +-
        corners[3] = new int[] {x - widthX / 2 + padding, z - widthZ / 2 + padding}; // --

        return corners;
    }


    /**
     * @return 2d X,Z corners
     */
    public int[][] getCornersAlongFace(BlockFace face, int padding) {
        int[][] corners = new int[2][2];

        if (face == BlockFace.NORTH) {
            corners[0] = new int[] {x - widthX / 2 + padding, z - widthZ / 2 + padding}; // --
            corners[1] = new int[] {x + widthX / 2 - padding, z - widthZ / 2 + padding}; // +-
        }
        else if (face == BlockFace.SOUTH) {
            corners[0] = new int[] {x - widthX / 2 + padding, z + widthZ / 2 - padding}; // -+
            corners[1] = new int[] {x + widthX / 2 - padding, z + widthZ / 2 - padding}; // ++
        }
        else if (face == BlockFace.WEST) {
            corners[0] = new int[] {x - widthX / 2 + padding, z - widthZ / 2 + padding}; // --
            corners[1] = new int[] {x - widthX / 2 + padding, z + widthZ / 2 - padding}; // -+
        }
        else if (face == BlockFace.EAST) {
            corners[0] = new int[] {x + widthX / 2 - padding, z - widthZ / 2 + padding}; // --
            corners[1] = new int[] {x + widthX / 2 - padding, z + widthZ / 2 - padding}; // -+
        }

        return corners;
    }

    // Positive x,z corner
    public int[] getUpperCorner() {
        return new int[] {x + widthX / 2, z + widthZ / 2};
    }

    // Negative x,z corner
    public int[] getLowerCorner() {
        return new int[] {x - widthX / 2, z - widthZ / 2};
    }

    public boolean isInRegion(@NotNull LimitedRegion region)
    {
        return region.isInRegion(x - widthX / 2, y, z - widthZ / 2) && region.isInRegion(
                x + widthX / 2,
                y,
                z + widthZ / 2
        );
    }

    // Positive x,z corner
    public int[] getUpperCorner(int pad) {
        int Z = z - pad + widthZ / 2;
        int X = x - pad + widthX / 2;
        if (pad > widthZ / 2) {
            Z = z;
        }
        if (pad > widthX / 2) {
            X = x;
        }
        return new int[] {X, Z};
    }

    // Negative x,z corner
    public int[] getLowerCorner(int pad) {
        int Z = z + pad - widthZ / 2;
        int X = x + pad - widthX / 2;
        if (pad > widthZ / 2) {
            Z = z;
        }
        if (pad > widthX / 2) {
            X = x;
        }
        return new int[] {X, Z};
    }

    /**
     * Forces all 3D coords in the room with the specified padding to be air.
     */
    public void purgeRoomContents(@NotNull PopulatorDataAbstract data, int padding) {
        int[] lowerCorner = getLowerCorner(padding);
        int[] upperCorner = getUpperCorner(padding);
        int lowestY = y + padding;
        int upperY = y + height - padding;

        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                for (int y = lowestY; y <= upperY; y++) {
                    data.setType(x, y, z, Material.AIR);
                }
            }
        }
    }

    public boolean isBig() {
        return widthX * widthZ * height >= 2000;
    }

    public boolean isHuge() {
        return widthX * widthZ * height >= 7000;
    }

    public boolean largerThanVolume(int vol) {
        return widthX * widthZ * height >= vol;
    }

    /**
     * @return the widthX
     */
    public int getWidthX() {
        return widthX;
    }

    /**
     * @param widthX the widthX to set
     */
    public void setWidthX(int widthX) {
        this.widthX = widthX;
    }

    /**
     * @return the widthZ
     */
    public int getWidthZ() {
        return widthZ;
    }

    /**
     * @param widthZ the widthZ to set
     */
    public void setWidthZ(int widthZ) {
        this.widthZ = widthZ;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    public RoomPopulatorAbstract getPop() {
        return pop;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return the z
     */
    public int getZ() {
        return z;
    }

    /**
     * @param z the z to set
     */
    public void setZ(int z) {
        this.z = z;
    }

    public boolean canLRCarve(int chunkX, int chunkZ, LimitedRegion lr){
        return chunkX == x >> 4 //Ensure that the center of the lr is the same chunk
                       && chunkZ == z >> 4
                       && isInRegion(lr); // No rooms that have bounds beyond LR
    }

    public void debugRedGround(@NotNull PopulatorDataAbstract data) {
        int[] lowerCorner = getLowerCorner();
        int[] upperCorner = getUpperCorner();
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                data.setType(x, GenUtils.getHighestGround(data, x, z), z, Material.RED_WOOL);
            }
        }
    }
}
