package org.terraform.structure.room;

import java.util.Random;

import org.terraform.coregen.populatordata.PopulatorDataAbstract;

public abstract class RoomPopulatorAbstract {
    private final boolean forceSpawn;
    private final boolean unique;
    public Random rand;

    public RoomPopulatorAbstract(Random rand, boolean forceSpawn, boolean unique) {
        this.rand = rand;
        this.forceSpawn = forceSpawn;
        this.unique = unique;
    }

    protected static int getNextIndex(int bfIndex) {
        bfIndex++;
        if (bfIndex >= 8) bfIndex = 0;
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
