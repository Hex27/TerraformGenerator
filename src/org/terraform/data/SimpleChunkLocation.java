package org.terraform.data;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

/**
 * class represent chunk location in simple form
 * @author wysohn
 *
 */
public class SimpleChunkLocation{
	private String world;
	private int x;
	private int z;
	public SimpleChunkLocation(String world, int x, int z) {
		super();
		this.world = world;
		this.x = x;
		this.z = z;
	}
	public SimpleChunkLocation(Chunk chunk){
		this.world = chunk.getWorld().getName();
		this.x = chunk.getX();
		this.z = chunk.getZ();
	}
	public String getWorld() {
		return world;
	}
	public int getX() {
		return x;
	}
	public int getZ() {
		return z;
	}
	public SimpleChunkLocation getRelative(int nx, int nz){
		return new SimpleChunkLocation(world,nx+x,nz+z);
	}
	public SimpleChunkLocation clone(){
		return new SimpleChunkLocation(world, x, z);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleChunkLocation other = (SimpleChunkLocation) obj;
		if (world == null) {
			if (other.world != null)
				return false;
		} else if (!world.equals(other.world))
			return false;
		if (x != other.x)
			return false;
		if (z != other.z)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return world + " , " + x + " , " + z;
	}
	
	public Chunk toChunk(){
		return Bukkit.getWorld(world).getChunkAt(x, z);
	}
	
	public static Chunk toChunk(SimpleChunkLocation loc){
		return Bukkit.getWorld(loc.world).getChunkAt(loc.x, loc.z);
	}
	
	//world , x, z
	public static SimpleChunkLocation chunkStrToLoc(String chunk){
		if(chunk == null) return null;
		
		String[] split = chunk.replaceAll(" ", "").split(",");
		
		String world = split[0];
		int x = Integer.parseInt(split[1]);
		int z = Integer.parseInt(split[2]);
		
		SimpleChunkLocation sc = new SimpleChunkLocation(world, x, z);
		
		return sc;
	}
	
}
