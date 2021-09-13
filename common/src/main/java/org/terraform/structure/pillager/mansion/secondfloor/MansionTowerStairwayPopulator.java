package org.terraform.structure.pillager.mansion.secondfloor;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.room.CubeRoom;

public class MansionTowerStairwayPopulator extends MansionRoomPopulator {

	public MansionTowerStairwayPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	@Override
	public void decorateRoom(PopulatorDataAbstract data, Random random) {
		new Wall(this.getRoom().getCenterSimpleBlock(data)).Pillar(5, Material.RED_WOOL);
	}

}
