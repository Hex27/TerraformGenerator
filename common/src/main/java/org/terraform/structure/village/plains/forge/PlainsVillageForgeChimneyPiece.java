package org.terraform.structure.village.plains.forge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.StairBuilder;

public class PlainsVillageForgeChimneyPiece extends PlainsVillageForgeStandardPiece {

	public PlainsVillageForgeChimneyPiece(PlainsVillagePopulator plainsVillagePopulator, int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
		super(plainsVillagePopulator, widthX, height, widthZ, type, validDirs);
	}
	
	//Use postBuildDecoration, as the walls are built after build()
    @Override
    public void postBuildDecoration(Random random, PopulatorDataAbstract data) {
    	SimpleBlock core = new SimpleBlock(data, this.getRoom().getX(), this.getRoom().getY(), this.getRoom().getZ());
    	if(this.getWalledFaces().size() == 0) {
    		spawnStraightChimney(random, new Wall(core));
    	}
    	
    	if(this.getWalledFaces().size() == 1){
    		if(core.getRelative(this.getWalledFaces().get(0),3).getType() == Material.CHISELED_STONE_BRICKS) {
    			spawnStraightChimney(random, new Wall(core));
    			return;
    		}
       	}
    	ArrayList<BlockFace> walledFaces = this.getWalledFaces();
    	Collections.shuffle(walledFaces);
    	for(BlockFace face:walledFaces) {

    		//Don't spawn walled chimney against entrance
    		if(core.getRelative(face,3).getType() == Material.CHISELED_STONE_BRICKS)
    			continue;
    		
    		Wall target =  new Wall(core,face.getOppositeFace());
    		spawnWallChimney(random, target.getRear(2));
    		return;
    	}
    }
    
