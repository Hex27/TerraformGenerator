package org.terraform.structure.pillager.mansion;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.StairBuilder;

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

    public void decorateAwkwardCorner(Wall target, Random random, BlockFace one, BlockFace two) {
        //Fill in gap in the corner
    	target.Pillar(groundFloorRoomHeight, random, Material.POLISHED_ANDESITE);

    	target.getRelative(0,2,0).setType(Material.STONE_BRICK_WALL);
    	target.getRelative(0,3,0).setType(Material.POLISHED_DIORITE);
    	target.getRelative(0,4,0).setType(Material.STONE_BRICK_WALL);
    	target.getRelative(0,2,0).CorrectMultipleFacing(3);
    	
    	target.getRelative(0, -1, 0).downUntilSolid(random, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        
    	//Small stair base
    	new StairBuilder(Material.COBBLESTONE_STAIRS)
    	.setFacing(one.getOppositeFace())
    	.apply(target.getRelative(one));
    	
    	new StairBuilder(Material.COBBLESTONE_STAIRS)
    	.setFacing(two.getOppositeFace())
    	.apply(target.getRelative(two));
    	
    	new StairBuilder(Material.COBBLESTONE_STAIRS)
    	.setFacing(two.getOppositeFace())
    	.apply(target.getRelative(two).getRelative(one))
    	.correct();

    	//Small stair base
    	new StairBuilder(Material.COBBLESTONE_STAIRS)
    	.setFacing(one.getOppositeFace())
    	.setHalf(Half.TOP)
    	.apply(target.getRelative(0,6,0).getRelative(one));
    	
    	new StairBuilder(Material.COBBLESTONE_STAIRS)
    	.setFacing(two.getOppositeFace())
    	.setHalf(Half.TOP)
    	.apply(target.getRelative(0,6,0).getRelative(two));
    	
    	new StairBuilder(Material.COBBLESTONE_STAIRS)
    	.setFacing(two.getOppositeFace())
    	.setHalf(Half.TOP)
    	.apply(target.getRelative(0,6,0).getRelative(two).getRelative(one))
    	.correct();
    }
    

}
