package org.terraform.structure.pillager.mansion.ground;

import java.io.FileNotFoundException;
import java.util.Random;

import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;

public class MansionGrandStairwayPopulator extends MansionRoomPopulator {

	public MansionGrandStairwayPopulator(CubeRoom room) {
		super(room);
	}

	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
//		int[] lowerBounds = this.getRoom().getLowerCorner();
//		int[] upperBounds = this.getRoom().getUpperCorner();
//		for(int x = lowerBounds[0]; x <= upperBounds[0]; x++)
//			for(int z = lowerBounds[1]; z <= upperBounds[1]; z++)
//				data.setType(x,this.getRoom().getY(), z, Material.PINK_WOOL);
		SimpleBlock target = this.getRoom().getCenterSimpleBlock(data);
		try {
			TerraSchematic schema = TerraSchematic.load("mansion/mansion-stairway", target);
	        //schema.parser = new OreLiftSchematicParser();
	        schema.setFace(BlockUtils.getDirectBlockFace(random));
	        schema.apply();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
