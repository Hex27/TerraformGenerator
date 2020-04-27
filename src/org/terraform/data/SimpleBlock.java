package org.terraform.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.util.Vector;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataPostGen;

import com.google.gson.annotations.SerializedName;

public class SimpleBlock {

	@SerializedName("w")
	private int x;
	private int y;
	private int z;
	PopulatorDataAbstract popData;	
//	
//	public Location getLocation(){
//		return new Location(Bukkit.getWorld(world),x,y,z);
//	}
	
	public Vector getVector(){
		return new Vector(x,y,z);
	}
	
	public double distanceSquared(SimpleBlock other){
		float selfX = (float) x;
		float selfY = (float) y;
		float selfZ = (float) z;
		float oX = (float) other.getX();
		float oY = (float) other.getY();
		float oZ = (float) other.getZ();
		
		return Math.pow(selfX-oX,2) + Math.pow(selfY-oY,2) + Math.pow(selfZ-oZ,2);
	}
	
	public boolean sameLocation(SimpleBlock other){
		return other.x == x && other.y == y && other.z == z;
	}
	
	public void setType(Material type){
		if(popData.getType(x, y, z) == Material.WATER){
			BlockData data = Bukkit.createBlockData(type);
			if(data instanceof Waterlogged){
				Waterlogged wl = (Waterlogged) data;
				wl.setWaterlogged(true);
				data = wl;
			}
			popData.setBlockData(x,y,z,data);
		}else
			popData.setType(x, y, z, type);
		
		//Setting leaves with setType will be persistent
		if(type.toString().contains("LEAVES")){
			Leaves l = (Leaves) Bukkit.createBlockData(type);
			l.setPersistent(true);
			
			setBlockData(l);
		}
	}
	
	/**
	 * Lenient set. Only replaces non-solid blocks.
	 * @param type
	 * @return if the set was a success.
	 */
	public boolean lsetType(Material type){
		if(!getType().isSolid()){
			setType(type);
			return true;
		}
		return false;
	}
	
	public boolean lsetBlockData(BlockData data){
		if(!getType().isSolid()){
			setBlockData(data);
			return true;
		}
		return false;
	}
	
	public SimpleBlock(Block b){
		this.popData = new PopulatorDataPostGen(b.getChunk());
		this.x = b.getX();
		this.y = b.getY();
		this.z = b.getZ();
	}
	
	public SimpleBlock(PopulatorDataAbstract data, int x, int y, int z){
		//this.world = world;
		this.popData = data;
		this.x = x;
		this.y = y;
		this.z = z;
		
	}
	
	public SimpleBlock(PopulatorDataAbstract data, Location loc){
		//this.world = loc.getWorld().getName();
		this.popData = data;
		this.x = (int) loc.getX();
		this.y = (int) loc.getY();
		this.z = (int) loc.getZ();
		
	}
	
	
	public SimpleBlock(PopulatorDataAbstract data, Block b){
		//this.world = b.getWorld().getName();
		this.popData = data;
		this.x = b.getX();
		this.y = b.getY();
		this.z = b.getZ();		
		//this.data = b.getBlockData().getAsString();
	}
	
	
	public BlockData getBlockData(){
		return popData.getBlockData(x, y, z);//Bukkit.createBlockData(getType());
	}
	
	public void setBlockData(BlockData dat){
		if(popData.getType(x, y, z) == Material.WATER){
			if(dat instanceof Waterlogged){
				Waterlogged wl = (Waterlogged) dat;
				wl.setWaterlogged(true);
				dat = wl;
			}
		}
		popData.setBlockData(x, y, z, dat);
	}
	
	public SimpleBlock getRelative(int nx, int ny, int nz){
		return new SimpleBlock(popData, x+nx, y+ny, z+nz);
	}
	
	public SimpleBlock getRelative(Vector v){
		return new SimpleBlock(popData, 
				(int) Math.round(x+v.getX()),
				(int) Math.round(y+v.getY()), 
				(int) Math.round(z+v.getZ()));
	}
	
	public String getCoords(){
		return x + "," + y + "," + z;
	}
	
	public SimpleBlock getRelative(BlockFace face){
		return new SimpleBlock(popData, x+face.getModX(), y+face.getModY(), z+face.getModZ());
	}
	
	public int getChunkX(){
		return x/16;
	}
	
	public int getChunkZ(){
		return z/16;
	}
	
	public SimpleChunkLocation getSChunk(String world){
		return new SimpleChunkLocation(world,getChunkX(),getChunkZ());
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public Material getType() {
		return popData.getType(x,y,z);
	}

	/**
	 * @return the popData
	 */
	public PopulatorDataAbstract getPopData() {
		return popData;
	}

}
