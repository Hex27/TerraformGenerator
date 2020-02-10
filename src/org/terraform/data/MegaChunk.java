package org.terraform.data;

import java.util.Objects;
import java.util.Random;

import org.terraform.utils.GenUtils;

public class MegaChunk {
	
	/**
	 * Refers to a cluster of 16x16 chunks
	 * Used for spawning structures.
	 */
	
	private int x;
	private int z;
	
	public MegaChunk(SimpleChunkLocation sLoc){
		this(sLoc.getX(),sLoc.getZ());
	}
	
	public MegaChunk(int x, int y, int z){
		this(x>>4,z>>4);
	}
	
	public MegaChunk(int chunkX, int chunkZ){
		this.x = chunkX>>4;
		this.z = chunkZ>>4;
	}
	
	public MegaChunk getRelative(int nx, int nz){
		MegaChunk other = new MegaChunk(0,0);
		other.x = this.x+nx;
		other.z = this.z+nz;
		return other;
	}
	
	/**
	 * 
	 * @param rand
	 * @return A random pair of xz block coords within the mega chunk
	 */
	public int[] getRandomCoords(Random rand){
		int lowChunkX = this.x << 4;
		int lowChunkZ = this.z << 4;
		int highChunkX = (this.x << 4) | 15;
		int highChunkZ = (this.z << 4) | 15;

		int lowX = lowChunkX << 4;
		int lowZ = lowChunkZ << 4;
		int highX = (highChunkX << 4) | 15;
		int highZ = (highChunkZ << 4) | 15;
		
		//Pad the sides. Never generate on the side of a mega chunk.
		int x = GenUtils.randInt(rand,lowX+32,highX-32);
		int z = GenUtils.randInt(rand,lowZ+32,highZ-32);
		return new int[]{x,z};
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof MegaChunk){
			if(((MegaChunk) o).getX() == this.x 
					&& ((MegaChunk) o).getZ() == this.z)
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(323522773,x,z);
		
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
