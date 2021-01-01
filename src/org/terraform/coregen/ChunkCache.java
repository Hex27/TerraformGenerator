package org.terraform.coregen;

import org.terraform.data.TerraformWorld;

public class ChunkCache {
    public final TerraformWorld tw;

    public final int chunkX;
    public final int chunkZ;

    double[][] cache = new double[16][16];

    public ChunkCache(TerraformWorld tw, int x, int z) {
        this.tw = tw;

        this.chunkX = getChunkCoordinate(x);
        this.chunkZ = getChunkCoordinate(z);
    }

    public static int getChunkCoordinate(int coordinate) {
        if (coordinate < 0) return (coordinate - 16) / 16;
        return coordinate / 16;
    }

    private static int transformBigCoordinate(int x) {
        if (x < 0) return Math.abs(x + 1) % 16;
        else return x % 16;
    }

    public double getHeight(int x, int z) {
        return cache[transformBigCoordinate(z)][transformBigCoordinate(x)];
    }

    public void cacheHeight(int x, int z, double value) {
        cache[transformBigCoordinate(z)][transformBigCoordinate(x)] = value;
    }
}