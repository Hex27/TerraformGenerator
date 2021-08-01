package org.terraform.structure.pillager.mansion.ground;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.TrapdoorBuilder;

import java.util.Random;

public class MansionRoofHandler {

	/**
	 * Gets the largest possible rectangle that the house's shape can offer
	 * Doesn't seem to work all the time though
	 * @param builder
	 * @return
	 */
    public static int[][] getLargestRectangle(MansionJigsawBuilder builder) {
        int[] lowestCoords = null;
        int[] highestCoords = null;
        
        //SimpleLocation lowestCoords = new SimpleLocation();
        //SimpleLocation highestCoords = new SimpleLocation();
        for (JigsawStructurePiece piece : builder.getPieces().values()) {
            if (lowestCoords == null) {
                lowestCoords = new int[]{piece.getRoom().getX(), piece.getRoom().getZ()};
            }
            if (highestCoords == null) highestCoords = new int[]{piece.getRoom().getX(), piece.getRoom().getZ()};
            if (piece.getRoom().getX() < lowestCoords[0])
                lowestCoords[0] = piece.getRoom().getX();
            if (piece.getRoom().getZ() < lowestCoords[1])
                lowestCoords[1] = piece.getRoom().getZ();

            if (piece.getRoom().getX() > highestCoords[0])
                highestCoords[0] = piece.getRoom().getX();
            if (piece.getRoom().getZ() > highestCoords[1])
                highestCoords[1] = piece.getRoom().getZ();
        }
        
        int previousNotInRect = getNumberOfPiecesNotInRectangle(builder, lowestCoords,highestCoords);
        int i = 0;
        int stall = 0;
        //Shrink the rectangle one side at a time until it is a rectangle
        //If the shrink operation did not change the number of pieces not in the rectangle,
        //then undo the shrink.
        while(previousNotInRect != 0) {
        	int piecesInRect = 0;
        	switch(i%4) {
        	case 0:
        		lowestCoords[0] += MansionJigsawBuilder.groundFloorRoomWidth;
        		piecesInRect = getNumberOfPiecesNotInRectangle(builder, lowestCoords,highestCoords);
        		if(piecesInRect == previousNotInRect) {
        			stall++;
        			if(stall < 4)
        				lowestCoords[0] -= MansionJigsawBuilder.groundFloorRoomWidth;
        			else
        				stall = 0;
        		}
        		break;
        	case 1:
        		lowestCoords[1] += MansionJigsawBuilder.groundFloorRoomWidth;
        		piecesInRect = getNumberOfPiecesNotInRectangle(builder, lowestCoords,highestCoords);
        		if(piecesInRect == previousNotInRect) {
        			stall++;
        			if(stall < 4)
        				lowestCoords[1] -= MansionJigsawBuilder.groundFloorRoomWidth;
        			else
        				stall = 0;
        		}
        		break;
        	case 2:
        		highestCoords[0] -= MansionJigsawBuilder.groundFloorRoomWidth;
        		piecesInRect = getNumberOfPiecesNotInRectangle(builder, lowestCoords,highestCoords);
        		if(piecesInRect == previousNotInRect) {
        			stall++;
        			if(stall < 4)
        				highestCoords[0] += MansionJigsawBuilder.groundFloorRoomWidth;
        			else
        				stall = 0;
        		}
        		break;
        	case 3:
        		highestCoords[1] -= MansionJigsawBuilder.groundFloorRoomWidth;
        		piecesInRect = getNumberOfPiecesNotInRectangle(builder, lowestCoords,highestCoords);
        		if(piecesInRect == previousNotInRect) {
        			stall++;
        			if(stall < 4)
        				highestCoords[1] += MansionJigsawBuilder.groundFloorRoomWidth;
        			else
        				stall = 0;}
        		break;
        	}
        	previousNotInRect = piecesInRect;
        	i++;
        }
        int y = builder.getCore().getY();
        for (int x = lowestCoords[0]; x <= highestCoords[0]; x += builder.getPieceWidth()) {
            for (int z = lowestCoords[1]; z <= highestCoords[1]; z += builder.getPieceWidth()) {
                if (builder.getPieces().containsKey(new SimpleLocation(x, y, z))) {
                	builder.getRoofedLocations().add(new SimpleLocation(x, y+MansionJigsawBuilder.roomHeight + 1, z));
                }
            }
        }
        return new int[][] {lowestCoords, highestCoords};
    }
    
