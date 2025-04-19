package org.terraform.coregen;

import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.datastructs.CompressedChunkBools;

import java.util.Arrays;

/**
 * I don't know why Z and X indices are swapped consistently here.
 * I guess it doesn't affect anything but it sure is a fucking
 * war crime
 */
public class ChunkCache {
    public final TerraformWorld tw;
    public final int chunkX, chunkZ;
    public static final float CHUNKCACHE_INVAL = TerraformGeneratorPlugin.injector.getMinY() - 1;

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
    float[] heightMapCache;
    short[] highestGroundCache;
    short[] transformedGroundCache;
    float[] yBarrierNoiseCache;

    CompressedChunkBools solids;
    BiomeBank[] biomeCache;

    public ChunkCache(TerraformWorld tw, int chunkX, int chunkZ) {
        this.tw = tw;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        initInternalCache();
    }

    private void initInternalCache() {
        heightMapCache = new float[256];
        Arrays.fill(heightMapCache, CHUNKCACHE_INVAL);
        transformedGroundCache = new short[256];
        Arrays.fill(transformedGroundCache, (short) CHUNKCACHE_INVAL);
        yBarrierNoiseCache = new float[256];
        Arrays.fill(yBarrierNoiseCache, CHUNKCACHE_INVAL);
        highestGroundCache = new short[256];
        Arrays.fill(highestGroundCache, (short) CHUNKCACHE_INVAL);

        /*
        If arrays.fill gives further speed problems, just use
        System.arraycopy next time with a static final MIN_VALUE array
        Problems might occur because the lib itself uses a for loop.
        Optimization lies entirely at the mercy of the running JVM.
        */

        //11/4/2025 not fucking adding more things to the sacred array are ya???
        solids = new CompressedChunkBools();
        biomeCache = new BiomeBank[256];
    }

    public void cacheSolid(int interChunkX, int interChunkY, int interChunkZ)
    {
        solids.set(interChunkX,interChunkY,interChunkZ);
    }
    public void cacheNonSolid(int interChunkX, int interChunkY, int interChunkZ)
    {
        solids.unSet(interChunkX,interChunkY,interChunkZ);
    }
    public boolean isSolid(int interChunkX, int interChunkY, int interChunkZ)
    {
        return solids.isSet(interChunkX,interChunkY,interChunkZ);
    }

    public double getHeightMapHeight(int rawX, int rawZ) {
        return heightMapCache[(rawX & 0xF) + 16 * (rawZ & 0xF)];
    }

    public short getHighestGround(int rawX, int rawZ) {
        return highestGroundCache[(rawX & 0xF) + 16 * (rawZ & 0xF)];
    }

    /**
     * This is solely for surface cave carving use as surface
     * caves may modify heights.
     *
     * @return the ACTUAL mutable copy from the cache.
     */
    public short getTransformedHeight(int chunkSubX, int chunkSubZ) {
        return transformedGroundCache[chunkSubX + 16 * chunkSubZ];
    }

    public void writeTransformedHeight(int chunkSubX, int chunkSubZ, short val) {
        transformedGroundCache[chunkSubX + 16 * chunkSubZ] = val;
    }

    /**
     * As usual, the solution to any pain point is caching it like an idiot
     * @return the noise calculated in NoiseCaveRegistry.YBarrier. Only the noise.
     */
    public float getYBarrierNoise(int chunkSubX, int chunkSubZ) {
        return yBarrierNoiseCache[chunkSubX + 16 * chunkSubZ];
    }

    public void cacheYBarrierNoise(int chunkSubX, int chunkSubZ, float val) {
        yBarrierNoiseCache[chunkSubX + 16 * chunkSubZ] = val;
    }

    /**
     * @param rawX  BLOCK COORD x
     * @param rawZ  BLOCK COORD z
     * @param value height to cache
     */
    public void cacheHeightMap(int rawX, int rawZ, double value) {
        heightMapCache[(rawX & 0xF) + 16 * (rawZ & 0xF)] = (float) value;
    }

    /**
     * @param rawX  BLOCK COORD x
     * @param rawZ  BLOCK COORD z
     * @param value height to cache
     */
    public void cacheHighestGround(int rawX, int rawZ, short value) {
        highestGroundCache[(rawX & 0xF) + 16 * (rawZ & 0xF)] = value;
    }

    public BiomeBank getBiome(int rawX, int rawZ) {
        return biomeCache[(rawX & 0xF) + 16 * (rawZ & 0xF)];
    }

    /**
     * @param rawX  BLOCK COORD x
     * @param rawZ  BLOCK COORD z
     * @param value biome to cache
     */
    public BiomeBank cacheBiome(int rawX, int rawZ, BiomeBank value) {
        biomeCache[(rawX & 0xF) + 16 * (rawZ & 0xF)] = value;
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
        if (!(obj instanceof ChunkCache chunk)) {
            return false;
        }
        return this.tw == chunk.tw && this.chunkX == chunk.chunkX && this.chunkZ == chunk.chunkZ;
    }

    @Override
    public String toString(){
        return tw.getName() + ":" + chunkX + "," + chunkZ;
    }
}