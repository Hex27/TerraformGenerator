package org.terraform.structure.pyramid;

import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;

import java.util.Random;

public class PyramidPathPopulator extends PathPopulatorAbstract {
    @SuppressWarnings("unused")
    private final Random rand;
    private final int height;

    public PyramidPathPopulator(Random rand) {
        this.rand = rand;
        this.height = 3;
    }

    public PyramidPathPopulator(Random rand, int height) {
        this.rand = rand;
        this.height = height;
    }

    @Override
    public void populate(PathPopulatorData ppd) {

    }

    @Override
    public int getPathWidth() {
        return 1;
    }

    @Override
    public int getPathHeight() {
        return height;
    }

}
