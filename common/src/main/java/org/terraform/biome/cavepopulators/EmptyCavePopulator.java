package org.terraform.biome.cavepopulators;

import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;

import java.util.Random;

/**
 * Used dto decorate nothing
 */
public class EmptyCavePopulator extends AbstractCavePopulator{
    @Override
    public void populate(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor) {

    }
}
