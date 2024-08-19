package org.terraform.coregen;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICAAbstract;

public abstract class NMSInjectorAbstract {

    public void startupTasks() {
    }

    public @Nullable BlockDataFixerAbstract getBlockDataFixer() {
        return null;
    }

    /**
     * @return whether or not the injection was a success
     */
    public abstract boolean attemptInject(World world);

    /**
     * @return a populatorDataICA instance.
     */
    public abstract PopulatorDataICAAbstract getICAData(Chunk chunk);

    /**
     * @param data must be instance of Version-specific PopulatorData
     * @return a populatorDataICA instance.
     */
    public abstract @Nullable PopulatorDataICAAbstract getICAData(PopulatorDataAbstract data);

    /**
     * This was unironically the easiest way to add a bee to a
     * beehive without spawning an entity.
     * <br><br>
     * You've never made a PR to spigot before, maybe learn how to do it
     * for this.
     */
    public abstract void storeBee(Beehive hive);

    /**
     * Force an NMS physics update at the location.
     */
    public void updatePhysics(World world, org.bukkit.block.Block block) {
        throw new UnsupportedOperationException("Tried to update physics without implementing.");
    }

    public int getMinY() {
        return 0;
    }

    public int getMaxY() {
        return 256;
    }
}
