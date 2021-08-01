package org.terraform.structure.pillager.mansion.tower;

import java.util.Random;
import java.util.AbstractMap.SimpleEntry;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.ground.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

public class MansionBaseTowerWallPiece extends MansionTowerWallPiece {

	public MansionBaseTowerWallPiece(MansionJigsawBuilder builder, int widthX, int height, int widthZ, JigsawType type,
			BlockFace[] validDirs) {
		super(builder, widthX, height, widthZ, type, validDirs);
		// TODO Auto-generated constructor stub
	}
	
    @Override
    public void build(PopulatorDataAbstract data, Random rand) {

        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getRelative(0, -1, 0);
        
        for (int i = 0; i < entry.getValue(); i++) {
            
        	//Primary Wall and wooden stair decorations
        	//w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.getRelative(0, 1, 0).Pillar(this.getRoom().getHeight(), rand, Material.DARK_OAK_PLANKS);
            new StairBuilder(Material.COBBLESTONE_STAIRS)
            .setFacing(w.getDirection().getOppositeFace())
            .apply(w.getRelative(0,1,0).getFront());
            
            w = w.getLeft();
        }
    }
    
    @Override
    public void postBuildDecoration(Random rand, PopulatorDataAbstract data) {
    	
        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);

    	Wall w = entry.getKey().getRelative(0, -1, 0);

    	
    	OrientableBuilder logBuilder = new OrientableBuilder(Material.DARK_OAK_LOG).setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getRight(w.getDirection())));
    	//Carving
        for (int i = 0; i < entry.getValue(); i++) {
            
        	w.getRear().getRelative(0,1,0).Pillar(6,Material.STONE);
        	logBuilder.apply(w.getRelative(0,7,0));
        	
        	//Decorative Roofing & carving
        	if(i == 0 || i == entry.getValue()-1) {
        		new SlabBuilder(Material.COBBLESTONE_SLAB)
        		.setType(Type.TOP)
        		.apply(w.getRelative(0,5,0).getFront());

        		new StairBuilder(Material.DARK_OAK_STAIRS)
        		.setHalf(Half.TOP)
        		.setFacing(i > 0 ? BlockUtils.getLeft(w.getDirection()) : BlockUtils.getRight(w.getDirection()))
        		.apply(w.getRelative(0,3,0));
        	}
        	else if(i == 1 || i == entry.getValue()-2) {
        		new StairBuilder(Material.COBBLESTONE_STAIRS)
        		.setFacing(i > 2 ? BlockUtils.getRight(w.getDirection()) : BlockUtils.getLeft(w.getDirection()))
        		.apply(w.getRelative(0,6,0).getFront());
        		
        		w.getRelative(0,3,0).setType(Material.AIR);
        	}
        	else if(i == 2 || i == entry.getValue()-3) {
        		new SlabBuilder(Material.COBBLESTONE_SLAB)
        		.setType(Type.TOP)
        		.apply(w.getRelative(0,6,0).getFront());

        		new SlabBuilder(Material.DARK_OAK_SLAB)
        		.setType(Type.TOP)
        		.apply(w.getRelative(0,5,0));
        	}
        	else{ //center
        		new SlabBuilder(Material.COBBLESTONE_SLAB)
        		.setType(Type.BOTTOM)
        		.apply(w.getRelative(0,7,0).getFront());

        		
        		w.getRelative(0,5,0).setType(Material.AIR);
        		new SlabBuilder(Material.DARK_OAK_SLAB)
        		.setType(Type.TOP)
        		.apply(w.getRelative(0,4,0));
        		w.getRelative(0,3,0).setType(Material.AIR);
        	}
        	
            w = w.getLeft();
        }    
    }
    

}
