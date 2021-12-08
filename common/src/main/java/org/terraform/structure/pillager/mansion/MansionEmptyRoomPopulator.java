package org.terraform.structure.pillager.mansion;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;

/**
 * Used to mark that a room was considered, but does not need to generate anything.
 * In contrast to roomPopulator = null, the compound room distributor will not
 * overwrite emptyroompopulator.
 *
 */
public class MansionEmptyRoomPopulator extends MansionRoomPopulator {
	
	public MansionEmptyRoomPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
	}

	@Override
	public MansionRoomSize getSize() {
		return new MansionRoomSize(1,1);
	}
	
	

}
