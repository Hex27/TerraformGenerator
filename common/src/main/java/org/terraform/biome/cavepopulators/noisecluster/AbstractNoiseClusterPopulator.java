package org.terraform.biome.cavepopulators.noisecluster;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.cavepopulators.AbstractCavePopulator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public abstract class AbstractNoiseClusterPopulator extends AbstractCavePopulator {


    public abstract boolean canSpawnCluster(TerraformWorld tw, int x, int y, int z);

}
