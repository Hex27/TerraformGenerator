package org.terraform.coregen;

import org.bukkit.World;

public abstract class NMSInjectorAbstract {
	
	/**
	 * @param world
	 * @return whether or not the injection was a success
	 */
	public abstract boolean attemptInject(World world);

}
