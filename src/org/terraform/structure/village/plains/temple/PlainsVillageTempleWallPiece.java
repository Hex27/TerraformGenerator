package org.terraform.structure.village.plains.temple;

import java.util.Random;
import java.util.AbstractMap.SimpleEntry;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.blockdata.StairBuilder;

public class PlainsVillageTempleWallPiece extends JigsawStructurePiece {

	public PlainsVillageTempleWallPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
		super(widthX, height, widthZ, type, validDirs);
		
	}

	
	@Override
	public void build(PopulatorDataAbstract data, Random rand) {
		Material[] stoneBricks = new Material[] {Material.STONE_BRICKS,Material.STONE_BRICKS,Material.STONE_BRICKS,Material.CRACKED_STONE_BRICKS};
		SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
		Wall w = entry.getKey().getRelative(0,-1,0);
		Wall core = null;
		for(int i = 0; i < entry.getValue(); i++) {
			w.getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			w.Pillar(5,rand, stoneBricks);
			if(i == 2) {
				core = w;
				setTempleWindows(w);
			}
			w = w.getLeft();
		}
		

		new StairBuilder(Material.STONE_BRICK_STAIRS)
		.setFacing(core.getDirection().getOppositeFace())
		.apply(core.getFront());
		core.getFront().getRelative(0,-1,0).downUntilSolid(rand, stoneBricks);

		Wall doorAdj = core.getFront().getRight();
		if(!doorAdj.getRelative(0,2,0).getType().isSolid()) {
			new StairBuilder(Material.STONE_BRICK_STAIRS)
			.setFacing(doorAdj.getDirection().getOppositeFace())
			.apply(doorAdj.getRelative(0,2,0));
			doorAdj.getRelative(0,1,0).downUntilSolid(rand, stoneBricks);
		}else {
			doorAdj.getRelative(0,2,0).setType(Material.CHISELED_STONE_BRICKS);
			doorAdj.getRelative(0,3,0).setType(Material.STONE_BRICK_WALL);
		}

		doorAdj = core.getFront().getLeft();
		new StairBuilder(Material.STONE_BRICK_STAIRS)
		.setFacing(doorAdj.getDirection().getOppositeFace())
		.apply(doorAdj.getRelative(0,2,0));
		doorAdj.getRelative(0,1,0).downUntilSolid(rand, stoneBricks);
		
	}

	/**
	 * Windows differ based on Temple direction.
	 * Northern/Southern windows will be stained glass while 
	 * Eastern/Western windows will be trapdoors.
	 * @param w
	 */
	private void setTempleWindows(Wall w) {
		w = w.getRelative(0,1,0);
		new StairBuilder(Material.STONE_BRICK_STAIRS)
		.setFacing(w.getDirection().getOppositeFace())
		.apply(w)
		.setHalf(Half.TOP)
		.apply(w.getRelative(0,3,0));
		w.getRelative(0,1,0).downUntilSolid(new Random(), Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
		
//		if(w.getDirection() == BlockFace.NORTH || w.getDirection() == BlockFace.SOUTH) {
		//Go with yellow because it's the nicer color.
		w.getRelative(0,1,0).Pillar(2,new Random(),Material.YELLOW_STAINED_GLASS_PANE);
		w.getRelative(0,1,0).CorrectMultipleFacing(2);
//		}
//		else
//		{
//			new TrapdoorBuilder(Material.OAK_TRAPDOOR)
//		}
		
	}
}
