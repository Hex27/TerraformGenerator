package org.terraform.structure.ancientcity;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class AncientCityPathPopulator extends PathPopulatorAbstract {
	
	//private int state = 0;
    private final Random rand;
    private final RoomLayoutGenerator gen;
    private HashSet<SimpleLocation> occupied;
    public AncientCityPathPopulator(Random rand, RoomLayoutGenerator gen, HashSet<SimpleLocation> occupied) {
        this.rand = rand;
        this.gen = gen;
        this.occupied = occupied;
    }

    @Override
    public void populate(PathPopulatorData ppd) {
        Wall core = new Wall(ppd.base, ppd.dir);
        
        //Do not populate in rooms.
        if(gen.isInRoom(new int[] {ppd.base.getX(), ppd.base.getZ()}))
        	return;
        
        if(ppd.isTurn) { //Turn area
        	for(int nx = -1; nx <= 1; nx++)
        		for(int nz = -1; nz <= 1; nz++) {
        			core.getRelative(nx,0,nz).setType(Material.GRAY_WOOL);
        		}
        	AncientCityUtils.placeSupportPillar(core.getDown());
    		
        	for(BlockFace face:BlockUtils.directBlockFaces)
        	{
        		core.getRelative(face,2).lsetType(Material.REDSTONE_BLOCK);
        		for(BlockFace rel:BlockUtils.getAdjacentFaces(face))
        			core.getRelative(rel).getRelative(face,2).lsetType(Material.REDSTONE_BLOCK);
        	}
        }
        else if (!ppd.isEnd){ //Straight path
        	//main path
        	core.setType(Material.GRAY_WOOL);
        	core.getLeft().setType(Material.GRAY_WOOL);
        	core.getRight().setType(Material.GRAY_WOOL);
        	core.getUp().Pillar(3, Material.AIR);
        	
        	new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_STAIRS)
        	.setHalf(Half.TOP)
        	.setFacing(BlockUtils.getLeft(ppd.dir))
        	.lapply(core.getRight(2))
        	.setFacing(BlockUtils.getRight(ppd.dir))
        	.lapply(core.getLeft(2));
        	
        	int state;
        	if(ppd.dir == BlockFace.NORTH)
        		state = (-1*core.getZ()) % 9;
        	else if(ppd.dir == BlockFace.SOUTH)
        		state = core.getZ() % 9;
        	else if(ppd.dir == BlockFace.EAST)
        		state = core.getX() % 9;
        	else //if(ppd.dir == BlockFace.WEST)
        		state = (-1*core.getX()) % 9;
        	
        	if(state < 0) state += 9;
        	
            if(!ppd.isOverlapped) {
            	placeWallArc(core,state);
            }
            //state++;
        }
        else { //End
        	AncientCityPathMiniRoomPlacer.placeAltar(core, rand);
        }
    }
    
    private void placeWallArc(Wall core, int state) {
    	if(occupied.contains(core.getLoc())) 
    		return;
    	
		occupied.add(core.getLoc());
		
		BlockFace pathFacing = core.getDirection();
		if(state > 4) pathFacing = core.getDirection().getOppositeFace();
		
		//0 is center
		//1, 8 is the segment closest to center
		//2, 7 is next closest
		//3, 6 is the next
		//4, 5 is the sides (highest point in the arc)
    	switch(state) {
    	case 0:
    		//Debug arrow
//    		core.getUp().setType(Material.RED_WOOL);
//    		core.getUp().getRear().getLeft().setType(Material.RED_WOOL);
//    		core.getUp().getRear().getRight().setType(Material.RED_WOOL);
        	//Middle of the arc.
    		core.getUp(6).lsetType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_WALL);
    		core.getUp(5).lsetType(OneOneSevenBlockHandler.DEEPSLATE_BRICKS);
			Lantern lantern = (Lantern) Bukkit.createBlockData(Material.SOUL_LANTERN);
			lantern.setHanging(true);
    		core.getUp(4).lsetBlockData(lantern);
			
    		//Support pillar
    		AncientCityUtils.placeSupportPillar(core.getDown());
    		
    		//Mirror the sides to create the arc in the middle
    		for(BlockFace leftRight:BlockUtils.getAdjacentFaces(core.getDirection())) {
    			
    			new StairBuilder(OneOneSevenBlockHandler.POLISHED_DEEPSLATE_STAIRS)
    			.setHalf(Half.TOP).setFacing(leftRight.getOppositeFace())
    			.apply(core.getRelative(leftRight,2).getDown());
    			
    			core.getRelative(leftRight,2).setType(OneOneSevenBlockHandler.CHISELED_DEEPSLATE);
    			core.getRelative(leftRight,2).getUp().lsetType(OneOneSevenBlockHandler.DEEPSLATE_TILES);
    			core.getRelative(leftRight,2).getUp(2).LPillar(2, OneOneSevenBlockHandler.POLISHED_DEEPSLATE);
    			core.getRelative(leftRight,2).getUp(4).lsetType(OneOneSevenBlockHandler.CHISELED_DEEPSLATE);
    			
    			new StairBuilder(OneOneSevenBlockHandler.POLISHED_DEEPSLATE_STAIRS)
    			.setFacing(leftRight.getOppositeFace())
    			.lapply(core.getUp(5).getRelative(leftRight,2));

    			new StairBuilder(OneOneSevenBlockHandler.POLISHED_DEEPSLATE_STAIRS)
    			.setFacing(leftRight)
    			.setHalf(Half.TOP)
    			.lapply(core.getUp(4).getRelative(leftRight));
    			
    			core.getRelative(leftRight).getUp(5).lsetType(OneOneSevenBlockHandler.DEEPSLATE_BRICKS);
    			
    		}
        	break;
    	case 1, 8:
			
			//Upper stair
			new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_TILE_STAIRS)
			.setHalf(Half.TOP)
			.setFacing(pathFacing.getOppositeFace())
			.lapply(core.getUp(5));
    	
    		for(BlockFace leftRight:BlockUtils.getAdjacentFaces(core.getDirection())) {
        		//Slab
    			new SlabBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB)
    			.setType(Type.TOP)
    			.lapply(core.getRelative(leftRight,2).getDown());
    			
