package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Random;

public class PlainsVillageTempleLoungePiece extends PlainsVillageTempleStandardPiece {

	public PlainsVillageTempleLoungePiece(PlainsVillagePopulator plainsVillagePopulator, int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
		super(plainsVillagePopulator, widthX, height, widthZ, type, validDirs);
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
        core.setType(plainsVillagePopulator.woodLog,Material.CRAFTING_TABLE,plainsVillagePopulator.woodPlank);
        core.getRelative(0,1,0).setType(Material.LANTERN,BlockUtils.pickPottedPlant());
    }

}
