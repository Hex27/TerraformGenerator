package org.terraform.biome.cave;

import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public abstract class AbstractCavePopulator {
    public abstract void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data);
}
