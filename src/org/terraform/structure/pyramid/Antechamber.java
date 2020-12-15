package org.terraform.structure.pyramid;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Stairs;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public abstract class Antechamber extends RoomPopulatorAbstract {

    public Antechamber(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }
    
    /**
     * This parent function will take care of floor and ceiling decorations,
     * along with some basic wall decorations
     */
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	//Wall decorations
    	//Buggy as all hell and also extremely ugly.
    	//Screw wall pillars. Jesus.
//    	int patternIndex = rand.nextInt(3); //0,1,2
//    	for(Entry<Wall,Integer> entry:room.getFourWalls(data, 1).entrySet()) {
//    		Wall w = entry.getKey();
//    		//Don't touch the corners, as the decorations are designed for non-corners
//    		//Step of 2 as we don't want wall decorators to take up the entire space.
//    		for(int i = 0; i < entry.getValue(); i+=2) {
//    			if(i != 0 && i < entry.getValue()-2) {
//	    			if(w.getRear().getType().isSolid())  //Don't block entrances.
//	    				placeWallDecoration(w,room.getHeight(),patternIndex);
//    			}
//    			w = w.getLeft(2);
//    		}
//    	}
    	
    	//Ceiling Corner Decorations
    	int[][] corners = room.getAllCorners(1);
    	for(int[] corner:corners) {
    		Wall w = new Wall(new SimpleBlock(data,corner[0],room.getY()+room.getHeight()-1,corner[1]));
    		w.downLPillar(rand, 2, Material.CUT_SANDSTONE, Material.SMOOTH_SANDSTONE);
    		for(BlockFace face:BlockUtils.directBlockFaces)
    			w.getRelative(face).setType(GenUtils.randMaterial(Material.CUT_SANDSTONE, Material.SMOOTH_SANDSTONE));
    	}
    	
    	//Create randomised patterns
    	int[] choices = new int[] {-2,-1,0,1,2};
    	int[] steps = new int[15];
    	for(int i = 0; i < 15; i++) steps[i] = choices[rand.nextInt(choices.length)];
    	
    	//For the floor
    	SimpleBlock center = new SimpleBlock(data,room.getX(),room.getY(),room.getZ());
    	
    	for(BlockFace face:BlockUtils.directBlockFaces) {
    		int length = room.getWidthX()/2;
    		if(face == BlockFace.NORTH || face == BlockFace.SOUTH)
    			length = room.getWidthZ()/2 ;
    		for(int i = 0; i < length; i++) {
    			if(face == BlockFace.NORTH || face == BlockFace.SOUTH)
    				center.getRelative(face,i).getRelative(steps[i]*face.getModZ(),0,0)
    				.setType(Material.ORANGE_TERRACOTTA);
    			else
    				center.getRelative(face,i).getRelative(0,0,steps[i]*face.getModX())
    				.setType(Material.ORANGE_TERRACOTTA);
    		}
    	}
    	center.setType(Material.BLUE_TERRACOTTA);
    	//For the ceiling
    	center = new SimpleBlock(data,room.getX(),room.getY()+room.getHeight(),room.getZ());
    	for(BlockFace face:BlockUtils.directBlockFaces) {
    		int length = room.getWidthX()/2;
    		if(face == BlockFace.NORTH || face == BlockFace.SOUTH)
    			length = room.getWidthZ()/2 ;
    		for(int i = 0; i < length; i++) {
    			if(face == BlockFace.NORTH || face == BlockFace.SOUTH)
    				center.getRelative(face,i).getRelative(steps[i]*face.getModZ(),0,0)
    				.setType(Material.ORANGE_TERRACOTTA);
    			else
    				center.getRelative(face,i).getRelative(0,0,steps[i]*face.getModX())
    				.setType(Material.ORANGE_TERRACOTTA);
    		}
    	}
    	center.setType(Material.BLUE_TERRACOTTA);
    }
    
    protected void randomRoomPlacement(PopulatorDataAbstract data, CubeRoom room, int lowerbound, int upperbound, Material... types) {
    	
    	for(int i = 0; i < GenUtils.randInt(lowerbound, upperbound); i++) {
    		int[] coords = room.randomCoords(rand,1);
    		BlockData bd = Bukkit.createBlockData(GenUtils.randMaterial(types));
    		if(bd instanceof Waterlogged) 
    			((Waterlogged) bd).setWaterlogged(false);
    		if(!data.getType(coords[0], room.getY()+1, coords[2]).isSolid())
    			data.setBlockData(coords[0], room.getY()+1, coords[2], bd);
    		else
    			data.setBlockData(coords[0], room.getY()+2, coords[2], bd);
    	}
    }
    
    /**
     * To be called on a wall in front of the room's walls.
     * @param w
     * @param roomHeight
     * @param patternIndex
     */
    protected void placeWallDecoration(Wall w, int roomHeight, int patternIndex) {
    	if(patternIndex == 0) { //Simple Chiselled Sandstone and stairs
    		w.LPillar(roomHeight, rand, Material.CHISELED_SANDSTONE);
    		
    		Stairs stair = (Stairs) Bukkit.createBlockData(Material.SANDSTONE_STAIRS);
			stair.setFacing(w.getDirection().getOppositeFace());
			w.getFront().setBlockData(stair);

    		stair = (Stairs) Bukkit.createBlockData(Material.SANDSTONE_STAIRS);
			stair.setHalf(Half.TOP);
    		stair.setFacing(w.getDirection().getOppositeFace());
			w.getFront().getRelative(0,roomHeight-2,0).setBlockData(stair);
    	}else if(patternIndex == 1) { //Stairs inside the pillars. 
    		w.LPillar(roomHeight, rand, Material.CHISELED_SANDSTONE);
    		
    		Stairs stair = (Stairs) Bukkit.createBlockData(Material.SANDSTONE_STAIRS);
			stair.setFacing(w.getDirection().getOppositeFace());
			w.setBlockData(stair);

    		stair = (Stairs) Bukkit.createBlockData(Material.SANDSTONE_STAIRS);
			stair.setHalf(Half.TOP);
    		stair.setFacing(w.getDirection().getOppositeFace());
			w.getRelative(0,roomHeight-2,0).setBlockData(stair);
    	}else if(patternIndex == 2) { //Sandstone wall. 
    		w.LPillar(roomHeight, rand, Material.SANDSTONE_WALL);
    		for(int i = 0; i < roomHeight; i++) {
    			//Make sure the walls are connected properly
    			BlockUtils.correctMultifacingData(w.getRelative(0,i,0).get());
    		}
    	}
    }
    
    
    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}