package org.terraform.structure.pillager.mansion.ground;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSchematicParser;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BannerUtils;
import org.terraform.utils.BlockUtils;

public class MansionWarroomPopulator extends MansionRoomPopulator {

	public MansionWarroomPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	//Refers to the library room width, not the width of one room cell.
	private static final int roomWidth = 15;
	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		int[] lowerBounds = this.getRoom().getLowerCorner(1);
		BlockFace randomFace = BlockUtils.getDirectBlockFace(random);
		//TerraformGeneratorPlugin.logger.info("Library picking face: " + randomFace);
		try {
			if(randomFace == BlockFace.NORTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-warroom", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
				schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.SOUTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidth, this.getRoom().getY(), lowerBounds[1]+roomWidth);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-warroom", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
		        schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.EAST) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidth, this.getRoom().getY(), lowerBounds[1]);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-warroom", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
		        schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.WEST) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]+roomWidth);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-warroom", target);
		        schema.setFace(randomFace);
		        schema.apply();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void decorateWall(Random rand, Wall w) {
		BannerUtils.generatePillagerBanner(w.getLeft().getRelative(0,3,0).get(), w.getDirection(),true);
		BannerUtils.generatePillagerBanner(w.getRight().getRelative(0,3,0).get(), w.getDirection(),true);
	}
	
	@Override
	public void decorateWindow(Random rand, Wall w) {
//		new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
//		.setFacing(w.getDirection().getOppositeFace())
//		.apply(w)
//		.setFacing(BlockUtils.getLeft(w.getDirection()))
//		.setShape(Shape.INNER_RIGHT)
//		.apply(w.getLeft())
//		.setFacing(BlockUtils.getRight(w.getDirection()))
//		.setShape(Shape.INNER_LEFT)
//		.apply(w.getRight());
//		if(rand.nextBoolean()) {
//			w.getLeft().setType(Material.BOOKSHELF);
//			w.getLeft().getRelative(0,1,0).setType(Material.LANTERN);
//		}else if(rand.nextBoolean()) {
//			w.getRight().setType(Material.BOOKSHELF);
//			w.getRight().getRelative(0,1,0).setType(Material.LANTERN);
//		}
	}

}
