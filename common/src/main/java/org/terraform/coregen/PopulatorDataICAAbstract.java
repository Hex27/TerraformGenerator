package org.terraform.coregen;

import java.util.Random;


public abstract class PopulatorDataICAAbstract extends PopulatorDataAbstract {
    public abstract void registerGuardians(int x0, int y0, int z0, int x1, int y1, int z1);

    public abstract void spawnMinecartWithChest(int x, int y, int z, TerraLootTable table, Random random);
}
