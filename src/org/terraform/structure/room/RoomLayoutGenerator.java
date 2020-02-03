package org.terraform.structure.room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

public class RoomLayoutGenerator {
	
	private ArrayList<CubeRoom> rooms = new ArrayList<>();
	private int numRooms;
	private int centX;
	private int centY;
	private int centZ;
	private Random rand;
	private int range;
	private int[] upperBound;
	private int[] lowerBound;
	private PathPopulatorAbstract pathPop;
	private ArrayList<RoomPopulatorAbstract> roomPops = new ArrayList<>();
	
	public RoomLayoutGenerator(Random random, int numRooms, int centX, int centY, int centZ, int range){
		this.numRooms = numRooms;
		this.centX = centX;
		this.centY = centY;
		this.centZ = centZ;
		this.rand = random;
		this.range = range;
		this.upperBound = new int[]{centX+range/2,centZ+range/2};
		this.lowerBound = new int[]{centX-range/2,centZ-range/2};
	}
	
	public int[] getCenter(){
		return new int[]{centX,centY,centZ};
	}
	
	public void setPathPopulator(PathPopulatorAbstract pop){
		this.pathPop = pop;
	}
	
	public void registerRoomPopulator(RoomPopulatorAbstract pop){
		this.roomPops.add(pop);
	}
	
	private int roomMaxHeight = 7;
	private int roomMinHeight = 5;
	private int roomMaxX = 15;
	private int roomMinX = 10;
	private int roomMaxZ = 15;
	private int roomMinZ = 10;
	
	public void reset(){rooms.clear();}
	
	public void generate(){ generate(true); }
	
	public void forceAddRoom(int widthX, int widthZ, int heightY){
		CubeRoom room = new CubeRoom(widthX, widthZ,heightY,
				centX+GenUtils.randInt(rand,-range/2,range/2),
				centY, 
				centZ+GenUtils.randInt(rand,-range/2,range/2));
		rooms.add(room);
	}
	
	public void generate(boolean normalise){
		for(int i = 0; i < numRooms; i++){
			int widthX = GenUtils.randInt(rand,roomMinX,roomMaxX);
			int widthZ = GenUtils.randInt(rand,roomMinZ,roomMaxZ);
			
			//Normalise room sizes to prevent strange shapes (Like narrow & tall etc)
			if(normalise){
				if(widthX < widthZ/2) widthX = widthZ + GenUtils.randInt(rand,-2,2);
				if(widthZ < widthX/2) widthZ = widthX + GenUtils.randInt(rand,-2,2);
			}
			int heightY = GenUtils.randInt(rand,roomMinHeight,roomMaxHeight);
			
			if(normalise){
				if(heightY > widthX) heightY = widthX + GenUtils.randInt(rand,-2,2);
				if(heightY < widthX/3) heightY = widthX/3 + GenUtils.randInt(rand,-2,2);
			}
			CubeRoom room = new CubeRoom(widthX, widthZ,heightY,
					centX+GenUtils.randInt(rand,-range/2,range/2),
					centY, 
					centZ+GenUtils.randInt(rand,-range/2,range/2));
			boolean canAdd = true;
			for(CubeRoom other:rooms){
				if(other.isOverlapping(room)){
					canAdd = false;
					break;
				}
			}	
			if(canAdd)
				rooms.add(room);
			
			//Give rooms a "connectedTo" variable list that contains other rooms.
			//These connected rooms will generate paths to each other.
			//Every room must have at least one connection
		}
	}
	
	public boolean anyOverlaps(){
		for(CubeRoom room:rooms){
			for(CubeRoom other:rooms){
				if(other.isClone(room)) continue;
				if(room.isOverlapping(other))
					return true;
			}
		}
		return false;
	}
	
