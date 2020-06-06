package org.terraform.coregen;

import org.bukkit.Chunk;
import org.bukkit.World;

public abstract class NMSInjectorAbstract {
	
	/**
	 * @param world
	 * @return whether or not the injection was a success
	 */
	public abstract boolean attemptInject(World world);
	
	/**
	 * 
	 * @param chunk
	 * @return a populatorDataICA instance.
	 */
	public abstract PopulatorDataICAAbstract getICAData(Chunk chunk);

	/**
	 * 
	 * @param data must be instance of Version-specific PopulatorData
	 * @return a populatorDataICA instance.
	 */
	public abstract PopulatorDataICAAbstract getICAData(PopulatorDataAbstract data);
}
