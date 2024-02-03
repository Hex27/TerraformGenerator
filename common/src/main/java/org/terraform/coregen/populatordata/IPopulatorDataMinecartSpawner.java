package org.terraform.coregen.populatordata;

import org.terraform.coregen.TerraLootTable;

import java.util.Random;

public interface IPopulatorDataMinecartSpawner {
    void spawnMinecartWithChest(int x, int y, int z, TerraLootTable table, Random random);

}
