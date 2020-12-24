package org.terraform.structure.village.plains.house;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;

public class PlainsVillageHouseJigsawBuilder extends JigsawBuilder {
	PlainsVillageHouseVariant var;
	public PlainsVillageHouseJigsawBuilder(int widthX, int widthZ, PopulatorDataAbstract data, int x, int y, int z) {
		super(widthX, widthZ, data, x, y, z);
		this.var = PlainsVillageHouseVariant.roll(new Random());
		this.pieceRegistry = new JigsawStructurePiece[] {
				new PlainsVillageBedroomPiece(var,5,3,5,JigsawType.STANDARD,BlockUtils.directBlockFaces),
				new PlainsVillageKitchenPiece(var,5,3,5,JigsawType.STANDARD,BlockUtils.directBlockFaces),
				new PlainsVillageLibraryPiece(var,5,3,5,JigsawType.STANDARD,BlockUtils.directBlockFaces),
				new PlainsVillageWallPiece(var,5,3,5,JigsawType.END,BlockUtils.directBlockFaces),
				new PlainsVillageEntrancePiece(var,5,3,5,JigsawType.ENTRANCE,BlockUtils.directBlockFaces)
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
			Material[] fenceType = new Material[] {Material.OAK_FENCE};
			Material cornerType = Material.OAK_LOG;
			if(this.var == PlainsVillageHouseVariant.COBBLESTONE)
				fenceType = new Material[] {Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL};
			else if(this.var == PlainsVillageHouseVariant.CLAY) {
				fenceType = new Material[] {Material.STONE_BRICK_WALL,Material.MOSSY_STONE_BRICK_WALL};
				cornerType = Material.STRIPPED_OAK_LOG;
			}
			if(piece.getWalledFaces().contains(BlockFace.NORTH)
					&&piece.getWalledFaces().contains(BlockFace.WEST)) { //nw
				target = new Wall(core.getRelative(-3, 0, -3));
				decorateAwkwardCorner(target,random,BlockFace.NORTH,BlockFace.WEST, cornerType, fenceType);
			}
			if(piece.getWalledFaces().contains(BlockFace.NORTH)
					&&piece.getWalledFaces().contains(BlockFace.EAST)) { //ne
				target = new Wall(core.getRelative(3, 0, -3));
				decorateAwkwardCorner(target,random,BlockFace.NORTH,BlockFace.EAST, cornerType, fenceType);
			}
			if(piece.getWalledFaces().contains(BlockFace.SOUTH)
					&&piece.getWalledFaces().contains(BlockFace.WEST)) { //sw
				target = new Wall(core.getRelative(-3, 0, 3));
				decorateAwkwardCorner(target,random,BlockFace.SOUTH,BlockFace.WEST, cornerType, fenceType);
			}
			if(piece.getWalledFaces().contains(BlockFace.SOUTH)
					&&piece.getWalledFaces().contains(BlockFace.EAST)) { //se
				target = new Wall(core.getRelative(3, 0, 3));
				decorateAwkwardCorner(target,random,BlockFace.SOUTH,BlockFace.EAST, cornerType, fenceType);
			}
		}

		//Place the roof
		if(!PlainsVillageRoofHandler.isRectangle(this))
			PlainsVillageRoofHandler.placeStandardRoof(this);
		else
			PlainsVillageRoofHandler.placeTentRoof(random,this);
		
		//Decorate rooms and walls
		for(JigsawStructurePiece piece:this.pieces.values()) {
			piece.postBuildDecoration(random, this.core.getPopData());
		}
		
	}
	
	public void decorateAwkwardCorner(Wall target, Random random, BlockFace one, BlockFace two, Material cornerType, Material[] fenceType) {
		target.Pillar(4,random,cornerType);
		target.getRelative(0,-1,0).downUntilSolid(random, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
		target = target.getRelative(0,1,0);
		target.getRelative(one).Pillar(3, random, fenceType);
		target.getRelative(two).Pillar(3, random, fenceType);
		target.getRelative(one).CorrectMultipleFacing(3);
		target.getRelative(two).CorrectMultipleFacing(3);
		target = target.getRelative(0,-1,0);
		target.getRelative(one).downUntilSolid(random, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
		target.getRelative(two).downUntilSolid(random, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
	}
	
}
