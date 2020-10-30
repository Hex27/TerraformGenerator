package org.terraform.carving;

import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public abstract class Carver {
    public abstract void carve(TerraformWorld tw, PopulatorDataAbstract data, Random random);
}
