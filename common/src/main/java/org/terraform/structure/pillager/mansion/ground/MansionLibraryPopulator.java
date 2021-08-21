package org.terraform.structure.pillager.mansion.ground;

import java.util.Random;

import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.room.CubeRoom;

public class MansionLibraryPopulator extends MansionRoomPopulator {

	public MansionLibraryPopulator(CubeRoom room) {
		super(room);
	}

	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		int[] lowerBounds = this.getRoom().getLowerCorner();
		
	}

}
