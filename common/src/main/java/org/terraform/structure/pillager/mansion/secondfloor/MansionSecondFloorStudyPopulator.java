package org.terraform.structure.pillager.mansion.secondfloor;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Lantern;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSchematicParser;
import org.terraform.structure.pillager.mansion.MansionRoomSize;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.version.OneOneSixBlockHandler;

public class MansionSecondFloorStudyPopulator extends MansionRoomPopulator {

	public MansionSecondFloorStudyPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	//Refers to the bedroom room width, not the width of one room cell.
	private static final int roomWidth = 15;
	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		int[] lowerBounds = this.getRoom().getLowerCorner(1);
		BlockFace randomFace = BlockUtils.getDirectBlockFace(random);
		//TerraformGeneratorPlugin.logger.info("Library picking face: " + randomFace);
		try {
			if(randomFace == BlockFace.NORTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-study", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
				schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.SOUTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidth, this.getRoom().getY(), lowerBounds[1]+roomWidth);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-study", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
		        schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.EAST) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidth, this.getRoom().getY(), lowerBounds[1]);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-study", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
		        schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.WEST) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]+roomWidth);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-study", target);
				schema.parser = new MansionRoomSchematicParser(random, data);
		        schema.setFace(randomFace);
		        schema.apply();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void decorateEntrance(Random rand, Wall w) {
		w = w.getRear();
		w.getLeft(2).Pillar(5, Material.DARK_OAK_PLANKS);
		w.getRight(2).Pillar(5, Material.DARK_OAK_PLANKS);
		w.getLeft(2).Pillar(3, Material.DARK_OAK_LOG);
		w.getRight(2).Pillar(3, Material.DARK_OAK_LOG);
		
		w.getLeft().getRelative(0,5,0).downPillar(2, Material.DARK_OAK_PLANKS);
		w.getRight().getRelative(0,5,0).downPillar(2, Material.DARK_OAK_PLANKS);
		w.getRelative(0,5,0).downPillar(2, Material.DARK_OAK_PLANKS);
		new StairBuilder(Material.DARK_OAK_STAIRS)
		.setFacing(w.getDirection()).setHalf(Half.TOP)
		.apply(w.getRelative(0,4,0))
		.setFacing(BlockUtils.getLeft(w.getDirection()))
		.apply(w.getRelative(0,3,0).getLeft())
		.setFacing(BlockUtils.getRight(w.getDirection()))
		.apply(w.getRelative(0,3,0).getRight());
	}
	
	@Override
	public void decorateWall(Random rand, Wall w) {
		w.getRear().Pillar(4, Material.BOOKSHELF);
		w.getLeft().getRear().Pillar(3, Material.BOOKSHELF);
		w.getRight().getRear().Pillar(3, Material.BOOKSHELF);
		w.getLeft(2).getRear().Pillar(4, Material.DARK_OAK_LOG);
		w.getRight(2).getRear().Pillar(4, Material.DARK_OAK_LOG);
		
		new OrientableBuilder(Material.DARK_OAK_LOG)
		.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
		.apply(w.getRear().getRelative(0,5,0));
		
		w.getRelative(0,5,0).downPillar(rand, 2, OneOneSixBlockHandler.getChainMaterial());
		Lantern lantern = (Lantern) Bukkit.createBlockData(Material.LANTERN);
		lantern.setHanging(true);
		w.getRelative(0,3,0).setBlockData(lantern);
		
		new StairBuilder(Material.DARK_OAK_STAIRS)
		.setFacing(BlockUtils.getLeft(w.getDirection()))
		.apply(w.getRear().getRelative(0,3,0).getRight())
		.apply(w.getRear().getRelative(0,3,0).getRight(3))
		.apply(w.getRear().getRelative(0,4,0).getRight(2))
		.setFacing(BlockUtils.getRight(w.getDirection()))
		.apply(w.getRear().getRelative(0,3,0).getLeft())
		.apply(w.getRear().getRelative(0,3,0).getLeft(3))
		.apply(w.getRear().getRelative(0,4,0).getLeft(2));

		new StairBuilder(Material.DARK_OAK_STAIRS)
		.setFacing(w.getDirection().getOppositeFace())
		.apply(w.getRear().getRelative(0,4,0).getLeft())
		.apply(w.getRear().getRelative(0,4,0).getRight());
	}
	
	@Override
	public void decorateWindow(Random rand, Wall w) {
		clearRoguePillar(w);
	}
	
	@Override
	public void decorateExit(Random rand, Wall w) { 
		clearRoguePillar(w);
	}
	
	public void clearRoguePillar(Wall base) {
		Entry<Wall,Integer> entry = this.getRoom().getWall(base.get().getPopData(), base.getDirection().getOppositeFace(), 0);
		Wall w = entry.getKey();
		for(int i = 0; i < entry.getValue(); i++) {
			if(!w.getType().isSolid()) w.setType(Material.RED_CARPET);
			if(w.getFront().getType() == Material.POLISHED_ANDESITE) {
				w.getFront().Pillar(6, Material.AIR);
				w.getFront().setType(Material.RED_CARPET);
				Wall target = w.getFront().getRelative(0,5,0);
				for(BlockFace face:BlockUtils.directBlockFaces) {
					if(target.getRelative(face).getType() == Material.DARK_OAK_STAIRS
							|| target.getRelative(face).getType() == Material.DARK_OAK_SLAB) {
						target.getRelative(face).setType(Material.AIR);
						target.getDown().getRelative(face).setType(Material.AIR);
						target.getRelative(face,2).setType(Material.AIR);
					}
				}
			}
			w = w.getLeft();
		}
	}

	@Override
	public MansionRoomSize getSize() {
		return new MansionRoomSize(2,2);
	}
}
