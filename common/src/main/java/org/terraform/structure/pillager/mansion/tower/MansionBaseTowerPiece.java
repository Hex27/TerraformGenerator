package org.terraform.structure.pillager.mansion.tower;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.ground.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

public class MansionBaseTowerPiece extends MansionStandardTowerPiece {

	public MansionBaseTowerPiece(MansionJigsawBuilder builder, int widthX, int height, int widthZ, JigsawType type,
			BlockFace[] validDirs) {
		super(builder, widthX, height, widthZ, type, validDirs);
	}
	
	@Override
	public void decorateAwkwardCorner(Wall target, Random random, BlockFace one, BlockFace two) {
    	//Fill in gap in the corner
    	target.Pillar(MansionJigsawBuilder.roomHeight, Material.STONE_BRICKS);
    	target.getRelative(one).getRelative(two).setType(Material.COBBLESTONE_SLAB);
    	
    	new SlabBuilder(Material.COBBLESTONE_SLAB)
    	.setType(Type.TOP)
    	.apply(target.getRelative(one).getRelative(two).getRelative(0,3,0));
    	
    	new StairBuilder(Material.COBBLESTONE_STAIRS)
    	.setFacing(one.getOppositeFace())
    	.apply(target.getRelative(two).getRelative(0,4,0))
    	.setFacing(two.getOppositeFace())
    	.apply(target.getRelative(one).getRelative(0,4,0));
    	
    	target.getRelative(one).Pillar(4, Material.COBBLESTONE_WALL);
    	target.getRelative(one).CorrectMultipleFacing(4);
    	target.getRelative(two).Pillar(4, Material.COBBLESTONE_WALL);
    	target.getRelative(two).CorrectMultipleFacing(4);
    	
    	target.getRelative(one).setType(Material.COBBLESTONE);
    	target.getRelative(two).setType(Material.COBBLESTONE);
    	if(target.getRelative(two).getRelative(0,-2,0).getType().isSolid())
    		target.getRelative(two).getRelative(0,-1,0).setType(Material.COBBLESTONE);
    	if(target.getRelative(one).getRelative(0,-2,0).getType().isSolid())
    		target.getRelative(one).getRelative(0,-1,0).setType(Material.COBBLESTONE);
    	
    }

}
