package org.terraform.structure.room;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
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

    public SimpleEntry<Wall, Integer> getWall(PopulatorDataAbstract data, BlockFace face, int padding) {
        int[] lowerBounds = getLowerCorner();
        int[] upperBounds = getUpperCorner();
        Wall wall;
        int length = 0;
        switch (face) {
            case SOUTH:
                wall = new Wall(
                        new SimpleBlock(data, lowerBounds[0] + padding, y + 1, upperBounds[1] - padding)
                        , BlockFace.NORTH);
                length = widthX - 2 * padding;
                break;
            case NORTH:
                wall = new Wall(
                        new SimpleBlock(data, upperBounds[0] - padding, y + 1, lowerBounds[1] + padding)
                        , BlockFace.SOUTH);
                length = widthX - 2 * padding;
                break;
            case WEST:
                wall = new Wall(
                        new SimpleBlock(data, lowerBounds[0] + padding, y + 1, lowerBounds[1] + padding)
                        , BlockFace.EAST);
                length = widthZ - 2 * padding;
                break;
            case EAST:
                wall = new Wall(
                        new SimpleBlock(data, upperBounds[0] - padding, y + 1, upperBounds[1] - padding)
                        , BlockFace.WEST);
                length = widthZ - 2 * padding;
                break;
            default:
                wall = null;
                TerraformGeneratorPlugin.logger.error("Invalid wall direction requested!");
                break;
        }
        SimpleEntry<Wall, Integer> walls = new SimpleEntry<Wall, Integer>(wall, length);

        return walls;
    }

    public HashMap<Wall, Integer> getFourWalls(PopulatorDataAbstract data, int padding) {
        int[] lowerBounds = getLowerCorner();
        int[] upperBounds = getUpperCorner();
        HashMap<Wall, Integer> walls = new HashMap<>();
        Wall north = new Wall(
                new SimpleBlock(data, lowerBounds[0] + padding, y + 1, upperBounds[1] - padding)
                , BlockFace.NORTH);
        Wall south = new Wall(
                new SimpleBlock(data, upperBounds[0] - padding, y + 1, lowerBounds[1] + padding)
                , BlockFace.SOUTH);
        Wall east = new Wall(
                new SimpleBlock(data, lowerBounds[0] + padding, y + 1, lowerBounds[1] + padding)
                , BlockFace.EAST);
        Wall west = new Wall(
                new SimpleBlock(data, upperBounds[0] - padding, y + 1, upperBounds[1] - padding)
                , BlockFace.WEST);

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
        if (pop != null) pop.populate(data, this);
    }

    public void fillRoom(PopulatorDataAbstract data, Material[] mat) {
        fillRoom(data, -1, mat, Material.AIR);
    }

    public void fillRoom(PopulatorDataAbstract data, int tile, Material[] mat, Material fillMat) {
        int tileIndex = 0;
        if (mat[0] != Material.BARRIER)
            //Create a solid block with the specified width
            for (int nx = x - widthX / 2; nx <= x + widthX / 2; nx++) {
                for (int ny = y; ny <= y + height; ny++) {
                    for (int nz = z - widthZ / 2; nz <= z + widthZ / 2; nz++) {
                        if (data.getType(nx, ny, nz) == Material.CAVE_AIR)
                            continue;
                        if (tile == -1) data.setType(nx, ny, nz, GenUtils.randMaterial(mat));
                        else {
                            data.setType(nx, ny, nz, mat[(Math.abs(nz + widthZ / 2 + ny + nx + widthX / 2 - tileIndex)) % mat.length]);
                            tileIndex += 1;
                            if (tileIndex == 2) tileIndex = 0;
                        }
                    }
                }
            }

        //Hollow out the room
        for (int nx = x - widthX / 2 + 1; nx <= x + widthX / 2 - 1; nx++) {
            for (int ny = y + 1; ny <= y + height - 1; ny++) {
                for (int nz = z - widthZ / 2 + 1; nz <= z + widthZ / 2 - 1; nz++) {
//					if(!fillMat)
//						data.setType(nx, ny, nz, Material.AIR);
//					else
//						data.setType(nx,ny,nz,Material.CAVE_AIR);
                    data.setType(nx, ny, nz, fillMat);
                }
            }
        }
    }

    public int[] getCenter() {
        return new int[]{x, y, z};
    }

    public double centralDistanceSquared(int[] other) {
        return Math.pow(x - other[0], 2) + Math.pow(y - other[1], 2) + Math.pow(z - other[2], 2);
    }

    public boolean isClone(CubeRoom other) {
        return this.x == other.x
                && this.y == other.y
                && this.z == other.z
                && this.widthX == other.widthX
                && this.height == other.height
                && this.widthZ == other.widthZ;
    }

    public boolean isOverlapping(CubeRoom room) {
//		int[][] corners = getAllCorners();
//		int[][] otherCorners = room.getAllCorners();
//		for(int i = 0; i < 4; i++){
//			if(room.isPointInside(corners[i]))
//				return true;
//			if(isPointInside(otherCorners[i]))
//				return true;
//		}
//		
        return Math.abs(room.x - this.x) < (Math.abs(room.widthX + this.widthX) / 2)
                && (Math.abs(room.z - this.z) < (Math.abs(room.widthZ + this.widthZ) / 2));
    }

    /**
     * @return random coordinates from inside the room.
     */
    public int[] randomCoords(Random rand) {
        return randomCoords(rand, 0);
    }

    /**
     * @return random 3d (xyz) coordinates from inside the room.
     */
    public int[] randomCoords(Random rand, int pad) {
        return GenUtils.randomCoords(rand,
                new int[]{x - widthX / 2 + pad, y + pad, z - widthZ / 2 + pad},
                new int[]{x + widthX / 2 - pad, y + height - 1 - pad, z + widthZ / 2 - pad});
    }

    /**
     * @param point 2d point (size 2 int array)
     */
    public boolean isPointInside(int[] point) {
        int[] boundOne = getUpperCorner();
        int[] boundTwo = getLowerCorner();

        if (boundOne[0] >= point[0]
                && boundOne[1] >= point[1]) {
            return boundTwo[0] <= point[0]
                    && boundTwo[1] <= point[1];
        }

        return false;
    }

    /**
     * 2D comparison.
     */
    public boolean isPointInside(SimpleLocation loc) {
        int[] boundOne = getUpperCorner();
        int[] boundTwo = getLowerCorner();

        if (boundOne[0] >= loc.getX()
                && boundOne[1] >= loc.getZ()) {
            return boundTwo[0] <= loc.getX()
                    && boundTwo[1] <= loc.getZ();
        }

        return false;
    }
    
    public SimpleLocation getSimpleLocation() {
    	return new SimpleLocation(getX(),getY(),getZ());
    }

    /**
     * IGNORES Y AXIS.
     * @param point 2d point (size 2 int array)
     */
    public boolean isPointInside(SimpleBlock point) {
        int[] boundOne = getUpperCorner();
        int[] boundTwo = getLowerCorner();

        if (boundOne[0] >= point.getX()
                && boundOne[1] >= point.getZ()) {
            return boundTwo[0] <= point.getX()
                    && boundTwo[1] <= point.getZ();
        }

        return false;
    }

    public boolean isInside(CubeRoom other) {
        for (int[] corner : getAllCorners()) {
            if (!other.isPointInside(corner))
                return false;
        }
        return true;
    }

    public boolean envelopesOrIsInside(CubeRoom other) {
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

        corners[0] = new int[]{x + widthX / 2 - padding, z + widthZ / 2 - padding}; //++
        corners[1] = new int[]{x - widthX / 2 + padding, z + widthZ / 2 - padding}; //-+
        corners[2] = new int[]{x + widthX / 2 - padding, z - widthZ / 2 + padding}; //+-
        corners[3] = new int[]{x - widthX / 2 + padding, z - widthZ / 2 + padding}; //--

        return corners;
    }


    /**
     * @return 2d X,Z corners
     */
    public int[][] getCornersAlongFace(BlockFace face, int padding) {
        int[][] corners = new int[2][2];

        if (face == BlockFace.NORTH) {
            corners[0] = new int[]{x - widthX / 2 + padding, z - widthZ / 2 + padding}; //--
            corners[1] = new int[]{x + widthX / 2 - padding, z - widthZ / 2 + padding}; //+-
        } else if (face == BlockFace.SOUTH) {
            corners[0] = new int[]{x - widthX / 2 + padding, z + widthZ / 2 - padding}; //-+
            corners[1] = new int[]{x + widthX / 2 - padding, z + widthZ / 2 - padding}; //++
        } else if (face == BlockFace.WEST) {
            corners[0] = new int[]{x - widthX / 2 + padding, z - widthZ / 2 + padding}; //--
            corners[1] = new int[]{x - widthX / 2 + padding, z + widthZ / 2 - padding}; //-+
        } else if (face == BlockFace.EAST) {
            corners[0] = new int[]{x + widthX / 2 - padding, z - widthZ / 2 + padding}; //--
            corners[1] = new int[]{x + widthX / 2 - padding, z + widthZ / 2 - padding}; //-+
        }

        return corners;
    }

    //Positive x,z corner
    public int[] getUpperCorner() {
        return new int[]{x + widthX / 2, z + widthZ / 2};
    }

    //Negative x,z corner
    public int[] getLowerCorner() {
        return new int[]{x - widthX / 2, z - widthZ / 2};
    }

    //Positive x,z corner
    public int[] getUpperCorner(int pad) {
        int Z = z - pad + widthZ / 2;
        int X = x - pad + widthX / 2;
        if (pad > widthZ / 2) Z = z;
        if (pad > widthX / 2) X = x;
        return new int[]{X, Z};
    }

    //Negative x,z corner
    public int[] getLowerCorner(int pad) {
        int Z = z + pad - widthZ / 2;
        int X = x + pad - widthX / 2;
        if (pad > widthZ / 2) Z = z;
        if (pad > widthX / 2) X = x;
        return new int[]{X, Z};
    }

    public boolean isBig() {
        return widthX * widthZ * height >= 2000;
    }

    public boolean isHuge() {
        return widthX * widthZ * height >= 7000;
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
}
