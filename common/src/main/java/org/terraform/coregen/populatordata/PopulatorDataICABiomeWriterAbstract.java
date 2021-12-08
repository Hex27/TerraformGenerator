package org.terraform.coregen.populatordata;

import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;

public abstract class PopulatorDataICABiomeWriterAbstract extends PopulatorDataICAAbstract {

	public abstract void setBiome(int rawX, int rawY, int rawZ, BiomeBank biomebank);
	public abstract void setBiome(int rawX, int rawY, int rawZ, Biome biome);
	
}
