package org.terraform.structure.pillager.mansion.tower;

import java.util.Random;
import java.util.AbstractMap.SimpleEntry;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

public class MansionLookoutTowerWallPiece extends MansionTowerWallPiece {

	public MansionLookoutTowerWallPiece(MansionJigsawBuilder builder, int widthX, int height, int widthZ, JigsawType type,
			BlockFace[] validDirs) {
		super(builder, widthX, height, widthZ, type, validDirs);
	}
	
    @Override
    public void build(PopulatorDataAbstract data, Random rand) {
    	super.build(data, rand);
    }
    
    @Override
    public void postBuildDecoration(Random rand, PopulatorDataAbstract data) {
    	
        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);

    	Wall w = entry.getKey().getRelative(0, -1, 0);

    	
    	//Carving
        for (int i = 0; i < entry.getValue(); i++) {
            
        	//sides
        	if(i == 0 || i == entry.getValue()-1) {
        		w.getRelative(0,1,0).Pillar(8, Material.DARK_OAK_LOG);
        		new SlabBuilder(Material.STONE_BRICK_SLAB)
        		.setType(Type.TOP)
        		.apply(w.getFront().getRelative(0,4,0));
        		
        	}else if(i == 1 || i == entry.getValue()-2) {
        		w.getRelative(0,1,0).Pillar(3, Material.AIR);
        		
        		new StairBuilder(Material.STONE_BRICK_STAIRS)
        		.setHalf(Half.TOP).setFacing(w.getDirection().getOppositeFace())
        		.apply(w.getFront());
        		
        		new OrientableBuilder(Material.STRIPPED_DARK_OAK_LOG)
        		.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
        		.apply(w.getRelative(0,1,0))
        		.apply(w.getRelative(0,1,0).getFront());
        		
        		w.getRelative(0,2,0).getFront().Pillar(3, Material.STONE_BRICK_WALL);
        		w.getRelative(0,2,0).getFront().CorrectMultipleFacing(3);
        		
        		w.getRelative(0,5,0).getFront().setType(Material.STONE_BRICK_SLAB);
        		
        	}else if(i == 2 || i == entry.getValue()-3) {
        		w.getRelative(0,1,0).Pillar(4, Material.AIR);

        		new StairBuilder(Material.STONE_BRICK_STAIRS)
        		.setHalf(Half.TOP).setFacing(w.getDirection().getOppositeFace())
        		.apply(w.getRelative(0,1,0).getFront());
        		new DirectionalBuilder(Material.DARK_OAK_FENCE_GATE)
        		.setFacing(w.getDirection())
        		.apply(w.getRelative(0,2,0).getFront());
        		w.getRelative(0,2,0).getFront().CorrectMultipleFacing(1);

        		new SlabBuilder(Material.STONE_BRICK_SLAB)
        		.setType(Type.TOP)
        		.apply(w.getFront().getRelative(0,5,0));

        		new StairBuilder(Material.DARK_OAK_STAIRS)
        		.setFacing(w.getDirection())
        		.apply(w.getRelative(0,1,0));
        	}else { //center
        		w.getRelative(0,1,0).Pillar(5, Material.AIR);

        		new StairBuilder(Material.STONE_BRICK_STAIRS)
        		.setHalf(Half.TOP).setFacing(w.getDirection().getOppositeFace())
        		.apply(w.getRelative(0,1,0).getFront());
        		w.getRelative(0,2,0).getFront().setType(Material.STONE_BRICK_WALL);
        		w.getRelative(0,2,0).getFront().CorrectMultipleFacing(1);

        		new StairBuilder(Material.DARK_OAK_STAIRS)
        		.setFacing(w.getDirection())
        		.apply(w.getRelative(0,1,0));
        		
        		//Edit stairs on the left and right

        		new StairBuilder(Material.DARK_OAK_STAIRS)
        		.setHalf(Half.TOP)
        		.setFacing(BlockUtils.getLeft(w.getDirection()))
        		.apply(w.getRelative(0,5,0).getLeft())
        		.apply(w.getRelative(0,4,0).getLeft(2))
        		.setFacing(BlockUtils.getRight(w.getDirection()))
        		.apply(w.getRelative(0,5,0).getRight())
        		.apply(w.getRelative(0,4,0).getRight(2));

        		w.getRelative(0,6,0).getFront().setType(Material.STONE_BRICK_SLAB);
        	}
        	
        	
            w = w.getLeft();
        }    
    }
    

}
