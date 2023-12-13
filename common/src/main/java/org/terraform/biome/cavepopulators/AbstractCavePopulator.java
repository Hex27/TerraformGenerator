package org.terraform.biome.cavepopulators;

import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public abstract class AbstractCavePopulator {
    public abstract void populate(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor);
}