    /**
     * 3x3 chimney spawned right in the middle of the room.
     * @param random
     * @param core must contain the direction the chimney is to face.
     */
    private void spawnWallChimney(Random random, Wall core) {
    	core = core.getRelative(0,1,0);
    	
    	//Refers to the height of the segment of the 
    	//chimney where there's a repeating pattern
    	int chimneyCoreHeight = random.nextInt(3)+5; 

    	for(BlockFace face:BlockUtils.xzDiagonalPlaneBlockFaces) {
    		Wall target = core.getRelative(face);
    		target.Pillar(chimneyCoreHeight+1, true, random, Material.COBBLESTONE_WALL, Material.COBBLESTONE);
    		target.CorrectMultipleFacing(chimneyCoreHeight+1); 
    	}
    	
    	for(BlockFace face:BlockUtils.directBlockFaces) {
    		

    		core.getRelative(face).setType(Material.COBBLESTONE);
    		
    		for(int i = 0; i < chimneyCoreHeight; i++) {
    			if(i % 2 == 0)
	    			new StairBuilder(Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS)
	    			.setFacing(face.getOppositeFace())
	    			.apply(core.getRelative(0,2+i,0).getRelative(face));
    			else
    				core.getRelative(0,2+i,0).getRelative(face).setType(Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
    		}
    		
    		if(face == core.getDirection()) { //Front
    			new StairBuilder(Material.COBBLESTONE_STAIRS)
    			.setFacing(face.getOppositeFace())
    			.apply(core.getFront(2).getLeft())
    			.apply(core.getFront(2).getRight());
    			core.getFront().getLeft().setType(Material.COBBLESTONE);
    			core.getFront().getRight().setType(Material.COBBLESTONE);
    			
    		}else if(face != core.getDirection().getOppositeFace()) //Sides
    			new StairBuilder(Material.COBBLESTONE_STAIRS)
    			.setFacing(face.getOppositeFace())
    			.apply(core.getRelative(face,2));
    		
    		else { //Walled face
    			new StairBuilder(Material.COBBLESTONE_STAIRS)
    			.setFacing(face.getOppositeFace())
    			.apply(core.getRelative(face,2))
    			.setHalf(Half.TOP)
    			.apply(core.getRelative(0,2,0).getRelative(face,2));
    			

    			//Modify the exterior. 
    			core.getRelative(face,2).getRelative(0,3,0).Pillar(2, random, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
    			
    			core.getRelative(face,2).getLeft().getRelative(0,-1,0)
    			.Pillar(6, random, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
    			
    			core.getRelative(face,2).getRight().getRelative(0,-1,0)
    			.Pillar(6, random, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
    			
    			new StairBuilder(Material.COBBLESTONE_STAIRS)
    			.setFacing(BlockUtils.getLeft(face))
    			.apply(core.getRelative(face, 2).getRelative(0,4,0).getLeft())
    			.setFacing(BlockUtils.getRight(face))
    			.apply(core.getRelative(face, 2).getRelative(0,4,0).getRight());
    			
    			core.getRelative(face, 2).getRelative(0, 5, 0).setType(Material.COBBLESTONE_SLAB, Material.MOSSY_COBBLESTONE_SLAB);
    			
    			//This will be the base
    			core.getRelative(face,2).getRelative(0,-2,0).getLeft().downUntilSolid(random,Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
    			core.getRelative(face,2).getRelative(0,-1,0).downUntilSolid(random, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
    			core.getRelative(face,2).getRelative(0,-2,0).getRight().downUntilSolid(random,Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
    			
    			//Solidify the wall behind
    			core.getRelative(face).Pillar(6, random, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
    			core.getRelative(face).getLeft().Pillar(6, random, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
    			core.getRelative(face).getRight().Pillar(6, random, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
    		}
    		
    		//Iron bars placed last.
    		core.getRelative(0,1,0).getRelative(face).setType(Material.IRON_BARS);
    		core.getRelative(0,1,0).getRelative(face).CorrectMultipleFacing(1); 
    	}
    	
    	new DirectionalBuilder(Material.BLAST_FURNACE)
    	.setFacing(core.getDirection())
    	.apply(core.getFront());

    	core.Pillar(chimneyCoreHeight+2, random, Material.AIR);
    	core.getRelative(0,-1,0).setType(Material.CAMPFIRE);
    	core.getRelative(0,-2,0).setType(Material.HAY_BLOCK);
    	core.setType(Material.LAVA);
    	//core.getRelative(0,chimneyCoreHeight+2,0).Pillar(2, random, Material.COBBLESTONE);
    	//core.getRelative(0,chimneyCoreHeight+4,0).Pillar(2, random, Material.COBBLESTONE_WALL);
    }
    
    /**
     * 3x3 chimney spawned right in the middle of the room.
     * @param random
     * @param core
     */
    private void spawnStraightChimney(Random random, Wall core) {
    	core = core.getRelative(0,1,0);
    	
    	//Refers to the height of the segment of the 
    	//chimney where there's a repeating pattern
    	int chimneyCoreHeight = random.nextInt(3)+6; 
    	for(BlockFace face:BlockUtils.directBlockFaces) {

    		core.getRelative(face).setType(Material.COBBLESTONE);
    		core.getRelative(0,1,0).getRelative(face).setType(Material.IRON_BARS);

    		for(int i = 0; i < chimneyCoreHeight; i++) {
    			if(i % 2 == 0)
	    			new StairBuilder(Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS)
	    			.setFacing(face.getOppositeFace())
	    			.apply(core.getRelative(0,2+i,0).getRelative(face));
    			else
    				core.getRelative(0,2+i,0).getRelative(face).setType(Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
    		}
    	}

    	for(BlockFace face:BlockUtils.xzDiagonalPlaneBlockFaces) {
    		Wall target = core.getRelative(face);
    		target.Pillar(chimneyCoreHeight+1, true, random, Material.COBBLESTONE_WALL, Material.COBBLESTONE);
    		target.CorrectMultipleFacing(chimneyCoreHeight+1); 
    	}
    	
    	BlockFace blastFurnaceDir = BlockUtils.getDirectBlockFace(random);
    	new DirectionalBuilder(Material.BLAST_FURNACE)
    	.setFacing(blastFurnaceDir)
    	.apply(core.getRelative(blastFurnaceDir));

    	//Empty out space in the chimney
    	core.Pillar(chimneyCoreHeight+2, random, Material.AIR);
    	core.getRelative(0,-1,0).setType(Material.CAMPFIRE);
    	core.getRelative(0,-2,0).setType(Material.HAY_BLOCK);
    	core.setType(Material.LAVA);
    	
    	//core.getRelative(0,chimneyCoreHeight+2,0).Pillar(2, random, Material.COBBLESTONE);
    	//core.getRelative(0,chimneyCoreHeight+4,0).Pillar(2, random, Material.COBBLESTONE_WALL);
    }

}
