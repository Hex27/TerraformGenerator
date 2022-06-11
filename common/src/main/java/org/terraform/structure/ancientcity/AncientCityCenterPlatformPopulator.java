package org.terraform.structure.ancientcity;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CylinderBuilder;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.version.OneOneSevenBlockHandler;

public class AncientCityCenterPlatformPopulator extends AncientCityAbstractRoomPopulator {

    public AncientCityCenterPlatformPopulator(HashSet<SimpleLocation> occupied, RoomLayoutGenerator gen, Random rand, boolean forceSpawn, boolean unique) {
        super(occupied, gen, rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	super.populate(data, room);
    	
    	//Generates outer walls
    	for(Entry<Wall,Integer> entry:this.effectiveRoom.getFourWalls(data, 0).entrySet()) {
    		Wall w = entry.getKey();
    		for(int i = 0; i < entry.getValue(); i++) {
    			
				if(i > 2 && i < entry.getValue() - 3 && i % 3 == 0) {
					
					if(!this.gen.isPointInPath(w, shrunkenWidth+1, 2)) {
						w.lsetType(OneOneSevenBlockHandler.CHISELED_DEEPSLATE);
						w.getUp().CorrectMultipleFacing(2);
						w.getUp().LPillar(2, OneOneSevenBlockHandler.DEEPSLATE_BRICK_WALL);
						w.getUp(3).lsetType(OneOneSevenBlockHandler.CHISELED_DEEPSLATE);
						new SlabBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB)
						.setType(Type.TOP).lapply(w.getUp(3).getRear());
						
						w.getRear().getUp(4).lsetType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_WALL);
						w.getRear().getUp(4).CorrectMultipleFacing(1);
					}
					for(BlockFace dir:BlockUtils.getAdjacentFaces(w.getDirection()))
					{
						Wall rel = w.getRelative(dir);
						if(this.gen.isPointInPath(rel, shrunkenWidth+1, 2)) 
							continue;
						
						rel.lsetType(OneOneSevenBlockHandler.DEEPSLATE_BRICKS);
						new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_STAIRS)
						.setFacing(rel.getDirection().getOppositeFace())
						.lapply(rel.getFront())
						.setFacing(dir).lapply(rel.getUp())
						.setFacing(dir.getOppositeFace())
						.lapply(rel.getUp(2))
						.lapply(rel.getUp(4).getRear());
						rel.getUp(3).lsetType(OneOneSevenBlockHandler.DEEPSLATE_BRICKS);
						
						new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_STAIRS)
						.setHalf(Half.TOP)
						.setFacing(rel.getDirection())
						.lapply(rel.getUp(3).getRear())
						.setFacing(rel.getDirection().getOppositeFace())
						.lapply(rel.getUp(3).getFront());
					}
					
				}
    			
