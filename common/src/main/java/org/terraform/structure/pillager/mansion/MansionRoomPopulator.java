package org.terraform.structure.pillager.mansion;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;

public abstract class MansionRoomPopulator {

	private HashMap<BlockFace, MansionInternalWallState> internalWalls;
	private CubeRoom room;
	public MansionRoomPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super();
		this.internalWalls = internalWalls;
		this.room = room;
	}
	
	public abstract void decorateRoom(PopulatorDataAbstract data, Random random);

	public CubeRoom getRoom() {
		return room;
	}
	
	public MansionRoomPopulator getInstance(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		MansionRoomPopulator pop;
		try {
			pop = (MansionRoomPopulator) this.getClass().getConstructors()[0].newInstance(room, internalWalls);
			return pop;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void decorateWall(Random rand, Wall w) {};
	public void decorateWindow(Random rand, Wall w) {};
	public void decorateEntrance(Random rand, Wall w) {};
	public void decorateExit(Random rand, Wall w) {};

	public HashMap<BlockFace, MansionInternalWallState> getInternalWalls() {
		return internalWalls;
	}
	
	public abstract MansionRoomSize getSize();
	
	public int[] getSpawnLocation() {
		return new int[] {this.room.getX(), this.room.getY()+1, this.room.getZ()};
	}
}
