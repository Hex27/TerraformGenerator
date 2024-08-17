package org.terraform.structure.pillager.mansion.secondfloor;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.blockdata.TrapdoorBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class MansionSecondFloorWallPiece extends JigsawStructurePiece {

	public boolean isTentRoofFace = false;
	private MansionJigsawBuilder builder;
    public MansionSecondFloorWallPiece(MansionJigsawBuilder builder, int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
        this.builder = builder;

    }
    
    public void buildIndividualRoofs(Random random, @NotNull PopulatorDataAbstract data, int[] lowerBound, int[] upperBound) {
    	
    	//Don't build roofs for pieces that are in sink-ins
    	if(this.builder.countOverlappingPiecesAtLocation(
    			this.getRoom().getSimpleLocation().getRelative(
    					0,-MansionJigsawBuilder.roomHeight-1,0
    					)) >= 3) {
    		return;	
    	}
    	
    	BlockFace walledFace = this.getRotation();
    	Wall w = new Wall(
				new SimpleBlock(data,this.getRoom().getSimpleLocation())
				.getRelative(walledFace.getOppositeFace(),3)
				.getRelative(0,7,0), 
				walledFace);
    	
    	int maxDepth = -1;
    	BlockFace roofOuterFace = null;
    	//Figure out where the roof's position is, so as to determine which direction
    	//the individual roof should face.
    	if(lowerBound[0] <= w.getX() 
    			&& lowerBound[1] <= w.getZ()
    			&& upperBound[0] >= w.getX() 
    			&& upperBound[1] >= w.getZ()) {
    		//Piece is inside the roof. Don't do anything.
    		builder.getRoofedLocations().add(this.getRoom().getSimpleLocation().getRelative(this.getRotation().getOppositeFace(), MansionJigsawBuilder.groundFloorRoomWidth));
        	return;
    	}
    	else if(w.getX() >= lowerBound[0] && w.getX() <= upperBound[0]) 
    	{
    		//Z Axis aligned.
    		if(w.getZ() > upperBound[1]) {
    			roofOuterFace = BlockFace.SOUTH;
    			maxDepth = w.getZ() - upperBound[1];
    		}else {
    			roofOuterFace = BlockFace.NORTH;
    			maxDepth = lowerBound[1] - w.getZ();
    		}
    	}
    	else if(w.getZ() >= lowerBound[1] && w.getZ() <= upperBound[1]) 
    	{
    		//X Axis aligned.
    		if(w.getX() > upperBound[0]) {
    			roofOuterFace = BlockFace.EAST;
    			maxDepth = w.getX() - upperBound[0];
    		}else {
    			roofOuterFace = BlockFace.WEST;
    			maxDepth = lowerBound[0] - w.getX();
    		}
    	}
    	
    	//Increase it to force it to properly sink into the main roof
    	maxDepth = (maxDepth*2);
    	if(maxDepth < 6) maxDepth*=2;
    	//Only place the roof if the walled face is correct. If not, ignore it.
    	if(roofOuterFace == null || walledFace != roofOuterFace)
    	{
    		return;
    	}
    	isTentRoofFace = true;
    	builder.getRoofedLocations().add(this.getRoom().getSimpleLocation().getRelative(this.getRotation().getOppositeFace(), MansionJigsawBuilder.groundFloorRoomWidth));
    	//Place the actual roof
		for(BlockFace side:BlockUtils.getAdjacentFaces(walledFace)) {
			
			for(int i = 0; i < 6; i++) {
				int position = 1+5-i;
				Wall roofPiece = w.getRelative(0,position,0).getRelative(side, i);
				
				if(i==0) {
					roofPiece.setType(Material.COBBLESTONE_SLAB);
					new OrientableBuilder(Material.DARK_OAK_LOG)
					.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
					.lapply(roofPiece.getRelative(0,-1,0));
				}else {
					StairBuilder builder = new StairBuilder(Material.COBBLESTONE_STAIRS)
					.setFacing(side.getOppositeFace())
					.lapply(roofPiece);
					
					if(BlockUtils.isAir(roofPiece.getRelative(0,-1,0).getType())
							|| roofPiece.getRelative(0,-1,0).getType() == Material.COBBLESTONE_STAIRS)
						builder
						.setFacing(side)
						.setHalf(Half.TOP)
						.apply(roofPiece.getRelative(0,-1,0));
					
					if(i == 5 
							&& (roofPiece.getRelative(0,-1,0).getRelative(side).getType() == Material.COBBLESTONE_STAIRS
							|| roofPiece.getRelative(0,-1,0).getRelative(side.getOppositeFace()).getType() == Material.COBBLESTONE_STAIRS))
					{
						roofPiece.getRelative(0,-1,0).setType(Material.AIR);
						roofPiece.getRelative(0,-1,0).getRelative(side).setType(Material.AIR);
						roofPiece.getRelative(0,-1,0).getRelative(side.getOppositeFace()).setType(Material.AIR);
					}
					
				}
				for(int depth = 1; depth < maxDepth; depth++) {
					if(i == 0) {
						roofPiece.getRear(depth).setType(Material.COBBLESTONE_SLAB);
						new OrientableBuilder(Material.DARK_OAK_LOG)
						.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
						.lapply(roofPiece.getRear(depth).getRelative(0,-1,0));
						
					}else// if(i != 5)
						new StairBuilder(Material.DARK_OAK_STAIRS)
						.setFacing(side.getOppositeFace())
						.lapply(roofPiece.getRear(depth));
				}
			}
		}
    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, @NotNull Random rand) {

        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getRelative(0, -1, 0);
        
        for (int i = 0; i < entry.getValue(); i++) {
            
        	//Primary Wall and ground beneath wall
        	//w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.getRelative(0, 1, 0).Pillar(this.getRoom().getHeight(), rand, Material.DARK_OAK_PLANKS);
            
            w = w.getLeft();
        }
    }
    
    /**
     * Extra decorations like windows or walls, depending on the surrounding walls
     */
    @Override
    public void postBuildDecoration(@NotNull Random rand, @NotNull PopulatorDataAbstract data) {
    	
        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
    	MansionSecondFloorWallType type = switch(rand.nextInt(3)) {
            case 0 -> MansionSecondFloorWallType.LARGE_WINDOW;
            case 1 -> MansionSecondFloorWallType.THIN_WINDOWS;
            case 2 -> MansionSecondFloorWallType.BALCONY;
            default -> null;
        };

        //Don't allow balconies in sink in areas
    	if(type == MansionSecondFloorWallType.BALCONY) {
        	int overlappers = 0;
	    	for(JigsawStructurePiece otherPiece:builder.getOverlapperPieces()) {
	    		int[] center = otherPiece.getRoom().getCenter();
	    		//Only check X and Z, as they're identical to bottom floor
	    		if(center[0] == this.getRoom().getCenter()[0]
						&& center[2] == this.getRoom().getCenter()[2])
	    			overlappers++;
	    	}
	    	if(overlappers > 1)
	    		type = MansionSecondFloorWallType.LARGE_WINDOW;
    	}
    	
    	Wall w = entry.getKey().getRelative(0, -1, 0);
        
        for (int i = 0; i < entry.getValue(); i++) {
            
        	switch(type) {
			case LARGE_WINDOW:
				if(i == 1 || i == entry.getValue()-2) //Side decoration
					w.getRelative(0,1,0).Pillar(this.getRoom().getHeight(),new Random(),Material.DARK_OAK_LOG);
				
				if(i == 3 || i == 4 || i == 5)
				{ //Window Panes and decorations at the base
					w.getRelative(0,2,0).Pillar(4, new Random(), Material.LIGHT_GRAY_STAINED_GLASS_PANE);
					w.getRelative(0,2,0).CorrectMultipleFacing(4);
					
					new OrientableBuilder(Material.DARK_OAK_LOG)
					.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
					.apply(w.getRelative(0,1,0).getFront());
					new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR)
					.setFacing(w.getDirection())
					.setOpen(true)
					.apply(w.getRelative(0,1,0).getFront(2));
					new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR)
					.setFacing(w.getDirection())
					.apply(w.getRelative(0,1,0).getFront().getRelative(0,1,0));
				}
				
				if(i == 2 || i == entry.getValue()-3)
				{//Supporting Pillars
					w.getFront().getRelative(0,1,0).setType(Material.COBBLESTONE);
					w.getFront().getRelative(0,2,0).Pillar(3, new Random(), Material.STONE_BRICK_WALL);
					w.getFront().getRelative(0,2,0).CorrectMultipleFacing(3);
					new OrientableBuilder(Material.DARK_OAK_LOG)
					.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
					.apply(w.getFront().getRelative(0,5,0));
				}
				
				if(i == 4) {
					//Place Main window decoration
					spawnWindowOverhang(w.getFront().getRelative(0,6,0));
				}
					
				break;
			case BALCONY:
				if(i == 4) { //center
					w.getRelative(0,1,0).getLeft().Pillar(3, Material.AIR);
					w.getRelative(0,1,0).getRight().Pillar(3, Material.AIR);
					w.getRelative(0,1,0).Pillar(4, Material.AIR);
					
					new StairBuilder(Material.DARK_OAK_STAIRS)
					.setHalf(Half.TOP)
					.setFacing(w.getDirection().getOppositeFace())
					.apply(w.getRelative(0,5,0))
					.setFacing(BlockUtils.getLeft(w.getDirection()))
					.apply(w.getLeft().getRelative(0,4,0))
					.setFacing(BlockUtils.getRight(w.getDirection()))
					.apply(w.getRight().getRelative(0,4,0));
					
					w.getRelative(0,6,0).getFront().setType(Material.COBBLESTONE_SLAB);
					new SlabBuilder(Material.COBBLESTONE_SLAB)
					.setType(Type.TOP)
					.apply(w.getRelative(0,5,0).getFront().getLeft())
					.apply(w.getRelative(0,5,0).getFront().getRight());
					
					new OrientableBuilder(Material.DARK_OAK_LOG)
					.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
					.apply(w.getRelative(0,6,0))
					.apply(w.getRight().getRelative(0,5,0))
					.apply(w.getLeft().getRelative(0,5,0));
					
					w.getRelative(0,1,0).getRight(2).getFront().setType(Material.POLISHED_ANDESITE);
					w.getRelative(0,2,0).getRight(2).getFront().Pillar(2, Material.STONE_BRICK_WALL);
					w.getRelative(0,2,0).getRight(2).getFront().CorrectMultipleFacing(2);
					
					w.getRelative(0,1,0).getLeft(2).getFront().setType(Material.POLISHED_ANDESITE);
					w.getRelative(0,2,0).getLeft(2).getFront().Pillar(2, Material.STONE_BRICK_WALL);
					w.getRelative(0,2,0).getLeft(2).getFront().CorrectMultipleFacing(2);
					
					
					new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
					.setFacing(w.getDirection().getOppositeFace())
					.apply(w.getRelative(0,1,0).getRight(2).getFront(2))
					.apply(w.getRight(2).getFront().getRelative(0,4,0))
					.apply(w.getRelative(0,1,0).getLeft(2).getFront(2))
					.apply(w.getLeft(2).getFront().getRelative(0,4,0))
					.setFacing(BlockUtils.getLeft(w.getDirection()))
					.apply(w.getRelative(0,1,0).getRight(3).getFront())
					.setFacing(BlockUtils.getRight(w.getDirection()))
					.apply(w.getRelative(0,1,0).getLeft(3).getFront())
					;
				}
				else if (i == 1 || i == entry.getValue() - 2) 
				{ //Side lamps and oak log
					w.getRelative(0,1,0).Pillar(this.getRoom().getHeight(), Material.DARK_OAK_LOG);
					new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
					.setHalf(Half.TOP)
					.setFacing(w.getDirection().getOppositeFace())
					.apply(w.getFront().getRelative(0,5,0));
					w.getFront().getRelative(0,6,0).setType(Material.LANTERN);
				}
				break;
			case THIN_WINDOWS:
				if(i == 2 || i == entry.getValue()-3) //Side decoration
				{
					w.getRelative(0,1,0).Pillar(this.getRoom().getHeight(),new Random(),Material.DARK_OAK_LOG);
					
					new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
					.setHalf(Half.TOP)
					.setFacing(w.getDirection().getOppositeFace())
					.apply(w.getRelative(0,2,0).getFront());
					
					
					w.getRelative(0,3,0).getFront().setType(Material.STONE_BRICK_WALL);
					w.getRelative(0,4,0).getFront().setType(Material.STONE_BRICK_WALL);
					w.getRelative(0,5,0).getFront().setType(Material.COBBLESTONE_SLAB);
				}
				if(i%2 == 1) {
	            	w.getRelative(0,2,0).Pillar(4, new Random(), Material.LIGHT_GRAY_STAINED_GLASS_PANE);
	            	w.getRelative(0,2,0).CorrectMultipleFacing(4);
	            }

	            if(i == (entry.getValue()/2)) {
	            	spawnWallSupportingPillar(w.getFront().getRelative(0,1,0), this.getRoom().getHeight());
	            }
				break;
        	}
        	
            w = w.getLeft();
        }
        
    	//Raise walls to meet roof.
        entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        w = entry.getKey().getRelative(0, -1, 0);
        
        for (int i = 0; i < entry.getValue(); i++) {
            
        	Wall target = w.getRelative(0, this.getRoom().getHeight(), 0);
         
        	if(target.getRelative(0,1,0).getType() == Material.DARK_OAK_LOG)
        		target.getRelative(0,1,0).setType(Material.AIR);

        	//Link the wall to the roof above
        	if(target.findCeiling(10) != null)
        	{
        		int spawnedHeight = target.getRelative(0,1,0).LPillar(10, new Random(), target.getType());
            	
            	if(!isTentRoofFace)
            	//Spawn slightly overhanging roof
    	        	if(spawnedHeight == 0 && target.getRelative(0,1,0).getFront().getType().isAir() &&  Tag.STAIRS.isTagged(target.getRelative(0,1,0).getType())) {
    	        		StairBuilder builder = new StairBuilder(Material.COBBLESTONE_STAIRS)
    	        		.setFacing(target.getDirection().getOppositeFace())
    	        		.lapply(target.getFront());
    	        		
    	        		for(int depth = 1; depth <= 2; depth++) {
    	        			if(Tag.STAIRS.isTagged(target.getLeft(depth).getRelative(0,1,0).getType())
    	        					 && target.getLeft(depth).getRelative(0,1,0).getFront().getType().isAir())
    	        				builder.lapply(target.getLeft(depth).getFront());
    	        			if(Tag.STAIRS.isTagged(target.getRight(depth).getRelative(0,1,0).getType())
    	       					 && target.getRight(depth).getRelative(0,1,0).getFront().getType().isAir())
    	        				builder.lapply(target.getRight(depth).getFront());
    	        		}
            			
            	}
        	}
        	
            w = w.getLeft();
        }
        
    }
    
    private void spawnWallSupportingPillar(@NotNull Wall w, int height) {
    	w.Pillar(height, new Random(), Material.POLISHED_ANDESITE);
    	
    	new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
    	.setFacing(w.getDirection().getOppositeFace())
    	.apply(w.getFront());
    	
    	new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
    	.setFacing(BlockUtils.getRight(w.getDirection()))
    	.apply(w.getRelative(BlockUtils.getLeft(w.getDirection())));
    	
    	new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
    	.setFacing(BlockUtils.getLeft(w.getDirection()))
    	.apply(w.getRelative(BlockUtils.getRight(w.getDirection())));
    	

    	new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
    	.setFacing(w.getDirection().getOppositeFace())
    	.apply(w.getRelative(0,height-1,0));
    	
    	w.getRelative(0,2,0).setType(Material.STONE_BRICK_WALL);
    	w.getRelative(0,3,0).setType(Material.POLISHED_DIORITE);
    	w.getRelative(0,4,0).setType(Material.STONE_BRICK_WALL);
    	w.getRelative(0,2,0).CorrectMultipleFacing(3);
    }
    
    private void spawnWindowOverhang(@NotNull Wall w) {
    	//log row
    	new OrientableBuilder(Material.DARK_OAK_LOG)
    	.setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getRight(w.getDirection())))
    	.apply(w).apply(w.getLeft()).apply(w.getRight());
    	
    	//Upsidedown overhang in front of log row.
    	new StairBuilder(Material.COBBLESTONE_STAIRS)
    	.setHalf(Half.TOP)
    	.setFacing(w.getDirection().getOppositeFace())
    	.apply(w.getFront());
    	new SlabBuilder(Material.COBBLESTONE_SLAB)
    	.setType(Type.TOP)
    	.apply(w.getFront().getLeft())
    	.apply(w.getFront().getRight());
    	
    	
    	//Inner upside down stairs
    	new StairBuilder(Material.COBBLESTONE_STAIRS)
    	.setHalf(Half.TOP)
    	.setFacing(BlockUtils.getLeft(w.getDirection()))
    	.apply(w.getRelative(0,-1,0).getLeft())
    	.setFacing(BlockUtils.getRight(w.getDirection()))
    	.apply(w.getRelative(0,-1,0).getRight());
    	
    	//Stairs at the top
    	new StairBuilder(Material.COBBLESTONE_STAIRS)
    	.setFacing(BlockUtils.getLeft(w.getDirection()))
    	.apply(w.getRight(2))
    	.apply(w.getRight().getRelative(0,1,0));

    	new StairBuilder(Material.COBBLESTONE_STAIRS)
    	.setFacing(BlockUtils.getRight(w.getDirection()))
    	.apply(w.getLeft(2))
    	.apply(w.getLeft().getRelative(0,1,0));
    	
    	new OrientableBuilder(Material.DARK_OAK_LOG)
    	.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
    	.apply(w.getRelative(0,1,0));
    }
    
    private enum MansionSecondFloorWallType{
    	THIN_WINDOWS,
    	LARGE_WINDOW,
    	BALCONY
    }

}
