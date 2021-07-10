package org.terraform.structure.pillager.mansion;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

public class MansionJigsawBuilder extends JigsawBuilder {
	
	public static final int groundFloorRoomHeight = 7;
	public static final int groundFloorRoomWidth = 9;
	
    public MansionJigsawBuilder(int widthX, int widthZ, PopulatorDataAbstract data, int x, int y, int z) {
        super(widthX, widthZ, data, x, y, z);
        this.pieceWidth = groundFloorRoomWidth;
        this.pieceRegistry = new JigsawStructurePiece[]{
                new MansionStandardGroundRoomPiece(groundFloorRoomWidth, groundFloorRoomHeight, groundFloorRoomWidth, JigsawType.STANDARD, BlockUtils.directBlockFaces),
                new MansionWallPiece(groundFloorRoomWidth, groundFloorRoomHeight, groundFloorRoomWidth, JigsawType.END, BlockUtils.directBlockFaces),
                new MansionEntrancePiece(groundFloorRoomWidth, groundFloorRoomHeight, groundFloorRoomWidth, JigsawType.ENTRANCE, BlockUtils.directBlockFaces)
        };
        this.chanceToAddNewPiece = 100;
    }

    @Override
    public JigsawStructurePiece getFirstPiece(Random random) {
        return new MansionStandardGroundRoomPiece(groundFloorRoomWidth, groundFloorRoomHeight, groundFloorRoomWidth, JigsawType.STANDARD, BlockUtils.directBlockFaces);
    }
    
    @Override
    public void build(Random random) {
        super.build(random);

        //Make sure awkward corners are fixed
        for (JigsawStructurePiece piece : this.pieces.values()) {
            SimpleBlock core = new SimpleBlock(
                    this.core.getPopData(),
                    piece.getRoom().getX(),
                    piece.getRoom().getY(),
                    piece.getRoom().getZ());
            Wall target;
            
            if (piece.getWalledFaces().contains(BlockFace.NORTH)
                    && piece.getWalledFaces().contains(BlockFace.WEST)) { //nw
                target = new Wall(core.getRelative(-5, 1, -5));
                decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.WEST);
            }
            if (piece.getWalledFaces().contains(BlockFace.NORTH)
                    && piece.getWalledFaces().contains(BlockFace.EAST)) { //ne
                target = new Wall(core.getRelative(5, 1, -5));
                decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.EAST);
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH)
                    && piece.getWalledFaces().contains(BlockFace.WEST)) { //sw
                target = new Wall(core.getRelative(-5, 1, 5));
                decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.WEST);
            }
            if (piece.getWalledFaces().contains(BlockFace.SOUTH)
                    && piece.getWalledFaces().contains(BlockFace.EAST)) { //se
                target = new Wall(core.getRelative(5, 1, 5));
                decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.EAST);
            }
        }

        joinOuterPillars();
        
        //Place the roof
