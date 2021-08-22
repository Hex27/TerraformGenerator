package org.terraform.structure.pillager.mansion;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;

public abstract class MansionRoomPopulator {

	private HashMap<BlockFace, Boolean> internalWalls;
	private CubeRoom room;
	public MansionRoomPopulator(CubeRoom room, HashMap<BlockFace, Boolean> internalWalls) {
		super();
		this.internalWalls = internalWalls;
		this.room = room;
	}
	
	public abstract void decorateRoom(PopulatorDataAbstract data, Random random);

	public CubeRoom getRoom() {
		return room;
	}
	
	public MansionRoomPopulator getInstance(CubeRoom room, HashMap<BlockFace, Boolean> internalWalls) {
		MansionRoomPopulator pop;
		try {
			pop = (MansionRoomPopulator) this.getClass().getConstructors()[0].newInstance(room, internalWalls);
			return pop;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public HashMap<BlockFace, Boolean> getInternalWalls() {
		return internalWalls;
	}
}
