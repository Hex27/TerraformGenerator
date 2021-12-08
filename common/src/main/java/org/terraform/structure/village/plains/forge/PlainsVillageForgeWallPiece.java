package org.terraform.structure.village.plains.forge;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageForgeWallPiece extends PlainsVillageForgePiece {

    public PlainsVillageForgeWallPiece(PlainsVillagePopulator plainsVillagePopulator, int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(plainsVillagePopulator, widthX, height, widthZ, type, validDirs);
    }


    @Override
    public void build(PopulatorDataAbstract data, Random rand) {
    	//All handled in postBuildDecoration, as the type to build is only identified then.
    } 
    
    @Override
    public void postBuildDecoration(Random rand, PopulatorDataAbstract data) {
    	if(getWallType() == PlainsVillageForgeWallType.SOLID) { //Wall
    		SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
    		Wall w = entry.getKey().getRelative(0, -1, 0);
    		for (int i = 0; i < entry.getValue(); i++) {
    			w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
    			w.RPillar(5, rand, Material.COBBLESTONE, Material.ANDESITE, Material.STONE);
    			
    			//Handle window hole placement
    			if(i == 2) { //Center
    				w.getRelative(0,2,0).setType(Material.AIR);
    				new SlabBuilder(Material.COBBLESTONE_SLAB,Material.ANDESITE_SLAB,Material.STONE_SLAB,Material.STONE_BRICK_SLAB)
    				.setType(Type.TOP)
    				.apply(w.getRelative(0,3,0).getFront());
    				
    				new StairBuilder(Material.COBBLESTONE_STAIRS,Material.ANDESITE_STAIRS,Material.STONE_STAIRS,Material.STONE_BRICK_STAIRS)
    				.setFacing(w.getDirection().getOppositeFace())
    				.apply(w.getRelative(0,1,0).getFront());
    				
    				w.getFront().downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
    			}
    			else if(i == 1 || i == 3) { //Beside the opening
    				w.getRelative(0,3,0).getFront().setType(Material.COBBLESTONE_SLAB,Material.ANDESITE_SLAB,Material.STONE_SLAB,Material.STONE_BRICK_SLAB);
    				w.getRelative(0,2,0).getFront().setType(Material.COBBLESTONE_WALL,Material.STONE_BRICK_WALL,Material.ANDESITE_WALL);
    				
    				//Stair decor
    				if(i == 1)
    					new StairBuilder(Material.COBBLESTONE_STAIRS,Material.ANDESITE_STAIRS,Material.STONE_STAIRS,Material.STONE_BRICK_STAIRS)
    					.setFacing(BlockUtils.getRight(w.getDirection()))
    					.apply(w.getRelative(0,2,0))
    					.setFacing(BlockUtils.getLeft(w.getDirection()))
    					.setHalf(Half.TOP)
    					.apply(w.getRelative(0,1,0).getFront());
    				else
    					new StairBuilder(Material.COBBLESTONE_STAIRS,Material.ANDESITE_STAIRS,Material.STONE_STAIRS,Material.STONE_BRICK_STAIRS)
    					.setFacing(BlockUtils.getLeft(w.getDirection()))
    					.apply(w.getRelative(0,2,0))
    					.setFacing(BlockUtils.getRight(w.getDirection()))
    					.setHalf(Half.TOP)
    					.apply(w.getRelative(0,1,0).getFront());
    				
    			}
    			
    			w = w.getLeft();
    		}
    	}
    	else //Fences
    	{
    		SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
    		Wall w = entry.getKey();
    		for (int i = 0; i < entry.getValue(); i++) {
    			w.getRelative(0,-2,0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
    			
    			if(i == 2) {
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
    
    public static enum PlainsVillageForgeWallType {
    	SOLID, FENCE;
    }
}
