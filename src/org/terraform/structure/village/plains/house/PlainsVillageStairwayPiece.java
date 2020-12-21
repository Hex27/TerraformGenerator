package org.terraform.structure.village.plains.house;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;

public class PlainsVillageStairwayPiece extends JigsawStructurePiece {

	public PlainsVillageStairwayPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
		super(widthX, height, widthZ, type, validDirs);
		
	}

	@Override
	public void build(PopulatorDataAbstract data, Random rand) {
		this.getRoom().fillRoom(data, new Material[] {Material.YELLOW_STAINED_GLASS});
	}

}
