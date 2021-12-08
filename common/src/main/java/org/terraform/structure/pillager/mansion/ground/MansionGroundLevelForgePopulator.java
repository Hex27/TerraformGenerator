package org.terraform.structure.pillager.mansion.ground;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs.Shape;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSchematicParser;
import org.terraform.structure.pillager.mansion.MansionRoomSize;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.ArmorStandUtils;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

public class MansionGroundLevelForgePopulator extends MansionRoomPopulator {

	public MansionGroundLevelForgePopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	//Refers to the kitchen room width, not the width of one room cell.
	private static final int roomWidthX = 6;
	private static final int roomWidthZ = 15;
	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		int[] lowerBounds = this.getRoom().getLowerCorner(1);
		BlockFace randomFace = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH}[random.nextInt(2)];
		//TerraformGeneratorPlugin.logger.info("Mushroom Farm at " + this.getRoom().getSimpleLocation() + " picking face: " + randomFace);
		try {
			if(randomFace == BlockFace.NORTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-forge", target);
		        schema.setFace(randomFace);
		        schema.parser = new MansionForgeSchematicParser(random, data);
		        schema.apply();
			}else if(randomFace == BlockFace.SOUTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidthX, this.getRoom().getY(), lowerBounds[1] + roomWidthZ);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-forge", target);
		        schema.setFace(randomFace);
		        schema.parser = new MansionForgeSchematicParser(random, data);
		        schema.apply();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void decorateExit(Random rand, Wall w) {
		
	}
	@Override
	public void decorateWindow(Random rand, Wall w) {
		new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
		.setHalf(Half.TOP).setFacing(w.getDirection().getOppositeFace())
		.apply(w.getLeft())
		.apply(w.getRight())
		.setShape(Shape.OUTER_RIGHT)
		.apply(w.getLeft(2))
		.setShape(Shape.OUTER_LEFT)
		.apply(w.getRight(2));
		
		w.setType(Material.FLETCHING_TABLE,
				Material.SMITHING_TABLE);
	}
	
	@Override
	public void decorateWall(Random rand, Wall w) {
		
		new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
		.setHalf(Half.TOP)
		.setFacing(BlockUtils.getLeft(w.getDirection()))
		.apply(w.getLeft(3))
		.setFacing(BlockUtils.getRight(w.getDirection()))
		.apply(w.getRight(3));
		
		new SlabBuilder(Material.POLISHED_ANDESITE_SLAB)
		.setType(Type.TOP)
		.apply(w)
		.apply(w.getLeft())
		.apply(w.getLeft(2))
		.apply(w.getRight())
		.apply(w.getRight(2));
		
		ArmorStandUtils.placeArmorStand(w.getRelative(0,2,0).get(), w.getDirection(), rand);
		ArmorStandUtils.placeArmorStand(w.getRelative(0,2,0).getLeft(2).get(), w.getDirection(), rand);
		ArmorStandUtils.placeArmorStand(w.getRelative(0,2,0).getRight(2).get(), w.getDirection(), rand);
		
	}
	
	private class MansionForgeSchematicParser extends MansionRoomSchematicParser
	{
		public MansionForgeSchematicParser(Random rand, PopulatorDataAbstract pop) {
			super(rand, pop);
		}
		
	    @Override
	    public void applyData(SimpleBlock block, BlockData data) {
            super.applyData(block, data);
	        if (data.getMaterial() == Material.POLISHED_DIORITE) {
	            ArmorStandUtils.placeArmorStand(block.getRelative(0,1,0), BlockUtils.getDirectBlockFace(rand), rand);
	        }
	    }
	}

	@Override
	public MansionRoomSize getSize() {
		return new MansionRoomSize(1,2);
	}
}
