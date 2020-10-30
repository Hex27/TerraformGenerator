package org.terraform.data;

import org.terraform.main.TConfigOption;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MegaChunk {

    /**
     * Refers to a cluster of 64x64 chunks
     * Used for spawning structures.
     */

    private int x;
    private int z;

    public MegaChunk(SimpleChunkLocation sLoc) {
        this(sLoc.getX(), sLoc.getZ());
    }

    public MegaChunk(int x, int y, int z) {
        this(x >> 4, z >> 4);
    }

    //A megachunk is (2^6) 64 chunks wide. (4096 chunks)
    public MegaChunk(int chunkX, int chunkZ) {
        this.x = chunkX >> TConfigOption.STRUCTURES_MEGACHUNK_BITSHIFTS.getInt();
        this.z = chunkZ >> TConfigOption.STRUCTURES_MEGACHUNK_BITSHIFTS.getInt();
    }

    public MegaChunk getRelative(int nx, int nz) {
        MegaChunk other = new MegaChunk(0, 0);
        other.x = this.x + nx;
        other.z = this.z + nz;
        return other;
    }

    /**
     * @param rand
     * @return A random pair of xz block coords within the mega chunk
     */
    public int[] getRandomCoords(Random rand) {
        int lowChunkX = this.x << TConfigOption.STRUCTURES_MEGACHUNK_BITSHIFTS.getInt();
        int lowChunkZ = this.z << TConfigOption.STRUCTURES_MEGACHUNK_BITSHIFTS.getInt();
        int highChunkX = (this.x << TConfigOption.STRUCTURES_MEGACHUNK_BITSHIFTS.getInt()) | 15;
        int highChunkZ = (this.z << TConfigOption.STRUCTURES_MEGACHUNK_BITSHIFTS.getInt()) | 15;

        int lowX = lowChunkX << 4;
        int lowZ = lowChunkZ << 4;
        int highX = (highChunkX << 4) | 15;
        int highZ = (highChunkZ << 4) | 15;

        //Pad the sides. Never generate on the side of a mega chunk.
        int x = GenUtils.randInt(rand, lowX + 64, highX - 64);
        int z = GenUtils.randInt(rand, lowZ + 64, highZ - 64);
        return new int[]{x, z};
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MegaChunk) {
            return ((MegaChunk) o).x == this.x
                    && ((MegaChunk) o).z == this.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 323522773 + x + z;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @return the z
     */
    public int getZ() {
        return z;
    }
}
