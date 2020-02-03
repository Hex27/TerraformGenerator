package org.terraform.structure.room;

import java.util.Random;

import org.terraform.coregen.PopulatorDataAbstract;

public abstract class RoomPopulatorAbstract {
	
	public Random rand;
	private boolean forceSpawn;
	private boolean unique;
	public RoomPopulatorAbstract(Random rand, boolean forceSpawn, boolean unique){
		this.rand = rand;
		this.forceSpawn = forceSpawn;
		this.unique = unique;
	}
	
	/**
	 * @return the rand
	 */
	public Random getRand() {
		return rand;
	}

	/**
	 * @return the forceSpawn
	 */
	public boolean isForceSpawn() {
		return forceSpawn;
	}

	/**
	 * @return the unique
	 */
	public boolean isUnique() {
		return unique;
	}

	public abstract void populate(PopulatorDataAbstract data, CubeRoom room);

	public abstract boolean canPopulate(CubeRoom room);
	
}
