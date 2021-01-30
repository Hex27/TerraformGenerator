package org.terraform.structure.village.plains.forge;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.OrientableBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageForgeWallPiece extends PlainsVillageForgePiece {


	
    public PlainsVillageForgeWallPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
    }


    @Override
    public void build(PopulatorDataAbstract data, Random rand) {
//        Material[] stoneBricks = {Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS};
//        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
//        Wall w = entry.getKey().getRelative(0, -1, 0);
//        for (int i = 0; i < entry.getValue(); i++) {
//            w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
//            w.Pillar(5, rand, stoneBricks);
//            
//            w = w.getLeft();
//        }
    } 
    @Override
    public void postBuildDecoration(Random rand, PopulatorDataAbstract data) {
    	if(getWallType() == PlainsVillageForgeWallType.SOLID) {
    		SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
    		Wall w = entry.getKey().getRelative(0, -1, 0);
    		for (int i = 0; i < entry.getValue(); i++) {
    			w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
    			w.Pillar(4, rand, Material.COBBLESTONE, Material.ANDESITE, Material.STONE);
    			w = w.getLeft();
    		}
    	}
    	else
    	{
    		SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
    		Wall w = entry.getKey();
    		for (int i = 0; i < entry.getValue(); i++) {
    			w.getRelative(0,-2,0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
    			
    			if(i == 2) {
    				w.getRelative(0,-1,0).Pillar(2, rand, Material.OAK_LOG);
    				w.getRelative(0,1,0).setType(Material.STONE_SLAB,Material.COBBLESTONE_SLAB,Material.ANDESITE_SLAB);
    			}
    			else
    			{
    				w.get().lsetType(Material.OAK_FENCE);
        			w.CorrectMultipleFacing(1);
        			new OrientableBuilder(Material.OAK_LOG)
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
