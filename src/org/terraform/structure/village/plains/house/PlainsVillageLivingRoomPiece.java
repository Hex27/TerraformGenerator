package org.terraform.structure.village.plains.house;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;

import java.util.Random;

public class PlainsVillageLivingRoomPiece extends JigsawStructurePiece {

	public PlainsVillageLivingRoomPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
		super(widthX, height, widthZ, type, validDirs);
		
	}

	@Override
	public void build(PopulatorDataAbstract data, Random rand) {
		this.getRoom().fillRoom(data, new Material[] {Material.RED_STAINED_GLASS});
	}

}
