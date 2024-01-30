package org.terraform.coregen.populatordata;

import java.util.Random;

import org.terraform.coregen.NaturalSpawnType;
import org.terraform.coregen.TerraLootTable;


public abstract class PopulatorDataICAAbstract extends PopulatorDataAbstract implements IPopulatorDataMinecartSpawner {
    
	public abstract void registerNaturalSpawns(NaturalSpawnType type, int x0, int y0, int z0, int x1, int y1, int z1);

}
