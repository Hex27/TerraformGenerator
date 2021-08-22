package org.terraform.structure.pillager.mansion.ground;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSchematicParser;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;

public class MansionLibraryPopulator extends MansionRoomPopulator {

	public MansionLibraryPopulator(CubeRoom room, HashMap<BlockFace, Boolean> internalWalls) {
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
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-library", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
				schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.SOUTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidth, this.getRoom().getY(), lowerBounds[1]+roomWidth);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-library", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
		        schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.EAST) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidth, this.getRoom().getY(), lowerBounds[1]);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-library", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
		        schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.WEST) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]+roomWidth);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-library", target);
		        schema.setFace(randomFace);
		        schema.apply();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
