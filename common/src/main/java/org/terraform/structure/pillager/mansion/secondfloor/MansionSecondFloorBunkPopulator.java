package org.terraform.structure.pillager.mansion.secondfloor;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSchematicParser;
import org.terraform.structure.pillager.mansion.MansionRoomSize;
import org.terraform.structure.room.CubeRoom;

public class MansionSecondFloorBunkPopulator extends MansionRoomPopulator {

	public MansionSecondFloorBunkPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	//Refers to the kitchen room width, not the width of one room cell.
	private static final int roomWidthX = 15;
	private static final int roomWidthZ = 6;
	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {

		int[] lowerBounds = this.getRoom().getLowerCorner(1);
		BlockFace randomFace = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH}[random.nextInt(2)];
		//TerraformGeneratorPlugin.logger.info("Library picking face: " + randomFace);
		try {
			if(randomFace == BlockFace.NORTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-bunk", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
				schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.SOUTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidthX, this.getRoom().getY(), lowerBounds[1]+roomWidthZ);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-bunk", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
		        schema.setFace(randomFace);
		        schema.apply();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	@Override
	public void decorateExit(Random rand, Wall w) {
		
	}

	@Override
	public void decorateWindow(Random rand, Wall w) {
		
	}
	
	@Override
	public void decorateWall(Random rand, Wall w) {
		
	}


	@Override
	public MansionRoomSize getSize() {
		return new MansionRoomSize(2,1);
	}
}
