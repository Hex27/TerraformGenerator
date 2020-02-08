package org.terraform.schematic;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class TerraRegion {
	
	private Location one;
	private Location two;
//	public TerraRegion(Location one, Location two) {
//		super();
//		this.one = one;
//		this.two = two;
//	}
	
	public ArrayList<Block> getBlocks(){
		ArrayList<Block> blocks = new ArrayList<>();
        
        int topBlockX = (one.getBlockX() < two.getBlockX() ? two.getBlockX() : one.getBlockX());
        int bottomBlockX = (one.getBlockX() > two.getBlockX() ? two.getBlockX() : one.getBlockX());
 
        int topBlockY = (one.getBlockY() < two.getBlockY() ? two.getBlockY() : one.getBlockY());
        int bottomBlockY = (one.getBlockY() > two.getBlockY() ? two.getBlockY() : one.getBlockY());
 
        int topBlockZ = (one.getBlockZ() < two.getBlockZ() ? two.getBlockZ() : one.getBlockZ());
        int bottomBlockZ = (one.getBlockZ() > two.getBlockZ() ? two.getBlockZ() : one.getBlockZ());
 
        for(int x = bottomBlockX; x <= topBlockX; x++)
        {
            for(int z = bottomBlockZ; z <= topBlockZ; z++)
            {
                for(int y = bottomBlockY; y <= topBlockY; y++)
                {
                    Block block = one	.getWorld().getBlockAt(x, y, z);
                   
                    blocks.add(block);
                }
            }
        }
       
        return blocks;
	}
	
	public boolean isComplete(){
		return one != null && two != null;
	}
	/**
	 * @return the one
	 */
	public Location getOne() {
		return one;
	}
	/**
	 * @return the two
	 */
	public Location getTwo() {
		return two;
	}
	/**
	 * @param one the one to set
	 */
	public void setOne(Location one) {
		this.one = one;
	}
	/**
	 * @param two the two to set
	 */
	public void setTwo(Location two) {
		this.two = two;
	}
	
	

}