//        if (!MansionRoofHandler.isRectangle(this))
//            MansionRoofHandler.placeStandardRoof(this);
//        else
//            MansionRoofHandler.placeTentRoof(random, this);

        //Decorate rooms and walls
        for (JigsawStructurePiece piece : this.pieces.values()) {
            piece.postBuildDecoration(random, this.core.getPopData());
        }

    }

    private HashMap<Wall, BlockFace[]> outerPillars = new HashMap<>();
    public void decorateAwkwardCorner(Wall target, Random random, BlockFace one, BlockFace two) {
        Material[] fenceType = {Material.COBBLESTONE_WALL,Material.COBBLESTONE_WALL,Material.COBBLESTONE_WALL,Material.COBBLESTONE_WALL,Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL};
        
        //Fill in gap in the corner
    	target.Pillar(groundFloorRoomHeight, random, Material.DARK_OAK_LOG);
    	target.getRelative(0, -1, 0).downUntilSolid(random, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        
    	//Build decorative pillar at the corner
    	
    	target = target.getRelative(one,3).getRelative(two,3);
    	
    	target.Pillar(groundFloorRoomHeight, random, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS);
    	target.getRelative(0, -1, 0).downUntilSolid(random, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        target.setType(Material.STONE_BRICKS); //For easier checks
    	
        target = target.getRelative(0, 1, 0);
        
        if(!Tag.WALLS.isTagged(target.getRelative(one).getType())) {
        	target.getRelative(one).Pillar(groundFloorRoomHeight-1, random, fenceType);
            target.getRelative(one).CorrectMultipleFacing(groundFloorRoomHeight-1);
            target.getRelative(0, -1, 0).getRelative(one).downUntilSolid(random, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        }
        else
        {
        	target.getRelative(0, -1, 0).getRelative(one).Pillar(groundFloorRoomHeight, new Random(), Material.AIR);
        }
        
        if(!Tag.WALLS.isTagged(target.getRelative(two).getType())) {
            target.getRelative(two).Pillar(groundFloorRoomHeight-1, random, fenceType);
            target.getRelative(two).CorrectMultipleFacing(groundFloorRoomHeight-1);
            target.getRelative(0, -1, 0).getRelative(two).downUntilSolid(random, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        }
        else
        {
        	target.getRelative(0, -1, 0).getRelative(two).Pillar(groundFloorRoomHeight, new Random(), Material.AIR);
        }
        
        target = target.getRelative(0, -1, 0);
        
        Wall closebyPillar = null;
        for(BlockFace face:BlockUtils.directBlockFaces) {
        	if(target.getRelative(face,2).getType() == Material.STONE_BRICKS) {
        		closebyPillar = target.getRelative(face,2);
        		break;
        	}
        }
        
        if(closebyPillar == null) {
        	outerPillars.put(target, new BlockFace[] {one.getOppositeFace(),two.getOppositeFace()});
        }
        else
        {
            Iterator<Wall> it = outerPillars.keySet().iterator();
            while(it.hasNext()) {
            	Wall candidate = it.next();
            	if(candidate.getX() == closebyPillar.getX()
            			&& candidate.getY() == closebyPillar.getY()
            			&& candidate.getZ() == closebyPillar.getZ())
            		it.remove();
            }
        }
    }
    
    public void joinOuterPillars() {
    	for(Entry<Wall, BlockFace[]> entry:outerPillars.entrySet()) {
            //Attempt to connect the pillars
            for(BlockFace face:entry.getValue()) {
            	Wall pillarConnector = entry.getKey().getRelative(face);
                while(pillarConnector.getType() != Material.STONE_BRICKS 
                		&& pillarConnector.getType() != Material.MOSSY_STONE_BRICKS
                		&& pillarConnector.getRelative(face,2).getType() != Material.DARK_OAK_PLANKS) {
                	if(pillarConnector.getType() != Material.STONE_BRICK_WALL) 
                	{
                    	
                    	//Special edge case for when there's 2 pillars very close to each other
                    	//and they lead into an awkward area
                    	if(pillarConnector.getRelative(face,3).getType() == Material.DARK_OAK_PLANKS) {
                    		for(BlockFace sideFace:BlockUtils.getAdjacentFaces(face)) {
                    			if(pillarConnector.getRelative(sideFace,2).getType() == Material.STONE_BRICK_WALL) {
                    				new StairBuilder(Material.COBBLESTONE_STAIRS)
                    				.setFacing(face)
                    				.apply(pillarConnector.getRelative(sideFace).getRelative(0,-1,0));
                    				break;
                    			}
                    		}
                    	}

                    	pillarConnector.setType(Material.STONE_BRICK_WALL);
                    	pillarConnector.CorrectMultipleFacing(1);
                    	
                	}
                	else if(pillarConnector.getRelative(face,3).getType() == Material.DARK_OAK_PLANKS)
                	{
                		pillarConnector.Pillar(groundFloorRoomHeight, new Random(), Material.STONE_BRICKS);
                    	break;
                	}
                	pillarConnector = pillarConnector.getRelative(face);
                }
                
            }
    	}
    }

}
