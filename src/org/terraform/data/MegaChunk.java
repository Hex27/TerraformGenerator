package org.terraform.data;

import org.terraform.main.TConfigOption;
import org.terraform.utils.GenUtils;

import java.util.Random;

/**
 * Refers to a cluster of 64x64 chunks
 * Used for spawning structures.
 */
public class MegaChunk {
    private final int x, z;

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

    public MegaChunk getRelative(int x, int z) {
        return new MegaChunk(this.x + x, this.z + z);
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

    public boolean containsXZBlockCoords(int x, int z) {
        MegaChunk mc = new MegaChunk(x, 0, z);
        return mc.equals(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MegaChunk) {
            MegaChunk megaChunk = (MegaChunk) obj;
            return this.x == megaChunk.x && this.z == megaChunk.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 5;

        result = prime * result + x;
        result = prime * result + z;

        return result;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
