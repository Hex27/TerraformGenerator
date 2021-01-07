package org.terraform.coregen;

import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;

public class ChunkCache {
    public final TerraformWorld tw;

    public final int chunkX;
    public final int chunkZ;

    double[][] heightCache = new double[16][16];
    BiomeBank[][] biomeCache = new BiomeBank[16][16];

    public ChunkCache(TerraformWorld tw, int x, int z) {
        this.tw = tw;

        this.chunkX = getChunkCoordinate(x);
        this.chunkZ = getChunkCoordinate(z);
    }

    public static int getChunkCoordinate(int coordinate) {
        return coordinate >> 4;
    }

    // Coordinates from 0 to 15
    private static int getCoordinateInsideChunk(int x) {
        return (x < 0 ? Math.abs(x + 1) % 16 : x % 16);
    }

    public double getHeight(int rawX, int rawZ) {
        return heightCache[getCoordinateInsideChunk(rawZ)][getCoordinateInsideChunk(rawX)];
    }

    public void cacheHeight(int rawX, int rawZ, double value) {
        heightCache[getCoordinateInsideChunk(rawZ)][getCoordinateInsideChunk(rawX)] = value;
    }

    public BiomeBank getBiome(int rawX, int rawZ) {
        return biomeCache[getCoordinateInsideChunk(rawZ)][getCoordinateInsideChunk(rawX)];
    }

    public BiomeBank cacheBiome(int rawX, int rawZ, BiomeBank value) {
        biomeCache[getCoordinateInsideChunk(rawZ)][getCoordinateInsideChunk(rawX)] = value;
        return value;
    }
}