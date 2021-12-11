package org.terraform.carving;

import java.util.Random;

import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

public abstract class NoiseCaveEntranceCarver {
    public abstract void carve(PopulatorDataAbstract data, TerraformWorld tw, Random random, int x, int z, int groundHeight);
}
