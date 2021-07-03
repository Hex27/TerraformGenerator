package org.terraform.coregen;

import org.bukkit.Chunk;
import org.bukkit.World;

public abstract class NMSInjectorAbstract {
	
	public void startupTasks() {}
	
    public BlockDataFixerAbstract getBlockDataFixer() {
        return null;
    }

    /**
     * @param world
     * @return whether or not the injection was a success
     */
    public abstract boolean attemptInject(World world);

    /**
     * @param chunk
     * @return a populatorDataICA instance.
     */
    public abstract PopulatorDataICAAbstract getICAData(Chunk chunk);

    /**
     * @param data must be instance of Version-specific PopulatorData
     * @return a populatorDataICA instance.
     */
    public abstract PopulatorDataICAAbstract getICAData(PopulatorDataAbstract data);
    
    /**
     * Force an NMS physics update at the location.
     */
    public abstract void updatePhysics(World world, org.bukkit.block.Block block);
    
    public int getMinY() {
    	return 0;
    }
    
    public int getMaxY() {
    	return 256;
    }
}