    			w = w.getLeft();
    		}
    	}
    	
    	//Spawn corner decorations
        int y = effectiveRoom.getY();
        for (int[] corner:effectiveRoom.getAllCorners(2)) {
        	int x = corner[0];
        	int z = corner[1];
        	spawnLargePillar(new SimpleBlock(data,x,y,z),room);
        }

    	BlockFace facing = BlockUtils.getDirectBlockFace(this.getRand());

    	int modX = 0; int modZ = 0;
    	if(BlockUtils.getAxisFromBlockFace(facing) == Axis.X) {
    		modX = 3;
    		modZ = 17;
    	}
    	else {
    		modZ = 3;
    		modX = 17;
    	}
        //Spawn a rectangular slab of deepslate bricks
        new CylinderBuilder(new Random(), room.getCenterSimpleBlock(data).getUp(13), AncientCityUtils.deepslateBricks)
        .setRX(modX).setRZ(modZ).setRY(20)
        .build();
        
        //Spawn center head
        spawnCentralHead(room.getCenterSimpleBlock(data).getUp(13), facing);
    }
    
    private void spawnCentralHead(SimpleBlock core, BlockFace facing) {
    	
    	int headHeight = 11;
    	int headWidth = 15;
    	int generalFuzzSize = 3;
    	for(int radius = 0; radius <= headWidth; radius++) {
    		for(BlockFace rel:BlockUtils.getAdjacentFaces(facing)) {
    			if(radius % 2 == 0) {
        			core.getRelative(rel,radius).setType(AncientCityUtils.deepslateTiles);
        			core.getUp(headHeight).getRelative(rel,radius).setType(AncientCityUtils.deepslateTiles);

    			}
    			else
    			{
        			core.getRelative(rel,radius).setType(Material.POLISHED_DIORITE,Material.DIORITE);
        			core.getUp(headHeight).getRelative(rel,radius).setType(Material.POLISHED_DIORITE,Material.DIORITE);
    			}

    			//Air the warden's teeth
    			
    			airWardenBlocks(core.getRelative(rel,radius),facing);
    			airWardenBlocks(core.getUp(headHeight).getRelative(rel,radius),facing);

    			airWardenBlocks(core.getUp(1).getRelative(rel,radius), headHeight-1, facing);
    			
    			//Fuzz up and down
    			core.getRelative(rel,radius).getDown().downPillar(GenUtils.randInt(rand, generalFuzzSize, generalFuzzSize+1), AncientCityUtils.deepslateTiles);
    			airWardenBlocks(core.getRelative(rel,radius).getDown(), facing);
    			core.getRelative(rel,radius).getUp(headHeight+1).Pillar(GenUtils.randInt(rand, generalFuzzSize, generalFuzzSize+1), AncientCityUtils.deepslateTiles);
    			
    			if(radius == headWidth) {
    				core.getRelative(rel,radius).Pillar(headHeight, true, new Random(), OneOneSevenBlockHandler.DEEPSLATE_TILES, Material.DIORITE);

    				//Fuzz left and right
    				for(int i = -2; i <= headHeight + 2; i++) {
    					SimpleBlock start = core.getRelative(rel, radius+1).getUp(i);
    					int maxFuzzSize = GenUtils.randInt(rand, generalFuzzSize, generalFuzzSize+1);
    					if(i >= headHeight/2 - 1 && i <= headHeight/2 + 2)
    						maxFuzzSize += 2;
    					for(int fuzzSize = 0; fuzzSize < maxFuzzSize; fuzzSize++)
    					{
    						start.getRelative(rel, fuzzSize).setType(AncientCityUtils.deepslateTiles);
    					}
    				}
    			}
    		}
    	}
    }
    
    private void airWardenBlocks(SimpleBlock b, BlockFace dir) {
    	airWardenBlocks(b, 1, dir);
    }
    
    private void airWardenBlocks(SimpleBlock b, int height, BlockFace dir) {
    	for(int i = 0; i < height; i++)
	    	for(int depth = 1; depth < 3; depth++)
	    	{
	    		b.getUp(i).getRelative(dir,depth).setType(Material.AIR);
	    		b.getUp(i).getRelative(dir,-depth).setType(Material.AIR);
	    	}
    }
    
    //Spawns in the corners of the center large platform
    private void spawnLargePillar(SimpleBlock core, CubeRoom room) {
    		
    	//Hollow square with a pillar in the middle
    	for(int relX = -2; relX <= 2; relX++)
        	for(int relZ = -2; relZ <= 2; relZ++)
        	{
    			SimpleBlock target = core.getRelative(relX,1,relZ);
    			if(Math.abs(relX) == 2 || Math.abs(relZ) == 2 || 
    					(relX == 0 && relZ == 0))
	    			target.RPillar(10, new Random(),
	    					OneOneSevenBlockHandler.DEEPSLATE_BRICKS, 
	    					OneOneSevenBlockHandler.CRACKED_DEEPSLATE_BRICKS, 
	    					OneOneSevenBlockHandler.DEEPSLATE_BRICKS, 
	    					OneOneSevenBlockHandler.CRACKED_DEEPSLATE_BRICKS,
	    					OneOneSevenBlockHandler.DEEPSLATE_BRICKS, 
	    					OneOneSevenBlockHandler.CRACKED_DEEPSLATE_BRICKS,
	    					OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB);
    			else
    				target.Pillar(10, Material.AIR);
        	}
    	
    	//Spiral Stairway upwards
        int bfIndex = 0;

    	if(core.getX() < room.getX() && core.getZ() < room.getZ())
    		bfIndex = 3;
    	else if(core.getX() > room.getX() && core.getZ() < room.getZ())
    		bfIndex = 5;
    	else if(core.getX() < room.getX() && core.getZ() > room.getZ())
    		bfIndex = 1;
    	else if(core.getX() > room.getX() && core.getZ() > room.getZ())
    		bfIndex = 7;
    	
        for (int i = 1; i <= 10; i++) {
            //If index is 1, carve an entrance
        	if(i == 1) {
	        	SimpleBlock target = core.getRelative(BlockUtils.xzPlaneBlockFaces[bfIndex],2);
	        	
	        	for(int relX = -1; relX <= 1; relX++)
	        		for(int relZ = -1; relZ <= 1; relZ++)
	        			target.getRelative(relX,1,relZ).Pillar(3,Material.AIR);
        	}
            //Two slab stairs
            BlockFace face = BlockUtils.xzPlaneBlockFaces[bfIndex];
            Slab bottom = (Slab) Bukkit.createBlockData(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB);
            bottom.setType(Type.BOTTOM);
            core.getRelative(face.getModX(), i, face.getModZ()).setBlockData(bottom);
            
            
            bfIndex = getNextIndex(bfIndex);

            face = BlockUtils.xzPlaneBlockFaces[bfIndex];
            Slab top = (Slab) Bukkit.createBlockData(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB);
            top.setType(Type.TOP);
            core.getRelative(face.getModX(), i, face.getModZ()).setBlockData(top);
            bfIndex = getNextIndex(bfIndex);
        }
    	
    	for(BlockFace face:BlockUtils.directBlockFaces) {
    		Wall target = new Wall(core.getUp(10), face).getFront(2);
    		target.getFront().getUp().setType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_WALL);
    		target.getFront().getDown().getRight().setType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_WALL);
    		target.getFront().getDown().getLeft().setType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_WALL);
    		target.getFront().getRight().setType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB);
    		target.getFront().getLeft().setType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_SLAB);
    		
    		new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_STAIRS)
    		.setFacing(face)
    		.apply(target.getUp().getRight())
    		.apply(target.getUp().getLeft());

    		new StairBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_STAIRS)
    		.setFacing(face)
    		.setHalf(Half.TOP)
    		.apply(target.getFront())
    		.apply(target.getFront().getRight().getDown(2))
    		.apply(target.getFront().getLeft().getDown(2));
    		
    		target.getUp().getRight(2).setType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_WALL);
    		target.getUp().getLeft(2).setType(OneOneSevenBlockHandler.DEEPSLATE_BRICK_WALL);
    		
    	}
    }
    

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}
