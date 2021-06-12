package org.terraform.utils.version;

import org.bukkit.Material;

public class OneOneSevenBlockHandler {

	private static final Material DIRT_PATH = Material.getMaterial("DIRT_PATH") == null ? 
			Material.getMaterial("GRASS_PATH") : Material.getMaterial("DIRT_PATH");

	public static final Material CALCITE = Material.getMaterial("CALCITE") == null ? 
			Material.getMaterial("DIORITE") : Material.getMaterial("CALCITE");
	
	public static final Material AMETHYST_BLOCK = Material.getMaterial("AMETHYST_BLOCK") == null ? 
			Material.getMaterial("STONE") : Material.getMaterial("AMETHYST_BLOCK");

	public static final Material AMETHYST_CLUSTER = Material.getMaterial("AMETHYST_CLUSTER") == null ? 
			Material.getMaterial("CAVE_AIR") : Material.getMaterial("AMETHYST_CLUSTER");
	
	public static final Material BUDDING_AMETHYST = Material.getMaterial("BUDDING_AMETHYST") == null ? 
			Material.getMaterial("STONE") : Material.getMaterial("BUDDING_AMETHYST");
	
	public static final Material POWDER_SNOW = Material.getMaterial("POWDER_SNOW") == null ? 
			Material.getMaterial("SNOW_BLOCK") : Material.getMaterial("POWDER_SNOW");
	
	public static final Material MOSS_BLOCK = Material.getMaterial("MOSS_BLOCK") == null ? 
			Material.getMaterial("GRASS_BLOCK") : Material.getMaterial("MOSS_BLOCK");
	
	public static final Material AZALEA = Material.getMaterial("AZALEA") == null ? 
			Material.getMaterial("OAK_SAPLING") : Material.getMaterial("AZALEA");

	public static final Material AZALEA_LEAVES = Material.getMaterial("AZALEA_LEAVES") == null ? 
			Material.getMaterial("OAK_LEAVES") : Material.getMaterial("AZALEA_LEAVES");

	public static final Material FLOWERING_AZALEA = Material.getMaterial("FLOWERING_AZALEA") == null ? 
			Material.getMaterial("OAK_SAPLING") : Material.getMaterial("FLOWERING_AZALEA");

	public static final Material FLOWERING_AZALEA_LEAVES = Material.getMaterial("FLOWERING_AZALEA_LEAVES") == null ? 
			Material.getMaterial("OAK_LEAVES") : Material.getMaterial("FLOWERING_AZALEA_LEAVES");
	
	public static final Material ROOTED_DIRT = Material.getMaterial("ROOTED_DIRT") == null ? 
			Material.getMaterial("DIRT") : Material.getMaterial("ROOTED_DIRT");

	public static final Material GLOW_LICHEN = Material.getMaterial("GLOW_LICHEN") == null ? 
			Material.getMaterial("VINE") : Material.getMaterial("GLOW_LICHEN");

	public static final Material CAVE_VINES = Material.getMaterial("CAVE_VINES") == null ? 
			Material.getMaterial("VINE") : Material.getMaterial("CAVE_VINES");

	public static final Material CAVE_VINES_PLANT = Material.getMaterial("CAVE_VINES_PLANT") == null ? 
			Material.getMaterial("VINE") : Material.getMaterial("CAVE_VINES_PLANT");

	public static final Material BIG_DRIPLEAF = Material.getMaterial("BIG_DRIPLEAF") == null ? 
			Material.getMaterial("OAK_LEAVES") : Material.getMaterial("BIG_DRIPLEAF");

	public static final Material BIG_DRIPLEAF_STEM = Material.getMaterial("BIG_DRIPLEAF_STEM") == null ? 
			Material.getMaterial("BAMBOO") : Material.getMaterial("BIG_DRIPLEAF_STEM");

	public static final Material SMALL_DRIPLEAF = Material.getMaterial("SMALL_DRIPLEAF") == null ? 
			Material.getMaterial("GRASS") : Material.getMaterial("SMALL_DRIPLEAF");

	public static final Material HANGING_ROOTS = Material.getMaterial("HANGING_ROOTS") == null ? 
			Material.getMaterial("CAVE_AIR") : Material.getMaterial("HANGING_ROOTS");

	public static final Material POINTED_DRIPSTONE = Material.getMaterial("POINTED_DRIPSTONE") == null ? 
			Material.getMaterial("GRANITE_WALL") : Material.getMaterial("POINTED_DRIPSTONE");

	public static final Material TUFF = Material.getMaterial("TUFF") == null ? 
			Material.getMaterial("STONE") : Material.getMaterial("TUFF");

	public static final Material DEEPSLATE = Material.getMaterial("DEEPSLATE") == null ? 
			Material.getMaterial("STONE") : Material.getMaterial("DEEPSLATE");

	public static final Material COPPER_ORE = Material.getMaterial("COPPER_ORE") == null ? 
			Material.getMaterial("STONE") : Material.getMaterial("COPPER_ORE");

	public static final Material DRIPSTONE_BLOCK = Material.getMaterial("DRIPSTONE_BLOCK") == null ? 
			Material.getMaterial("GRANITE") : Material.getMaterial("DRIPSTONE_BLOCK");

	//This isn't relevant anymore but im lazy
	public static Material DIRT_PATH() {
		return DIRT_PATH;
	}

	public static Material deepSlateVersion(Material target) {
		Material mat = Material.getMaterial("DEEPSLATE_"+target.toString());
		if(mat == null)
			return target;
		else
			return mat;
	}
}