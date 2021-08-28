package org.terraform.structure.pillager.mansion.ground;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;

public class MansionGrandStairwayPopulator extends MansionRoomPopulator {

	public MansionGrandStairwayPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		SimpleBlock target = this.getRoom().getCenterSimpleBlock(data);
		try {
			TerraSchematic schema = TerraSchematic.load("mansion/mansion-stairway", target);
	        //schema.parser = new MansionRoomSchematicParser();
	        schema.setFace(BlockUtils.getDirectBlockFace(random));
	        schema.apply();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
