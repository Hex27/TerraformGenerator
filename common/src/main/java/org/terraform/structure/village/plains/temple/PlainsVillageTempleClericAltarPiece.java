package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Map.Entry;
import java.util.Random;

public class PlainsVillageTempleClericAltarPiece extends PlainsVillageTempleStandardPiece {

	PlainsVillageTempleJigsawBuilder builder;
	public PlainsVillageTempleClericAltarPiece(int widthX, int height, int widthZ, JigsawType type, boolean unique, PlainsVillageTempleJigsawBuilder builder, BlockFace[] validDirs) {
		super(widthX, height, widthZ, type, unique, validDirs);
		this.builder = builder;
	}
	
	private static final Material[] stairTypes = {
			Material.POLISHED_GRANITE_STAIRS,
			Material.POLISHED_ANDESITE_STAIRS,
			Material.POLISHED_DIORITE_STAIRS,
			Material.STONE_BRICK_STAIRS
	};
	
	@Override
	public JigsawStructurePiece getInstance(Random rand, int depth) {
		PlainsVillageTempleClericAltarPiece p = (PlainsVillageTempleClericAltarPiece) super.getInstance(rand, depth);
		p.builder = this.builder;
		return p;
	}
	
	@Override
    public void postBuildDecoration(Random random, PopulatorDataAbstract data) {
        super.postBuildDecoration(random, data);
		
        Material stairType = stairTypes[random.nextInt(stairTypes.length)];
        
        SimpleBlock core = new SimpleBlock(data,this.getRoom().getX(),this.getRoom().getY()+1,this.getRoom().getZ());
        
        BlockFace dir = this.builder.getEntranceDirection();
        
        if(dir == null) 
        	dir = BlockUtils.getDirectBlockFace(random);
        
        core.setType(Material.CHISELED_STONE_BRICKS);
        core.getRelative(0,1,0).setType(Material.BREWING_STAND);
        
        for(Entry<Wall,Integer> entry:this.getRoom().getFourWalls(data, 0).entrySet()) {
        	Wall w = entry.getKey().getRelative(0,-1,0);
        	for(int i = 0; i < entry.getValue(); i++) {
        		w.getRelative(0,1,0).setType(Material.AIR);
        		
        		new StairBuilder(stairType)
        		.setFacing(w.getDirection().getOppositeFace())
        		.setWaterlogged(true)
        		.apply(w);
        		
        		if(!Tag.STAIRS.isTagged(w.getFront().getType())) {
            		w.getFront().getRelative(0,1,0).setType(Material.AIR);
            		w.getFront().setType(Material.WATER);
            		w.getFront().getRelative(0,-1,0).setType(Material.CHISELED_STONE_BRICKS);
            		
            		//Sometimes the pond will have corals.
            		if(random.nextBoolean())
            			w.getFront().setType(CoralGenerator.CORAL_FANS);
        		}
        		
        		w = w.getLeft();
        	}
        }
        
        new StairBuilder(Material.COBBLESTONE_STAIRS,Material.STONE_BRICK_STAIRS)
        .setFacing(dir.getOppositeFace())
        .apply(core.getRelative(dir));
        
        core.getRelative(0,-1,0).setType(Material.STONE_BRICKS);
        core.getRelative(dir).getRelative(0,-1,0).setType(Material.STONE_BRICKS);
        core.getRelative(dir,2).getRelative(0,-1,0).setType(Material.STONE_BRICKS);
        
        for(int corner[]:this.getRoom().getAllCorners()) {
        	data.setType(corner[0], getRoom().getY(), corner[1], Material.CHISELED_STONE_BRICKS);
        	data.setType(corner[0], getRoom().getY()+1, corner[1], Material.LANTERN);
        }
    }

}
