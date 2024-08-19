package org.terraform.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Crappy event meant to allow another debug plugin to register structure locations
 * on plexmap.
 * <p>
 * Does not accurately depict some structure's locations, not for production use.
 */
public final class TerraformStructureSpawnEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String structureName;
    private final int x;
    private final int z;

    public TerraformStructureSpawnEvent(int x, int z, String structureName) {
        this.structureName = structureName;
        this.x = x;
        this.z = z;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    public @NotNull HandlerList getHandlers() {
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