package org.terraform.structure.village.plains.forge;

import org.bukkit.block.BlockFace;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.structure.village.plains.forge.PlainsVillageForgeWallPiece.PlainsVillageForgeWallType;

public abstract class PlainsVillageForgePiece extends JigsawStructurePiece {

	protected PlainsVillagePopulator plainsVillagePopulator;
	private PlainsVillageForgeWallType wallType = null;
	
	public PlainsVillageForgePiece(PlainsVillagePopulator plainsVillagePopulator, int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
		super(widthX, height, widthZ, type, validDirs);
		this.plainsVillagePopulator = plainsVillagePopulator;
	}

	public PlainsVillageForgePiece(PlainsVillagePopulator plainsVillagePopulator, int widthX, int height, int widthZ, JigsawType type, boolean unique, BlockFace... validDirs) {
	    	super(widthX, height, widthZ, type, unique, validDirs);
	    	this.plainsVillagePopulator = plainsVillagePopulator;
	}

	public PlainsVillageForgeWallType getWallType() {
		return wallType;
	}

	public void setWallType(PlainsVillageForgeWallType wallType) {
		this.wallType = wallType;
	}
	

}
