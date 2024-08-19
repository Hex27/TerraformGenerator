package org.terraform.coregen.populatordata;

import org.terraform.coregen.NaturalSpawnType;


public abstract class PopulatorDataICAAbstract extends PopulatorDataAbstract implements IPopulatorDataMinecartSpawner {

    public abstract void registerNaturalSpawns(NaturalSpawnType type, int x0, int y0, int z0, int x1, int y1, int z1);

}
