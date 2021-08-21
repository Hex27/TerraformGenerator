package org.terraform.structure.pillager.mansion;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;

public abstract class MansionRoomPopulator {

	private CubeRoom room;
	public MansionRoomPopulator(CubeRoom room) {
		super();
		this.room = room;
	}
	
	public abstract void decorateRoom(PopulatorDataAbstract data, Random random);

	public CubeRoom getRoom() {
		return room;
	}
	
	public MansionRoomPopulator getInstance(CubeRoom room) {
		MansionRoomPopulator pop;
		try {
			pop = (MansionRoomPopulator) this.getClass().getConstructors()[0].newInstance(room);
			return pop;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
}