//    			if(pathFacing == core.getDirection())
//    				core.getUp().setType(Material.BLUE_WOOL);
//    			else
//    				core.getUp().setType(Material.GREEN_WOOL);
    			
    			//Stairs that complete the arc
    			new StairBuilder(OneOneSevenBlockHandler.POLISHED_DEEPSLATE_STAIRS)
    			.setFacing(pathFacing.getOppositeFace())
    			.lapply(core.getRelative(leftRight,2).getUp());
    			new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_TILE_STAIRS)
    			.setHalf(Half.TOP)
    			.setFacing(pathFacing.getOppositeFace())
    			.lapply(core.getRelative(leftRight,2).getUp(4));
    			
    			core.getRelative(leftRight,2).getUp(5).lsetType(OneOneSevenBlockHandler.DEEPSLATE_TILES);
    			core.getRelative(leftRight).getUp(5).lsetType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_WALL);
    			core.getRelative(leftRight).getUp(6).lsetType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB);
    			
    		}
    		break;
    	case 2, 7:
    		for(BlockFace leftRight:BlockUtils.getAdjacentFaces(core.getDirection())) {
        		//Slab
    			new SlabBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB)
    			.setType(Type.TOP)
    			.lapply(core.getRelative(leftRight,2).getDown())
    			.lapply(core.getRelative(leftRight).getUp(5));
    			
    			core.getRelative(leftRight,2).getUp(5).lsetType(OneOneSevenBlockHandler.DEEPSLATE_TILES);
    			core.getRelative(leftRight,2).getUp(6).lsetType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB);
    			
    		}
    		break;
    	case 3, 6:
    		//Center slab
    		new SlabBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB)
			.setType(Type.TOP)
			.lapply(core.getUp(5));
    	
	    	for(BlockFace leftRight:BlockUtils.getAdjacentFaces(core.getDirection())) {
	    		//Slab
				new SlabBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB)
				.setType(Type.TOP)
				.lapply(core.getRelative(leftRight, 2).getUp(5));
				
				new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_STAIRS)
				.setFacing(pathFacing.getOppositeFace())
				.lapply(core.getRelative(leftRight, 2).getUp(6));
			}
    		break;
    	case 4, 5:
    		//Center slab
    		new SlabBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB)
			.setType(Type.TOP)
			.lapply(core.getUp(5));
    	
	    	for(BlockFace leftRight:BlockUtils.getAdjacentFaces(core.getDirection())) {
	    		//Slab
	    		new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_STAIRS)
				.setFacing(leftRight)
				.setHalf(Half.TOP)
				.lapply(core.getRelative(leftRight, 2).getUp(5));
				
				new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_STAIRS)
				.setFacing(pathFacing.getOppositeFace())
				.lapply(core.getRelative(leftRight, 2).getUp(6));
			}
    		break;
		default:
			TerraformGeneratorPlugin.logger.info("Ancient City Populator: Irregular path state: " + state);
			break;
    	}
    }

    @Override
    public boolean customCarve(SimpleBlock base, BlockFace dir, int pathWidth) {
        Wall core = new Wall(base.getRelative(0, 1, 0), dir);
        int seed = 55 + core.getX() + core.getY() ^ 2 + core.getZ() ^ 3;
        BlockUtils.carveCaveAir(seed,
                pathWidth, pathWidth + 1, pathWidth, core.get(), false, 
                BlockUtils.badlandsStoneLike);

        return true;
    }

    @Override
    public int getPathWidth() {
        return 3;
    }
}
