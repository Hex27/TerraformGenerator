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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void decorateWall(Random rand, Wall w) {
//		w.getRear().Pillar(4, Material.BOOKSHELF);
//		w.getLeft().getRear().Pillar(3, Material.BOOKSHELF);
//		w.getRight().getRear().Pillar(3, Material.BOOKSHELF);
//		w.getLeft(2).getRear().Pillar(4, Material.DARK_OAK_LOG);
//		w.getRight(2).getRear().Pillar(4, Material.DARK_OAK_LOG);
//		
//		new OrientableBuilder(Material.DARK_OAK_LOG)
//		.setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
//		.apply(w.getRear().getRelative(0,5,0));
//		
//		w.getRelative(0,6,0).downPillar(rand, 2, OneOneSixBlockHandler.getChainMaterial());
//		Lantern lantern = (Lantern) Bukkit.createBlockData(Material.LANTERN);
//		lantern.setHanging(true);
//		w.getRelative(0,4,0).setBlockData(lantern);
//		
//		new StairBuilder(Material.DARK_OAK_STAIRS)
//		.setFacing(BlockUtils.getLeft(w.getDirection()))
//		.apply(w.getRear().getRelative(0,3,0).getRight())
//		.apply(w.getRear().getRelative(0,3,0).getRight(3))
//		.apply(w.getRear().getRelative(0,4,0).getRight(2))
//		.setFacing(BlockUtils.getRight(w.getDirection()))
//		.apply(w.getRear().getRelative(0,3,0).getLeft())
//		.apply(w.getRear().getRelative(0,3,0).getLeft(3))
//		.apply(w.getRear().getRelative(0,4,0).getLeft(2));
//
//		new StairBuilder(Material.DARK_OAK_STAIRS)
//		.setFacing(w.getDirection().getOppositeFace())
//		.apply(w.getRear().getRelative(0,4,0).getLeft())
//		.apply(w.getRear().getRelative(0,4,0).getRight());
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
