package org.terraform.coregen;

import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;

import java.util.Arrays;

/**
 * I don't know why Z and X indices are swapped consistently here.
 * I guess it doesn't affect anything but it sure is a fucking
 * war crime
 */
public class ChunkCache {
    public final TerraformWorld tw;
    public final int chunkX, chunkZ;

    /**
     * heightCache caches the FINAL height of the terrain (the one applied to the 
     * world). 
     * <br>
     * dominantBiomeHeightCache holds the non-final height calculation of
     * the most dominant biome at those coordinates.
     * <br>
     * blurredHeightCache will hold intermediate height blurring values
     * (calculated after dominantBiomeHeightCache)
     */
//    float[][] dominantBiomeHeightCache; //KEY 0
//    float[][] blurredHeightCache; //KEY 1
//    float[][] intermediateBlurCache; //KEY 2
//    double[][] heightMapCache; //KEY 3
//    short[][] highestGroundCache; //KEY 4
//    short[][] transformedHeightCache; //KEY 5
    float[] arrayCache; //These 6 arrays are now one big array. No more nested pointers
    BiomeBank[] biomeCache;

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
        //initInternalCache(); THIS IS COMMENTED ON PURPOSE.
        //THIS CONSTRUCTOR IS ONLY USED FOR CACHE HITS, SO DOES NOT INITIALIZE.
        //Good practice mandates that we mark this constructor with protected,
        //but the class calling this is in another package, so too bad!
    }

    public static int getChunkCoordinate(int coordinate) {
        return coordinate >> 4;
    }

    public void initInternalCache() {
        /*
        This used to be a nested for loop initiating 6 2D float arrays.
        However, neither the compiler or JVM bothered optimizing it
        and it became a fucking hotspot. Because of that, it is
        more appropriate to just inline it manually into this
        disgusting monster array with hardcoded numbers because of
        how speed sensitive this is.
        */
        arrayCache = new float[1536]; //6*256 for 6 separate caches

        /*
        If arrays.fill gives further speed problems, just use
        System.arraycopy next time with a static final MIN_VALUE array
        Problems might occur because the lib itself uses a for loop.
        Optimization lies entirely at the mercy of the running JVM.
        */
        Arrays.fill(arrayCache, Float.MIN_VALUE);

        biomeCache = new BiomeBank[256];
    }


    public float getDominantBiomeHeight(int rawX, int rawZ) {
        /*
          A general explanation of this shitty inlining:
          rawX & 0xF will return internal chunk coordinates [0-15]. This
          happens because the chunk coordinates are the LSB, so ANDing by
          0xF will return the bits needed to give 0-15.
         */
        return arrayCache[(rawX&0xF)+16*(rawZ&0xF)];
    }

    /**
     *
     * @param rawX BLOCK COORD x
     * @param rawZ BLOCK COORD z
     * @param value dominant biome height to cache
     */
    public void cacheDominantBiomeHeight(int rawX, int rawZ, float value) {
        arrayCache[(rawX&0xF)+16*(rawZ&0xF)] = value;
    }

    public double getHeightMapHeight(int rawX, int rawZ) {
        return arrayCache[768 + (rawX&0xF)+16*(rawZ&0xF)];
    }

    public short getHighestGround(int rawX, int rawZ) {
        return (short) arrayCache[1024 + (rawX&0xF)+16*(rawZ&0xF)];
    }

    /**
     * This is solely for surface cave carving use as surface
     * caves may modify heights.
     * @return the ACTUAL mutable copy from the cache.
     */
    public short getTransformedHeight(int chunkSubX, int chunkSubZ){
        return (short) arrayCache[1280 + chunkSubX + 16*chunkSubZ];
    }

    public void writeTransformedHeight(int chunkSubX, int chunkSubZ, short val){
        arrayCache[1280 + chunkSubX + 16*chunkSubZ] = val;
    }

    /**
     *
     * @param rawX BLOCK COORD x
     * @param rawZ BLOCK COORD z
     * @param value height to cache
     */
    public void cacheHeightMap(int rawX, int rawZ, double value) {
        arrayCache[768 + (rawX&0xF)+16*(rawZ&0xF)] = (float) value;
    }

    /**
     *
     * @param rawX BLOCK COORD x
     * @param rawZ BLOCK COORD z
     * @param value height to cache
     */
    public void cacheHighestGround(int rawX, int rawZ, short value) {
        arrayCache[1024 + (rawX&0xF)+16*(rawZ&0xF)] = value;
    }

    /**
     * Only used in TerraformGenerator for height calculations.
     * Do not use elsewhere.
     */
    public void cacheTransformedHeight(short[][] heights) {
        for(int i = 0; i < 16; i++)
            for(int j = 0; j < 16; j++)
                arrayCache[1280 + i + 16*j] = heights[i][j];
    }
    
    public float getBlurredHeight(int rawX, int rawZ) {
        return arrayCache[256 + (rawX&0xF)+16*(rawZ&0xF)];
    }

    /**
     * MEANT FOR USE ONLY IN THE BLURRING PROCESS.
     */
    public double getIntermediateBlurHeight(int rawX, int rawZ)
    {
        return arrayCache[512 + (rawX&0xF)+16*(rawZ&0xF)];
    }


    /**
     *
     * @param rawX BLOCK COORD x
     * @param rawZ BLOCK COORD z
     * @param value height to cache
     */
    public void cacheBlurredHeight(int rawX, int rawZ, float value) {
        arrayCache[256 + (rawX&0xF)+16*(rawZ&0xF)] = value;
    }

    /**
     * MEANT FOR USE ONLY IN THE BLURRING PROCESS.
     * @param rawX BLOCK COORD x
     * @param rawZ BLOCK COORD z
     * @param value height to cache
     */
    public void cacheIntermediateBlurredHeight(int rawX, int rawZ, float value) {
        arrayCache[512 + (rawX&0xF)+16*(rawZ&0xF)] = value;
    }


    public BiomeBank getBiome(int rawX, int rawZ) {
        return biomeCache[(rawX&0xF)+16*(rawZ&0xF)];
    }

    /**
     *
     * @param rawX BLOCK COORD x
     * @param rawZ BLOCK COORD z
     * @param value biome to cache
     */
    public BiomeBank cacheBiome(int rawX, int rawZ, BiomeBank value) {
        biomeCache[(rawX&0xF)+16*(rawZ&0xF)] = value;
        return value;
    }

    /**
     * Nobody benchmarked the performance of this hashcode.
     * oh well.
     */
    @Override
    public int hashCode() {
        return tw.hashCode() ^ (chunkX + chunkZ * 31);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkCache chunk)) return false;
        return this.tw == chunk.tw
                && this.chunkX == chunk.chunkX
                && this.chunkZ == chunk.chunkZ;
    }
}