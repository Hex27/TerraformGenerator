package org.terraform.structure.village.plains.forge;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.structure.village.plains.forge.PlainsVillageForgeWallPiece.PlainsVillageForgeWallType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.StairwayBuilder;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageForgeEntrancePiece extends PlainsVillageForgePiece {

    public PlainsVillageForgeEntrancePiece(PlainsVillagePopulator plainsVillagePopulator, int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(plainsVillagePopulator, widthX, height, widthZ, type, validDirs);

    }

    @Override
    public void build(PopulatorDataAbstract data, Random rand) {
        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getRelative(0, -1, 0);

        //Wall
        for (int i = 0; i < entry.getValue(); i++) {
            w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w = w.getLeft();
        }

        Wall core = new Wall(new SimpleBlock(data, this.getRoom().getX(), this.getRoom().getY() + 1, this.getRoom().getZ()), this.getRotation());
        core = core.getRear(2);
        
        //Stairway down
//        BlockUtils.angledStairwayUntilSolid(core.getFront().getRelative(0, -1, 0).get(), core.getDirection(),
//                new Material[]{
//                        Material.COBBLESTONE, Material.MOSSY_COBBLESTONE
//                },
//                Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS);
        
        if(core.getFront().getType().isSolid()) {
	        new StairwayBuilder(Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS)
	        .setAngled(true)
	        .setStopAtWater(true)
	        .setStairwayDirection(BlockFace.UP)
	        .build(core.getFront(3));
	        core.getFront().Pillar(2, rand, Material.AIR);
	        core.getFront(2).Pillar(3, rand, Material.AIR);
        }else
	        new StairwayBuilder(Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS)
	        .setAngled(true)
	        .setStopAtWater(true)
	        .build(core.getFront().getRelative(0, -1, 0));
        
    }
    
    @Override
    public void postBuildDecoration(Random rand, PopulatorDataAbstract data) {    	
    	if(getWallType() == PlainsVillageForgeWallType.SOLID) { //Door entrance
    		SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
    		Wall w = entry.getKey().getRelative(0, -1, 0);
    		for (int i = 0; i < entry.getValue(); i++) {
    			w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
    			w.RPillar(5, rand, Material.COBBLESTONE, Material.ANDESITE, Material.STONE);
    			w = w.getLeft();
    		}
    		 Wall core = new Wall(new SimpleBlock(data, this.getRoom().getX(), this.getRoom().getY() + 1, this.getRoom().getZ()), this.getRotation());
	         core = core.getRear(2);
	         core.getRelative(0,-1,0).setType(Material.CHISELED_STONE_BRICKS);
    	     BlockUtils.placeDoor(data, plainsVillagePopulator.woodDoor, core.getX(), core.getY(), core.getZ(), core.getDirection().getOppositeFace());
    	     
    	     //Door decor
    	     core.getRelative(0,2,0).getFront().setType(Material.STONE_BRICK_SLAB);
    	     
    	     core.getRelative(0,2,0).setType(Material.CHISELED_STONE_BRICKS);
    	     core.getLeft().Pillar(2, rand, Material.CHISELED_STONE_BRICKS);
    	     core.getRight().Pillar(2, rand, Material.CHISELED_STONE_BRICKS);
    	     
    	     core.getLeft().getFront().setType(Material.STONE_BRICK_WALL);
    	     core.getRight().getFront().setType(Material.STONE_BRICK_WALL);

    	     core.getLeft().getFront().getRelative(0,-1,0).setType(Material.STONE_BRICKS);
    	     core.getRight().getFront().getRelative(0,-1,0).setType(Material.STONE_BRICKS);
    	     
    	     new StairBuilder(Material.STONE_BRICK_STAIRS)
    	     .setFacing(BlockUtils.getLeft(core.getDirection()))
    	     .apply(core.getRight().getFront().getRelative(0,1,0))
    	     .setFacing(BlockUtils.getRight(core.getDirection()))
    	     .apply(core.getLeft().getFront().getRelative(0,1,0))
    	     .setHalf(Half.TOP)
    	     .apply(core.getRight().getRelative(0,2,0))
    	     .setFacing(BlockUtils.getLeft(core.getDirection()))
    	     .apply(core.getLeft().getRelative(0,2,0));
    	}
    	else //Just a hole in a fence
    	{
    		SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
    		Wall w = entry.getKey();
    		for (int i = 0; i < entry.getValue(); i++) {
    			w.getRelative(0,-2,0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
    			
    			if(i == 2) {
    				//Opening
    				w.getRelative(0,-1,0).setType(Material.CHISELED_STONE_BRICKS);
    			}
    			else if(i == 1 || i == 3) 
    			{
    				w.getRelative(0,-1,0).Pillar(2, rand, plainsVillagePopulator.woodLog);
    				w.getRelative(0,1,0).setType(Material.STONE_SLAB,Material.COBBLESTONE_SLAB,Material.ANDESITE_SLAB);
    			}
    			else
    			{
    				w.get().lsetType(plainsVillagePopulator.woodFence);
        			w.CorrectMultipleFacing(1);
        			new OrientableBuilder(plainsVillagePopulator.woodLog)
        			.setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getLeft(w.getDirection())))
        			.apply(w.getRelative(0,-1,0));
    			}
    			
    			w = w.getLeft();
    		}
    	}
    	
    }

}
