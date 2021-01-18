package org.terraform.structure.village.plains.temple;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.StairBuilder;

public class PlainsVillageTempleLoungePiece extends PlainsVillageTempleStandardPiece {

	public PlainsVillageTempleLoungePiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
		super(widthX, height, widthZ, type, validDirs);
	}
	
	private static final Material[] stairTypes = {
			Material.POLISHED_GRANITE_STAIRS,
			Material.POLISHED_ANDESITE_STAIRS,
			Material.POLISHED_DIORITE_STAIRS
	};
	
	@Override
    public void postBuildDecoration(Random random, PopulatorDataAbstract data) {
        super.postBuildDecoration(random, data);
		
        Material stairType = stairTypes[random.nextInt(stairTypes.length)];
        
        SimpleBlock core = new SimpleBlock(data,this.getRoom().getX(),this.getRoom().getY()+1,this.getRoom().getZ());
        
        for(BlockFace face:BlockUtils.getRandomBlockfaceAxis(random)) {

        	new StairBuilder(stairType)
        	.setFacing(face)
        	.apply(core.getRelative(face).getRelative(BlockUtils.getAdjacentFaces(face)[0]))
        	.apply(core.getRelative(face).getRelative(BlockUtils.getAdjacentFaces(face)[1]));
        
        }
        core.setType(Material.OAK_LOG,Material.CRAFTING_TABLE,Material.OAK_PLANKS);
        core.getRelative(0,1,0).setType(Material.LANTERN,BlockUtils.pickPottedPlant());
    }

}