    private static int getNumberOfPiecesNotInRectangle(MansionJigsawBuilder builder, int[] lowestCoords, int[] highestCoords) {
    	int y = builder.getCore().getY();
    	int notInRect = 0;
        for (int x = lowestCoords[0]; x <= highestCoords[0]; x += builder.getPieceWidth()) {
            for (int z = lowestCoords[1]; z <= highestCoords[1]; z += builder.getPieceWidth()) {
                if (!builder.getPieces().containsKey(new SimpleLocation(x, y, z))) {
                	notInRect++;
                }
            }
        }

        //If all pieces accounted for by looping the coords, this is a rectangle.
        return notInRect;
    }
    
    public static Axis getDominantAxis(int[] lowestCoords, int[] highestCoords) {
    	Axis superiorAxis;
        //Longer axis is the superior one
        if (highestCoords[0] - lowestCoords[0] > highestCoords[1] - lowestCoords[1])
            superiorAxis = Axis.X;
        else if (highestCoords[0] - lowestCoords[0] < highestCoords[1] - lowestCoords[1])
            superiorAxis = Axis.Z;
        else //Square house
            superiorAxis = Axis.X;

        return superiorAxis;
    }
    
    public static BlockFace getDominantBlockFace(int[] lowestCoords, int[] highestCoords) {
    	BlockFace superiorAxis;
        //Longer axis is the superior one
        if (highestCoords[0] - lowestCoords[0] > highestCoords[1] - lowestCoords[1])
            superiorAxis = BlockFace.WEST;
        else if (highestCoords[0] - lowestCoords[0] < highestCoords[1] - lowestCoords[1])
            superiorAxis = BlockFace.NORTH;
        else //Square house
            superiorAxis = BlockFace.WEST;

        return superiorAxis;
    }

