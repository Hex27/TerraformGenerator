package org.terraform.structure.pillager.mansion.ground;

import java.util.Random;

import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.room.CubeRoom;

public class MansionIndoorsFarmPopulator extends MansionRoomPopulator {

	public MansionIndoorsFarmPopulator(CubeRoom room) {
		super(room);
	}

	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		int[] lowerBounds = this.getRoom().getLowerCorner();
		int[] upperBounds = this.getRoom().getUpperCorner();
		for(int x = lowerBounds[0]; x <= upperBounds[0]; x++)
			for(int z = lowerBounds[1]; z <= upperBounds[1]; z++)
				data.setType(x,this.getRoom().getY(), z, Material.GREEN_WOOL);
	
	}

}
