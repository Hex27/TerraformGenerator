package org.terraform.biome.cave;

import java.util.Random;

import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

public abstract class AbstractCavePopulator {
	
	public abstract void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data);
	
}
