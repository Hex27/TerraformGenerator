package org.terraform.structure.village.plains.house;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;

import java.util.Random;

public class PlainsVillageWallPiece extends JigsawStructurePiece {

	public PlainsVillageWallPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
		super(widthX, height, widthZ, type, validDirs);
		
	}

	@Override
	public void build(PopulatorDataAbstract data, Random rand) {
		
		int[][] corners = this.getRoom()
				.getCornersAlongFace(getRotation().getOppositeFace(), -1);
		
		for(int x = corners[0][0]; x <= corners[1][0]; x++)
			for(int z = corners[0][1]; z <= corners[1][1]; z++) {
				new Wall(new SimpleBlock(data,x,this.getRoom().getY(),z))
				.Pillar(5,rand,Material.PURPLE_STAINED_GLASS);
			}
	}

}