    /**
     * 
     * @param rand
     * @param builder
     * @param bounds
     * @return the highest Y modified by the roof
     */
    public static int placeTentRoof(Random rand, MansionJigsawBuilder builder, int[][] bounds) {
        Axis superiorAxis = Axis.Z;
        PopulatorDataAbstract data = builder.getCore().getPopData();
        
        int highestY = -1;
        
        int[] lowestCoords = bounds[0];
        int[] highestCoords = bounds[1];
        
        //RoofY
        //Lol idk why 4
        int y = builder.getCore().getY() + 2*MansionJigsawBuilder.roomHeight + 4;
        
        superiorAxis = getDominantAxis(lowestCoords, highestCoords);
        
        lowestCoords[0] -= 5;
        lowestCoords[1] -= 5;
        highestCoords[0] += 5;
        highestCoords[1] += 5;

        Wall w;
        int length;
        int breadth;
        if (superiorAxis == Axis.X) {
            length = highestCoords[0] - lowestCoords[0] + 5;
            breadth = (highestCoords[1] - lowestCoords[1]) + 3;
            w = new Wall(new SimpleBlock(data, highestCoords[0] + 2, y - 1, lowestCoords[1] - 1), BlockFace.WEST);
        } else {
            length = highestCoords[1] - lowestCoords[1] + 5;
            breadth = (highestCoords[0] - lowestCoords[0]) + 3;
            w = new Wall(new SimpleBlock(data, lowestCoords[0] - 1, y - 1, lowestCoords[1] - 2), BlockFace.SOUTH);
        }

        for (int i = 0; i < length; i++) {
            Wall target = w;
            boolean ascendBlock = false;
            for (int right = 0; right < breadth-1; right++) {

                //Place logs at the sides
                if (right != 0 && right != breadth - 1) {
                    if (i == 0 || i == length - 1) {
                        //Sandwiched by trapdoors
                        new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR)
                                .setHalf(Half.TOP)
                                .setOpen(true)
                                .setFacing(i == 0 ? target.getDirection().getOppositeFace() : target.getDirection())
                                .lapply(target.getRelative(0, -1, 0));
                    } else {
                    	if(target.getRelative(0, -2, 0).getType() != Material.DARK_OAK_PLANKS)
                    		new OrientableBuilder(Material.DARK_OAK_LOG)
                                .setAxis(superiorAxis)
                                .apply(target.getRelative(0, -1, 0).get());
                        
                        //Connect the roof to the walls below
                        if(i == 2 || i == length - 3) {
                        	//Lower Walls
                        	Wall bottom = target.getAtY(builder.getCore().getY() + 2*MansionJigsawBuilder.roomHeight+2);
                        	if(bottom != null) {
                        		if(BlockUtils.isAir(bottom.getType()) || Tag.STAIRS.isTagged(bottom.getType()) || Tag.SLABS.isTagged(bottom.getType())) {
                        			bottom.setType(Material.DARK_OAK_PLANKS);
                        		}
                        		target.getRelative(0,-2,0).downPillar(new Random(), target.getY()-bottom.getY()-2, bottom.getType());
                        	}
                        }
                        else if(i != 1 && i != length-2)//Force set air for things below the roof within the walls
                        {
                        	target.getRelative(0,-2,0).downPillar(new Random(), target.getY()-y+1, Material.AIR);
                        }
                    }
                }

                Material slabType = Material.DARK_OAK_SLAB;

                if (right == 0 || right == breadth - 2 || i == 0 || i == length - 1) {
                    slabType = Material.COBBLESTONE_SLAB;
                }

                if (breadth % 2 == 1) { //For odd breadth.
                    if (right > breadth / 2) {
                        //Slope down
                    	attemptReplaceSlab(slabType,target,ascendBlock? Type.BOTTOM : Type.DOUBLE);
                    	if(ascendBlock) {
                    		target = target.getRight().getRelative(0, -1, 0);
                    		ascendBlock = false;
                    	}else{
                    		target = target.getRight();
                    		ascendBlock = true;
                    	}
                    } else if (right < breadth / 2) {
                        //Slope up
                    	attemptReplaceSlab(slabType,target,ascendBlock? Type.DOUBLE : Type.BOTTOM);
                    	if(ascendBlock) {
                    		target = target.getRight().getRelative(0, 1, 0);
                    		ascendBlock = false;
                    	}else{
                    		target = target.getRight();
                    		ascendBlock = true;
                    	}
                    } else {
                        //Top (Only exists when the breadth is odd.
                    	highestY = target.getY();
                        target.setType(slabType);
                    	if(ascendBlock) {
                    		target = target.getRight().getRelative(0, -1, 0);
                    		ascendBlock = false;
                    	}else{
                    		target = target.getRight();
                    		ascendBlock = true;
                    	}
                    }
                } else { //For even breadth
                    if (right == breadth / 2 - 1) {
                    	highestY = target.getY();
                        target.setType(Material.DARK_OAK_PLANKS);
                        if(slabType == Material.COBBLESTONE_SLAB)
                        	target.setType(Material.COBBLESTONE);
                        target = target.getRight();
                    } else if (right >= breadth / 2) {
                        //Slope down
                    	attemptReplaceSlab(slabType,target,ascendBlock? Type.BOTTOM : Type.DOUBLE);
                    	if(ascendBlock) {
                    		target = target.getRight().getRelative(0, -1, 0);
                    		ascendBlock = false;
                    	}else{
                    		target = target.getRight();
                    		ascendBlock = true;
                    	}
                    } else if (right < breadth / 2) {
                        //Slope up
                    	attemptReplaceSlab(slabType,target,ascendBlock? Type.DOUBLE : Type.BOTTOM);
                    	if(ascendBlock) {
                    		target = target.getRight().getRelative(0, 1, 0);
                    		ascendBlock = false;
                    	}else{
                    		target = target.getRight();
                    		ascendBlock = true;
                    	}
                    }
                }
            }
            w = w.getFront();
        }

        return highestY;
    }
    
    private static void attemptReplaceSlab(Material slabType, Wall w, Type type) {
    	if(!w.getType().isSolid()) {
        	if(w.findCeiling(5) != null) return;
	    	new SlabBuilder(slabType)
	    	.setType(type)
	    	.lapply(w);
    	} else if(Tag.STAIRS.isTagged(w.getType()) || Tag.SLABS.isTagged(w.getType()))
    		w.setType(Material.DARK_OAK_PLANKS);
    }

}
