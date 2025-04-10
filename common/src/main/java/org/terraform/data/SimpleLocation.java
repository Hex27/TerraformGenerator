package org.terraform.data;

import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

public record SimpleLocation(int x, int y, int z) {

    public SimpleLocation(@NotNull SimpleLocation other) {
        this(other.x, other.y, other.z);
    }

    public @NotNull SimpleLocation getRelative(int x, int y, int z) {
        return new SimpleLocation(this.x + x, this.y + y, this.z + z);
    }

    public @NotNull SimpleLocation getRelative(@NotNull BlockFace face) {
        return new SimpleLocation(this.x + face.getModX(), this.y + face.getModY(), this.z + face.getModZ());
    }

    public @NotNull SimpleLocation getRelative(@NotNull BlockFace face, int i) {
        return new SimpleLocation(
                this.x + face.getModX() * i,
                this.y + face.getModY() * i,
                this.z + face.getModZ() * i
        );
    }

    public float distance(@NotNull SimpleLocation o) {
        return (float) Math.sqrt(Math.pow(o.x - x, 2) + Math.pow(o.y - y, 2) + Math.pow(o.z - z, 2));
    }

    public float distanceSqr(@NotNull SimpleLocation o) {
        return (float) (Math.pow(o.x - x, 2) + Math.pow(o.y - y, 2) + Math.pow(o.z - z, 2));
    }

    public float distanceQuad(@NotNull SimpleLocation o) {
        return (float) Math.pow(Math.pow(o.x - x, 2) + Math.pow(o.y - y, 2) + Math.pow(o.z - z, 2), 4);
    }

    public float distanceSqr(int nx, int ny, int nz) {
        return (float) (Math.pow(nx - x, 2) + Math.pow(ny - y, 2) + Math.pow(nz - z, 2));
    }

    /**
     * Returns a value between 0 and 2PI to represent a 360 degree angle
     * offset of the other location "o" compared to this location.
     */
    public float twoDAngleTo(@NotNull SimpleLocation o) {

        // Handle absolute cases first
        if (o.x == x && o.z == z) {
            return 0f;
        }
        else if (o.x == x && o.z > z) {
            return 0f;
        }
        else if (o.x == x) {
            return (float) Math.PI;
        }
        else if (o.x > x && o.z == z) {
            return (float) Math.PI / 2;
        }
        else if (o.x < x && o.z == z) {
            return (float) (3 * Math.PI / 2);
        }
        // Handle CAST trigo calculations
        else if (o.x > x && o.z > z) { // A segment
            return (float) Math.atan((double) (o.x - x) / (o.z - z));
        }
        else if (o.x > x) { // C segment
            return (float) (Math.atan((double) (z - o.z) / (o.x - x)) + Math.PI / 2);
        }
        else if (o.z < z) { // T segment
            return (float) (Math.atan((double) (x - o.x) / (z - o.z)) + Math.PI);
        }
        else { // S segment
            return (float) (Math.atan((double) (o.z - z) / (x - o.x)) + 3 * Math.PI / 2);
        }
    }

    /**
     * Returns a value between 0 and PI to represent a 180 degree angle
     * offset of the other location "o" compared to this location.
     * <p>
     * Will return the same value for segment CA and ST (mirrored)
     */
    public float twoDAngleWrapTo(@NotNull SimpleLocation o) {

        // Handle absolute cases first
        if (o.x == x && o.z == z) {
            return 0f;
        }
        else if (o.x == x && o.z > z) {
            return 0;
        }
        else if (o.x == x) {
            return (float) Math.PI;
        }
        else if (o.x > x && o.z == z) {
            return (float) Math.PI / 2;
        }
        else if (o.x < x && o.z == z) {
            return (float) (Math.PI / 2);
        }
        // Handle CAST trigo calculations
        else if (o.x > x && o.z > z) { // A segment
            return (float) Math.atan((double) (o.x - x) / (o.z - z));
        }
        else if (o.x > x) { // C segment
            return (float) (Math.atan((double) (z - o.z) / (o.x - x)) + Math.PI / 2);
        }
        else if (o.z < z) { // T segment
            return (float) (Math.atan((double) (z - o.z) / (x - o.x)) + Math.PI / 2);
        }
        else { // S segment
            return (float) Math.atan((double) (x - o.x) / (o.z - z));
        }
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @return the z
     */
    public int getZ() {
        return z;
    }

    @Override
    public @NotNull String toString() {
        return this.x + "," + this.y + "," + this.z;
    }

    public @NotNull SimpleLocation getAtY(int newy) {
        return new SimpleLocation(x, newy, z);
    }

    public @NotNull SimpleLocation getUp(int n) {
        return getRelative(0, n, 0);
    }

    public @NotNull SimpleLocation getUp() {
        return getRelative(0, 1, 0);
    }

    public @NotNull SimpleLocation getDown(int n) {
        return getRelative(0, -n, 0);
    }

    public @NotNull SimpleLocation getDown() {
        return getRelative(0, -1, 0);
    }
}
