package org.terraform.structure.pillager.mansion.ground;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSchematicParser;
import org.terraform.structure.room.CubeRoom;

public class MansionGroundLevelKitchenPopulator extends MansionRoomPopulator {

	public MansionGroundLevelKitchenPopulator(CubeRoom room, HashMap<BlockFace, Boolean> internalWalls) {
		super(room, internalWalls);
	}

	//Refers to the kitchen room width, not the width of one room cell.
	private static final int roomWidthX = 6;
	private static final int roomWidthZ = 15;
	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		int[] lowerBounds = this.getRoom().getLowerCorner(1);
		BlockFace randomFace = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH}[random.nextInt(2)];
		TerraformGeneratorPlugin.logger.info("Kitchen at " + this.getRoom().getSimpleLocation() + " picking face: " + randomFace);
		try {
			if(randomFace == BlockFace.NORTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]+roomWidthZ);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-kitchen", target);
		        schema.setFace(randomFace);
		        schema.parser = new MansionKitchenSchematicParser(random, data);
		        schema.apply();
			}else if(randomFace == BlockFace.SOUTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidthX, this.getRoom().getY(), lowerBounds[1]);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-kitchen", target);
		        schema.setFace(randomFace);
		        schema.parser = new MansionKitchenSchematicParser(random, data);
		        schema.apply();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private class MansionKitchenSchematicParser extends MansionRoomSchematicParser
	{
		public MansionKitchenSchematicParser(Random rand, PopulatorDataAbstract pop) {
			super(rand, pop);
		}
		
	    @Override
	    public void applyData(SimpleBlock block, BlockData data) {
	        if (data.getMaterial() == Material.MELON) {
	            block.setType(
            		Material.MELON,
            		Material.PUMPKIN,
            		Material.HAY_BLOCK,
            		Material.DRIED_KELP_BLOCK
            		);
	        } else {
	            super.applyData(block, data);
	        }
	    }
	}
	

}
