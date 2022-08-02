package org.terraform.structure.ancientcity;

import java.util.Random;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.version.OneOneSevenBlockHandler;

public class AncientCityResearchBasementHandler {

	public static void populate(PopulatorDataAbstract data, CubeRoom room, BlockFace headFacing) {
		
		//Clear out the room and place floor
		int[] lowerCorner = room.getLowerCorner();
		int[] upperCorner = room.getUpperCorner();
		for(int x = lowerCorner[0]; x <= upperCorner[0]; x++)
			for(int z = lowerCorner[1]; z <= upperCorner[1]; z++)
				for(int y = room.getY(); y < room.getY() + room.getHeight(); y++)
				{
					if(y == room.getY() || y == room.getY() + room.getHeight()-1)
						data.setType(x, y, z, AncientCityUtils.deepslateBricks);
					else
						data.setType(x, y, z, Material.AIR);
				}
		
		//Place walls
		for(Entry<Wall, Integer> entry:room.getFourWalls(data, 0).entrySet())
		{
			Wall w = entry.getKey().getLeft(3);
			for(int i = 3; i < entry.getValue()-3; i+=3)
			{
				w.getUp(4).setType(OneOneSevenBlockHandler.CHISELED_DEEPSLATE);
				w.setType(OneOneSevenBlockHandler.CHISELED_DEEPSLATE);
				new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_TILE_STAIRS)
				.setFacing(w.getDirection())
				.apply(w.getUp(3))
				.setHalf(Half.TOP)
				.apply(w.getUp());
				new OrientableBuilder(Material.POLISHED_BASALT)
				.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
				.apply(w.getUp(2))
				.apply(w.getUp(2).getFront());
				
				//Back of wall
				w.getUp().getFront().setType(OneOneSevenBlockHandler.CHISELED_DEEPSLATE);
				w.getUp(3).getFront().setType(OneOneSevenBlockHandler.CHISELED_DEEPSLATE);
				w.getFront().setType(OneOneSevenBlockHandler.DEEPSLATE_TILES);
				w.getUp(4).getFront().setType(OneOneSevenBlockHandler.DEEPSLATE_TILES);
				
				
				//Left and right sides
				for(BlockFace face:BlockUtils.getAdjacentFaces(w.getDirection()))
				{
					Wall temp = w.getRelative(face);
					
					temp.getUp(2).setType(Material.POLISHED_BASALT);
					
					new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_TILE_STAIRS)
					.setFacing(face.getOppositeFace())
					.apply(temp)
					.setHalf(Half.TOP)
					.apply(temp.getUp(4));
					
					new StairBuilder(OneOneSevenBlockHandler.POLISHED_DEEPSLATE_STAIRS)
					.setFacing(face.getOppositeFace())
					.apply(temp.getUp(3))
					.setHalf(Half.TOP)
					.apply(temp.getUp());
					
					//rear
					w.getFront().getRelative(face).Pillar(2, Material.POLISHED_BASALT);
					w.getFront().getRelative(face).getUp(2).setType(OneOneSevenBlockHandler.DEEPSLATE_TILES);
					w.getFront().getRelative(face).getUp(3).Pillar(2, Material.POLISHED_BASALT);
				}
				
				w = w.getLeft(3);
			}
		}
		
		//Place 4 solid cubes of deepslate at the corners
		//Decorate the sides
		for(int[] corner:room.getAllCorners(2))
		{
			
			SimpleBlock core = new SimpleBlock(data,corner[0],room.getY(),corner[1]);
			
	    	//Solid rectangle
	    	for(int relX = -2; relX <= 2; relX++)
	        	for(int relZ = -2; relZ <= 2; relZ++)
	        	{
	    			SimpleBlock target = core.getRelative(relX,1,relZ);
	    			target.RPillar(4, new Random(),
	    					OneOneSevenBlockHandler.DEEPSLATE_BRICKS, 
	    					OneOneSevenBlockHandler.CRACKED_DEEPSLATE_BRICKS, 
	    					OneOneSevenBlockHandler.DEEPSLATE_BRICKS, 
	    					OneOneSevenBlockHandler.CRACKED_DEEPSLATE_BRICKS,
	    					OneOneSevenBlockHandler.DEEPSLATE_BRICKS, 
	    					OneOneSevenBlockHandler.CRACKED_DEEPSLATE_BRICKS,
	    					OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB);
	        	}
	    	
	    	//Stairs at the top and bottom
	    	for(BlockFace side:BlockUtils.directBlockFaces)
	    	{
	    		Wall w = new Wall(core,side).getFront(3).getUp();
    			if(!w.getDown().isSolid() || w.isSolid()) continue;
    			//w.setType(Material.RED_WOOL);
	    		for(BlockFace adj:BlockUtils.getAdjacentFaces(side))
	    		{
	    			if(w.getRelative(adj).isSolid()) continue;
	    			new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_STAIRS)
	    			.setFacing(w.getDirection().getOppositeFace())
	    			.apply(w)
	    			.apply(w.getRelative(adj,1))
	    			.apply(w.getRelative(adj,2))
	    			.apply(w.getRelative(adj,3))
	    			.setHalf(Half.TOP)
	    			.apply(w.getUp(3))
	    			.apply(w.getRelative(adj,1).getUp(3))
	    			.apply(w.getRelative(adj,2).getUp(3))
	    			.apply(w.getRelative(adj,3).getUp(3));
	    			
	    			//Make these corners
	    			BlockUtils.correctStairData(w.getRelative(adj,3));
	    			BlockUtils.correctStairData(w.getRelative(adj,3).getUp(3));
	    			w.getRelative(adj,2).getRear().Pillar(4, Material.POLISHED_BASALT);
	    		}
	    	}
	    	
		}
	}
}
