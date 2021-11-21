package org.terraform.coregen;

import org.bukkit.Axis;
import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;

public class ChunkCache {
    public final TerraformWorld tw;
    public final int chunkX, chunkZ;

    /**
     * heightCache caches the FINAL height of the terrain (the one applied to the 
     * world). dominantBiomeHeightCache holds the non-final height calculation of
     * the most dominant biome at those coordinates.
     */
    double[][] dominantBiomeHeightCache;
    double[][] heightMapCache;
    short[][] highestGroundCache;
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
    	highestGroundCache = new short[16][16];
    	for(short i = 0; i < 16; i++)
    		for(short j = 0; j < 16; j++)
    			highestGroundCache[i][j] = Short.MIN_VALUE;
        heightMapCache = new double[16][16];
        dominantBiomeHeightCache = new double[16][16];
        biomeCache = new BiomeBank[16][16];
    }

    // Coordinates from 0 to 15
    private int getCoordinateInsideChunk(int j, Axis ax) {
        return (ax == Axis.X) ? j - this.chunkX * 16 : j - this.chunkZ * 16;
    }

    public double getDominantBiomeHeight(int rawX, int rawZ) {
        return dominantBiomeHeightCache[getCoordinateInsideChunk(rawZ, Axis.Z)][getCoordinateInsideChunk(rawX, Axis.X)];
    }

    /**
     *
     * @param rawX BLOCK COORD x
     * @param rawZ BLOCK COORD z
     * @param value dominant biome height to cache
     */
    public void cacheDominantBiomeHeight(int rawX, int rawZ, double value) {
    	dominantBiomeHeightCache[getCoordinateInsideChunk(rawZ, Axis.Z)][getCoordinateInsideChunk(rawX, Axis.X)] = value;
    }

    public double getHeightMapHeight(int rawX, int rawZ) {
        return heightMapCache[getCoordinateInsideChunk(rawZ, Axis.Z)][getCoordinateInsideChunk(rawX, Axis.X)];
    }

    public short getHighestGround(int rawX, int rawZ) {
        return highestGroundCache[getCoordinateInsideChunk(rawZ, Axis.Z)][getCoordinateInsideChunk(rawX, Axis.X)];
    }


    /**
     *
     * @param rawX BLOCK COORD x
     * @param rawZ BLOCK COORD z
     * @param value height to cache
     */
    public void cacheHeightMap(int rawX, int rawZ, double value) {
        heightMapCache[getCoordinateInsideChunk(rawZ, Axis.Z)][getCoordinateInsideChunk(rawX, Axis.X)] = value;
    }

    /**
     *
     * @param rawX BLOCK COORD x
     * @param rawZ BLOCK COORD z
     * @param value height to cache
     */
    public void cacheHighestGround(int rawX, int rawZ, short value) {
        highestGroundCache[getCoordinateInsideChunk(rawZ, Axis.Z)][getCoordinateInsideChunk(rawX, Axis.X)] = value;
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