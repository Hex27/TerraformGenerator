package org.terraform.structure.room;

import org.terraform.coregen.populatordata.PopulatorDataAbstract;

import java.util.Random;

public abstract class RoomPopulatorAbstract {
    public final Random rand;
    private final boolean forceSpawn;
    private final boolean unique;

    public RoomPopulatorAbstract(Random rand, boolean forceSpawn, boolean unique) {
        this.rand = rand;
        this.forceSpawn = forceSpawn;
        this.unique = unique;
    }

    protected static int getNextIndex(int bfIndex) {
        bfIndex++;
        if (bfIndex >= 8) {
            bfIndex = 0;
        }
        return bfIndex;
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