	public void fill(PopulatorDataAbstract data, TerraformWorld tw, Material... mat){
		ArrayList<PathGenerator> pathGens = new ArrayList<>();
		for(CubeRoom room:rooms){
			SimpleBlock base = new SimpleBlock(data, room.getX(),room.getY(),room.getZ());
			PathGenerator gen = new PathGenerator(base,mat,rand,upperBound,lowerBound);
			if(pathPop != null) gen.setPopulator(pathPop);
			while(!gen.isDead()){
				gen.next();
			}
			pathGens.add(gen);
		}
		
		for(CubeRoom room:rooms){
			room.fillRoom(data, mat);
		}
		
		//Populate pathways
		for(PathGenerator pGen:pathGens){
			pGen.populate();
		}
		
		if(roomPops.size() == 0) return;
		
		//Allocate room populators, and populate rooms
		Iterator<RoomPopulatorAbstract> it = roomPops.iterator();
		while(it.hasNext()){
			RoomPopulatorAbstract pops = it.next();
			if(pops.isForceSpawn()){
				for(CubeRoom room:rooms){
					if(room.pop == null && pops.canPopulate(room)){
						//Bukkit.getLogger().info("Set down forced populator of " + pops.getClass().getName());
						room.setRoomPopulator(pops);
						if(pops.isUnique()) it.remove();
						break;
					}
				}
			}
		}

		if(roomPops.size() == 0) return;
		
		for(CubeRoom room:rooms){			
			if(room.pop == null){
				List<RoomPopulatorAbstract> shuffled = (List<RoomPopulatorAbstract>) roomPops.clone();
				Collections.shuffle(shuffled, rand);
				for(RoomPopulatorAbstract roomPop:shuffled){
					if(roomPop.canPopulate(room)){
						room.setRoomPopulator(roomPop);
						if(roomPop.isUnique()){
							roomPops.remove(roomPop);
						}
						
						break;					
					}
				}
			}
			Bukkit.getLogger().info("Registered: " + room.pop.getClass().getName() + " at " + room.getX() + "," + room.getY() + "," + room.getZ());
			room.populate(data);
		}
	}

	/**
	 * @return the numRooms
	 */
	public int getNumRooms() {
		return numRooms;
	}

	/**
	 * @return the range
	 */
	public int getRange() {
		return range;
	}

	/**
	 * @return the roomMaxHeight
	 */
	public int getRoomMaxHeight() {
		return roomMaxHeight;
	}

	/**
	 * @return the roomMinHeight
	 */
	public int getRoomMinHeight() {
		return roomMinHeight;
	}

	/**
	 * @return the rooms
	 */
	public ArrayList<CubeRoom> getRooms() {
		return rooms;
	}

	/**
	 * @return the roomMaxX
	 */
	public int getRoomMaxX() {
		return roomMaxX;
	}

	/**
	 * @return the roomMinX
	 */
	public int getRoomMinX() {
		return roomMinX;
	}

	/**
	 * @return the roomMaxZ
	 */
	public int getRoomMaxZ() {
		return roomMaxZ;
	}

	/**
	 * @return the roomMinZ
	 */
	public int getRoomMinZ() {
		return roomMinZ;
	}

	/**
	 * @param numRooms the numRooms to set
	 */
	public void setNumRooms(int numRooms) {
		this.numRooms = numRooms;
	}

	/**
	 * @param range the range to set
	 */
	public void setRange(int range) {
		this.range = range;
	}

	/**
	 * @param roomMaxHeight the roomMaxHeight to set
	 */
	public void setRoomMaxHeight(int roomMaxHeight) {
		this.roomMaxHeight = roomMaxHeight;
	}

	/**
	 * @param roomMinHeight the roomMinHeight to set
	 */
	public void setRoomMinHeight(int roomMinHeight) {
		this.roomMinHeight = roomMinHeight;
	}

	/**
	 * @param roomMaxX the roomMaxX to set
	 */
	public void setRoomMaxX(int roomMaxX) {
		this.roomMaxX = roomMaxX;
	}

	/**
	 * @param roomMinX the roomMinX to set
	 */
	public void setRoomMinX(int roomMinX) {
		this.roomMinX = roomMinX;
	}

	/**
	 * @param roomMaxZ the roomMaxZ to set
	 */
	public void setRoomMaxZ(int roomMaxZ) {
		this.roomMaxZ = roomMaxZ;
	}

	/**
	 * @param roomMinZ the roomMinZ to set
	 */
	public void setRoomMinZ(int roomMinZ) {
		this.roomMinZ = roomMinZ;
	}

	/**
	 * @return the centX
	 */
	public int getCentX() {
		return centX;
	}

	/**
	 * @return the centY
	 */
	public int getCentY() {
		return centY;
	}

	/**
	 * @return the centZ
	 */
	public int getCentZ() {
		return centZ;
	}

	/**
	 * @param centX the centX to set
	 */
	public void setCentX(int centX) {
		this.centX = centX;
	}

	/**
	 * @param centY the centY to set
	 */
	public void setCentY(int centY) {
		this.centY = centY;
	}

	/**
	 * @param centZ the centZ to set
	 */
	public void setCentZ(int centZ) {
		this.centZ = centZ;
	}

	/**
	 * @return the rand
	 */
	public Random getRand() {
		return rand;
	}

	/**
	 * @return the pathPop
	 */
	public PathPopulatorAbstract getPathPop() {
		return pathPop;
	}

	/**
	 * @param rand the rand to set
	 */
	public void setRand(Random rand) {
		this.rand = rand;
	}

	/**
	 * @param pathPop the pathPop to set
	 */
	public void setPathPop(PathPopulatorAbstract pathPop) {
		this.pathPop = pathPop;
	}
	

}
