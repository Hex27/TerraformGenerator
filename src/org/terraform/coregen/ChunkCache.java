package org.terraform.coregen;

import org.bukkit.Axis;
import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;

public class ChunkCache {
    public final TerraformWorld tw;

    public final int chunkX;
    public final int chunkZ;

    double[][] heightCache = new double[16][16];
    BiomeBank[][] biomeCache = new BiomeBank[16][16];

    public ChunkCache(TerraformWorld tw, int chunkX, int chunkZ) {
        this.tw = tw;

        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
    
    /**
     * Y is not used, but will force the dev to remember that these are block coords.
     * @param tw
     * @param rawX
     * @param rawY
     * @param rawZ
     */
    public ChunkCache(TerraformWorld tw, int rawX, int rawY, int rawZ) {
        this.tw = tw;

        this.chunkX = getChunkCoordinate(rawX);
        this.chunkZ = getChunkCoordinate(rawZ);
    }

    public static int getChunkCoordinate(int coordinate) {
        return coordinate >> 4;
    }

    // Coordinates from 0 to 15
    private int getCoordinateInsideChunk(int j, Axis ax) {
    	int res;
    	if(ax == Axis.X) {
    		res=  j-this.chunkX*16;
    	}else
    		res= j-this.chunkZ*16;
    	return res;
    }

    public double getHeight(int rawX, int rawZ) {
        return heightCache[getCoordinateInsideChunk(rawZ,Axis.Z)][getCoordinateInsideChunk(rawX,Axis.X)];
    }

    public void cacheHeight(int rawX, int rawZ, double value) {
        heightCache[getCoordinateInsideChunk(rawZ,Axis.Z)][getCoordinateInsideChunk(rawX,Axis.X)] = value;
    }

    public BiomeBank getBiome(int rawX, int rawZ) {
        return biomeCache[getCoordinateInsideChunk(rawZ,Axis.Z)][getCoordinateInsideChunk(rawX,Axis.X)];
    }

    public BiomeBank cacheBiome(int rawX, int rawZ, BiomeBank value) {
        biomeCache[getCoordinateInsideChunk(rawZ,Axis.Z)][getCoordinateInsideChunk(rawX,Axis.X)] = value;
        return value;
    }
    
    public boolean areCoordsInside(int rawX, int rawZ) {
    	return chunkX == rawX >> 4 && chunkZ == rawZ >> 4;
    }
    
    private static final int hashPrime = 81349;//Long.parseLong("6770384441");
    public static int calculateHash(int chunkX, int chunkZ, TerraformWorld tw) {
    	return tw.hashCode() ^ (chunkX + chunkZ*hashPrime);
    }
    
    public int getHash() {
    	return calculateHash(chunkX, chunkZ, tw);
    }
    
    @Override
    public int hashCode() {
    	return calculateHash(chunkX, chunkZ, tw);
    }
    
    @Override
    public boolean equals(Object other) {
    	if(!(other instanceof ChunkCache))
    		return false;
    	
    	ChunkCache o = (ChunkCache) other;
    	if(o.chunkX != chunkX) return false;
    	if(o.chunkZ != chunkZ) return false;
    	if(tw.hashCode() != o.tw.hashCode()) return false;
    	return true;
    }
}