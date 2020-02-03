package org.terraform.structure;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

public abstract class StructurePopulator {
	
	public abstract boolean canSpawn(Random rand, ArrayList<BiomeBank> biomes);
	
	public abstract void populate(TerraformWorld world, Random random, PopulatorDataAbstract data);

}
