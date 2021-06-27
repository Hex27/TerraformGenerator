package org.terraform.biome.custombiomes;

import org.bukkit.block.Biome;

public abstract class CustomBiomeSupportedBiomeGrid {
	
	public void setBiome(int x, int z, CustomBiomeType bio, Biome fallback) {
        for (int y = 0; y < 256; y++) {
            setBiome(x, y, z, bio, fallback);
        }
	}
	
	public abstract void setBiome(int x, int y, int z, CustomBiomeType bio, Biome fallback);
}
