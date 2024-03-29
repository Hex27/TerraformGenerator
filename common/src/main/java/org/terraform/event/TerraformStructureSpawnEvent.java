package org.terraform.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Crappy event meant to allow another debug plugin to register structure locations
 * on plexmap.
 * 
 * Does not accurately depict some structure's locations, not for production use.
 */
public final class TerraformStructureSpawnEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String structureName;
    private int x;
    private int z;
    
    public TerraformStructureSpawnEvent(int x, int z, String structureName) {
    	this.structureName = structureName;
    	this.x = x;
    	this.z = z;
    }


    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


	public String getStructureName() {
		return structureName;
	}


	public int getX() {
		return x;
	}


	public int getZ() {
		return z;
	}
}