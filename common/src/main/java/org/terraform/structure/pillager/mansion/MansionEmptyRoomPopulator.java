package org.terraform.structure.pillager.mansion;

import java.util.Random;

import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;

/**
 * Used to mark that a room was considered, but does not need to generate anything.
 * In contrast to roomPopulator = null, the compound room distributor will not
 * overwrite emptyroompopulator.
 *
 */
public class MansionEmptyRoomPopulator extends MansionRoomPopulator {
	
	public MansionEmptyRoomPopulator(CubeRoom room) {
		super(room);
	}

	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
	}

}
