package org.terraform.utils.version;

import org.bukkit.block.Biome;

public class OneOneEightBlockHandler {


	public static final Biome JAGGED_PEAKS = getBiome("JAGGED_PEAKS", "MOUNTAINS");
	public static final Biome SNOWY_SLOPES = getBiome("SNOWY_SLOPES", "SNOWY_MOUNTAINS");
	public static final Biome STONY_SHORE = getBiome("STONY_SHORE", "MOUNTAIN_EDGE");
	public static final Biome ERODED_BADLANDS = getBiome("ERODED_BADLANDS", "BADLANDS_PLATEAU");
	public static final Biome SNOWY_PLAINS = getBiome("SNOWY_PLAINS", "SNOWY_TUNDRA");
	
	private static Biome getBiome(String name, String fallback) {
		try {
			return Biome.valueOf(name);
		}
		catch(IllegalArgumentException e) {
			return Biome.valueOf(fallback);
		}
	}
}
