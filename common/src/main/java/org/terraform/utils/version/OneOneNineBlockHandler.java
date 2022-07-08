package org.terraform.utils.version;

import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class OneOneNineBlockHandler {

	//public static final EntityType FROG = getEntityType("FROG", "FISH");

	public static final EntityType ALLAY = getEntityType("ALLAY", "CHICKEN");
	
	public static final Material MUD = Material.getMaterial("MUD") == null ? 
			Material.getMaterial("PODZOL") : Material.getMaterial("MUD");
	
	public static final Material REINFORCED_DEEPSLATE = Material.getMaterial("REINFORCED_DEEPSLATE") == null ? 
			Material.getMaterial("POLISHED_DIORITE") : Material.getMaterial("REINFORCED_DEEPSLATE");
	
	
	public static final Material MANGROVE_LEAVES = Material.getMaterial("MANGROVE_LEAVES") == null ? 
			Material.getMaterial("OAK_LEAVES") : Material.getMaterial("MANGROVE_LEAVES");
	public static final Material MANGROVE_LOG = Material.getMaterial("MANGROVE_LOG") == null ? 
			Material.getMaterial("OAK_LOG") : Material.getMaterial("MANGROVE_LOG");
	public static final Material MANGROVE_WOOD = Material.getMaterial("MANGROVE_WOOD") == null ? 
			Material.getMaterial("OAK_WOOD") : Material.getMaterial("MANGROVE_WOOD");
	public static final Material MANGROVE_PROPAGULE = Material.getMaterial("MANGROVE_PROPAGULE") == null ? 
			Material.getMaterial("AIR") : Material.getMaterial("MANGROVE_PROPAGULE");
	public static final Material MANGROVE_ROOTS = Material.getMaterial("MANGROVE_ROOTS") == null ? 
			Material.getMaterial("OAK_WOOD") : Material.getMaterial("MANGROVE_ROOTS");
	public static final Material MUDDY_MANGROVE_ROOTS = Material.getMaterial("MUDDY_MANGROVE_ROOTS") == null ? 
			Material.getMaterial("OAK_WOOD") : Material.getMaterial("MUDDY_MANGROVE_ROOTS");
	
	public static final Material MANGROVE_FENCE = Material.getMaterial("MANGROVE_FENCE") == null ? 
			Material.getMaterial("OAK_FENCE") : Material.getMaterial("MANGROVE_FENCE");
	
	
	public static final Biome MANGROVE_SWAMP = getBiome("MANGROVE_SWAMP", "SWAMP");
	
	private static Biome getBiome(String name, String fallback) {
		try {
			return Biome.valueOf(name);
		}
		catch(IllegalArgumentException e) {
			return Biome.valueOf(fallback);
		}
	}
	private static EntityType getEntityType(String name, String fallback) {
		try {
			return EntityType.valueOf(name);
		}
		catch(IllegalArgumentException e) {
			return EntityType.valueOf(fallback);
		}
	}
	
	private static final String propaguleDataString = "minecraft:mangrove_propagule[hanging=true,age=4,waterlogged=false]";
	public static BlockData getHangingMangrovePropagule() {
		return Bukkit.createBlockData(propaguleDataString);
	}
}
