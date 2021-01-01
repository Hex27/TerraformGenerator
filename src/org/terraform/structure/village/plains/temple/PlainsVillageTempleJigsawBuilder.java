package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class PlainsVillageTempleJigsawBuilder extends JigsawBuilder {
	
	public PlainsVillageTempleJigsawBuilder(int widthX, int widthZ, PopulatorDataAbstract data, int x, int y, int z) {
		super(widthX, widthZ, data, x, y, z);
		this.pieceRegistry = new JigsawStructurePiece[] {
				new PlainsVillageTempleStandardPiece(5,3,5,JigsawType.STANDARD,BlockUtils.directBlockFaces),
				new PlainsVillageTempleWallPiece(5,3,5,JigsawType.END,BlockUtils.directBlockFaces),
				new PlainsVillageTempleEntrancePiece(5,3,5,JigsawType.ENTRANCE,BlockUtils.directBlockFaces)
		};
		this.chanceToAddNewPiece = 30;
	}
	
	@Override
	public void build(Random random) {
		super.build(random);
		
		//Make sure awkward corners are fixed
		for(JigsawStructurePiece piece:this.pieces.values()) {
			SimpleBlock core = new SimpleBlock(
					this.core.getPopData(),
					piece.getRoom().getX(),
					piece.getRoom().getY(),
					piece.getRoom().getZ());
			Wall target;
			
			if(piece.getWalledFaces().contains(BlockFace.NORTH)
					&&piece.getWalledFaces().contains(BlockFace.WEST)) { //nw
				target = new Wall(core.getRelative(-3, 0, -3));
				decorateAwkwardCorner(target,random,BlockFace.NORTH,BlockFace.WEST);
			}
			if(piece.getWalledFaces().contains(BlockFace.NORTH)
					&&piece.getWalledFaces().contains(BlockFace.EAST)) { //ne
				target = new Wall(core.getRelative(3, 0, -3));
				decorateAwkwardCorner(target,random,BlockFace.NORTH,BlockFace.EAST);
			}
			if(piece.getWalledFaces().contains(BlockFace.SOUTH)
					&&piece.getWalledFaces().contains(BlockFace.WEST)) { //sw
				target = new Wall(core.getRelative(-3, 0, 3));
				decorateAwkwardCorner(target,random,BlockFace.SOUTH,BlockFace.WEST);
			}
			if(piece.getWalledFaces().contains(BlockFace.SOUTH)
					&&piece.getWalledFaces().contains(BlockFace.EAST)) { //se
				target = new Wall(core.getRelative(3, 0, 3));
				decorateAwkwardCorner(target,random,BlockFace.SOUTH,BlockFace.EAST);
			}
		}

		//Place the roof
		if(!PlainsVillageTempleRoofHandler.isRectangle(this))
			PlainsVillageTempleRoofHandler.placeStandardRoof(this);
		else
			PlainsVillageTempleRoofHandler.placeTentRoof(random,this);
		
		//Decorate rooms and walls
		for(JigsawStructurePiece piece:this.pieces.values()) {
			piece.postBuildDecoration(random, this.core.getPopData());
		}
		
	}
	
	public void decorateAwkwardCorner(Wall target, Random random, BlockFace one, BlockFace two) {
		Material[] cobblestone = new Material[] {Material.COBBLESTONE,Material.MOSSY_COBBLESTONE};
		target.Pillar(6,random,cobblestone);
		target.getRelative(0,6,0).Pillar(6,random, Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL);
		target.getRelative(0,-1,0).downUntilSolid(random, cobblestone);
		target = target.getRelative(0,1,0);
		target.getRelative(one).Pillar(3, random, Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL);
		target.getRelative(two).Pillar(3, random, Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL);
		target.getRelative(one).CorrectMultipleFacing(3);
		target.getRelative(two).CorrectMultipleFacing(3);
		target = target.getRelative(0,-1,0);
		target.getRelative(one).downUntilSolid(random, cobblestone);
		target.getRelative(two).downUntilSolid(random, cobblestone);
	}	
}
