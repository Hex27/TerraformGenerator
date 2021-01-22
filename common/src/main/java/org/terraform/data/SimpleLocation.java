package org.terraform.data;

import org.bukkit.block.BlockFace;

import java.util.Objects;

public class SimpleLocation {

    private int x;
    private int y;
    private int z;

    public SimpleLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public SimpleLocation(SimpleLocation other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public SimpleLocation getRelative(int x, int y, int z) {
        return new SimpleLocation(this.x + x, this.y + y, this.z + z);
    }

    public SimpleLocation getRelative(BlockFace face) {
        return new SimpleLocation(this.x + face.getModX(), this.y + face.getModY(), this.z + face.getModZ());
    }

    public SimpleLocation getRelative(BlockFace face, int i) {
        return new SimpleLocation(this.x + face.getModX() * i, this.y + face.getModY() * i, this.z + face.getModZ() * i);
    }

    public int distanceSqr(SimpleLocation o) {
        return (int) (Math.pow(o.x - x, 2) + Math.pow(o.y - y, 2) + Math.pow(o.z - z, 2));
    }

    public int distanceSqr(int nx, int ny, int nz) {
        return (int) (Math.pow(nx - x, 2) + Math.pow(ny - y, 2) + Math.pow(nz - z, 2));
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, 93929798);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SimpleLocation) {
            SimpleLocation sLoc = (SimpleLocation) obj;
            return sLoc.x == x && sLoc.y == y && sLoc.z == z;
        }
        return false;
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
