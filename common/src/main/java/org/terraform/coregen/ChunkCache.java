package org.terraform.coregen;

import org.bukkit.Axis;
import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;

public class ChunkCache {
    public final TerraformWorld tw;
    public final int chunkX, chunkZ;

    double[][] heightCache;
    BiomeBank[][] biomeCache;

    public ChunkCache(TerraformWorld tw, int chunkX, int chunkZ) {
        this.tw = tw;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        initInternalCache();
    }

    /**
     * Y is not used, but will force the dev to remember that these are block coords.
     */
    public ChunkCache(TerraformWorld tw, int rawX, int rawY, int rawZ) {
        this.tw = tw;
        this.chunkX = getChunkCoordinate(rawX);
        this.chunkZ = getChunkCoordinate(rawZ);
    }

    public static int getChunkCoordinate(int coordinate) {
        return coordinate >> 4;
    }

    public void initInternalCache() {
        heightCache = new double[16][16];
        biomeCache = new BiomeBank[16][16];
    }

    // Coordinates from 0 to 15
    private int getCoordinateInsideChunk(int j, Axis ax) {
        return (ax == Axis.X) ? j - this.chunkX * 16 : j - this.chunkZ * 16;
    }

    public double getHeight(int rawX, int rawZ) {
        return heightCache[getCoordinateInsideChunk(rawZ, Axis.Z)][getCoordinateInsideChunk(rawX, Axis.X)];
    }

    /**
     *
     * @param rawX BLOCK COORD x
     * @param rawZ BLOCK COORD z
     * @param value height to cache
     */
    public void cacheHeight(int rawX, int rawZ, double value) {
        heightCache[getCoordinateInsideChunk(rawZ, Axis.Z)][getCoordinateInsideChunk(rawX, Axis.X)] = value;
    }

    public BiomeBank getBiome(int rawX, int rawZ) {
        return biomeCache[getCoordinateInsideChunk(rawZ, Axis.Z)][getCoordinateInsideChunk(rawX, Axis.X)];
    }

    /**
     *
     * @param rawX BLOCK COORD x
     * @param rawZ BLOCK COORD z
     * @param value biome to cache
     */
    public BiomeBank cacheBiome(int rawX, int rawZ, BiomeBank value) {
        biomeCache[getCoordinateInsideChunk(rawZ, Axis.Z)][getCoordinateInsideChunk(rawX, Axis.X)] = value;
        return value;
    }

    public boolean areCoordsInside(int rawX, int rawZ) {
        return chunkX == rawX >> 4 && chunkZ == rawZ >> 4;
    }

    @Override
    public int hashCode() {
        return tw.hashCode() ^ (chunkX + chunkZ * 31);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkCache)) return false;
        ChunkCache chunk = (ChunkCache) obj;
        return this.tw == chunk.tw && this.chunkX == chunk.chunkX && this.chunkZ == chunk.chunkZ;
    }
}