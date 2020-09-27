package org.terraform.structure;

import java.util.ArrayList;
import java.util.Random;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

public abstract class StructurePopulator {
	
	public abstract boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ,ArrayList<BiomeBank> biomes);
	
	public abstract boolean isEnabled();
	
	public abstract void populate(TerraformWorld world, PopulatorDataAbstract data);

	public abstract int[] getNearestFeature(TerraformWorld world, int rawX, int rawZ);
	
	public abstract Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ);
}
