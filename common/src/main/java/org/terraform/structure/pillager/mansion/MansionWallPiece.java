package org.terraform.structure.pillager.mansion;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class MansionWallPiece extends JigsawStructurePiece {

    public MansionWallPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);

    }

    @Override
    public void build(PopulatorDataAbstract data, Random rand) {

        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getRelative(0, -1, 0);
        
        for (int i = 0; i < entry.getValue(); i++) {
            
        	//Primary Wall and ground beneath wall
        	w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.Pillar(1, rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.getRelative(0, 1, 0).Pillar(this.getRoom().getHeight(), rand, Material.DARK_OAK_PLANKS);
            
            if(i == (entry.getValue()/2)) {
            	spawnWallSupportingPillar(w.getFront().getRelative(0,1,0), this.getRoom().getHeight());
            }
            
            w = w.getLeft();
        }
    }
    
    private void spawnWallSupportingPillar(Wall w, int height) {
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
    	.setHalf(Half.TOP)
    	.setFacing(w.getDirection().getOppositeFace())
    	.apply(w.getRelative(0,height-1,0).getFront());
    	
    	new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
    	.setHalf(Half.TOP)
    	.setFacing(BlockUtils.getRight(w.getDirection()))
    	.apply(w.getRelative(0,height-1,0).getRelative(BlockUtils.getLeft(w.getDirection())));
    	
    	new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
    	.setHalf(Half.TOP)
    	.setFacing(BlockUtils.getLeft(w.getDirection()))
    	.apply(w.getRelative(0,height-1,0).getRelative(BlockUtils.getRight(w.getDirection())));

    	w.getRelative(0,2,0).setType(Material.STONE_BRICK_WALL);
    	w.getRelative(0,3,0).setType(Material.POLISHED_DIORITE);
    	w.getRelative(0,4,0).setType(Material.STONE_BRICK_WALL);
    	w.getRelative(0,2,0).CorrectMultipleFacing(3);
    }

}
