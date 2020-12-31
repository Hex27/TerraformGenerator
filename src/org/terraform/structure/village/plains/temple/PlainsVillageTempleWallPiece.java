package org.terraform.structure.village.plains.temple;

import java.util.Random;
import java.util.AbstractMap.SimpleEntry;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;

public class PlainsVillageTempleWallPiece extends JigsawStructurePiece {

	public PlainsVillageTempleWallPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
		super(widthX, height, widthZ, type, validDirs);
		
	}

	@Override
	public void build(PopulatorDataAbstract data, Random rand) {
		SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
		Wall w = entry.getKey().getRelative(0,-1,0);
		for(int i = 0; i < entry.getValue(); i++) {
			w.getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			w.Pillar(2,rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			
			w.getRelative(0,2,0).Pillar(2, rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			
			w = w.getLeft();
		}
	}

}
