package org.terraform.utils.version;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.BlockUtils;

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

	public static final Material MOSS_CARPET = Material.getMaterial("MOSS_CARPET") == null ? 
			Material.getMaterial("CAVE_AIR") : Material.getMaterial("MOSS_CARPET");
	
	public static final Material AZALEA = Material.getMaterial("AZALEA") == null ? 
			Material.getMaterial("OAK_SAPLING") : Material.getMaterial("AZALEA");

	public static final Material AZALEA_LEAVES = Material.getMaterial("AZALEA_LEAVES") == null ? 
			Material.getMaterial("OAK_LEAVES") : Material.getMaterial("AZALEA_LEAVES");

	public static final Material FLOWERING_AZALEA = Material.getMaterial("FLOWERING_AZALEA") == null ? 
			Material.getMaterial("OAK_SAPLING") : Material.getMaterial("FLOWERING_AZALEA");

	public static final Material FLOWERING_AZALEA_LEAVES = Material.getMaterial("FLOWERING_AZALEA_LEAVES") == null ? 
			Material.getMaterial("OAK_LEAVES") : Material.getMaterial("FLOWERING_AZALEA_LEAVES");
	
	public static final Material SPORE_BLOSSOM = Material.getMaterial("SPORE_BLOSSOM") == null ? 
			Material.getMaterial("OAK_LEAVES") : Material.getMaterial("SPORE_BLOSSOM");

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
	
	public static final Material COBBLED_DEEPSLATE_WALL = Material.getMaterial("COBBLED_DEEPSLATE_WALL") == null ? 
			Material.getMaterial("COBBLESTONE_WALL") : Material.getMaterial("COBBLED_DEEPSLATE_WALL");
	
	public static final Material COBBLED_DEEPSLATE_SLAB = Material.getMaterial("COBBLED_DEEPSLATE_SLAB") == null ? 
			Material.getMaterial("COBBLESTONE_SLAB") : Material.getMaterial("COBBLED_DEEPSLATE_SLAB");

	public static final Material COPPER_ORE = Material.getMaterial("COPPER_ORE") == null ? 
			Material.getMaterial("STONE") : Material.getMaterial("COPPER_ORE");

	public static final Material DRIPSTONE_BLOCK = Material.getMaterial("DRIPSTONE_BLOCK") == null ? 
			Material.getMaterial("GRANITE") : Material.getMaterial("DRIPSTONE_BLOCK");

	public static final Biome LUSH_CAVES = getBiome("LUSH_CAVES", "PLAINS");
	public static final Biome DRIPSTONE_CAVES = getBiome("DRIPSTONE_CAVES", "PLAINS");
	
	private static Biome getBiome(String name, String fallback) {
		try {
			return Biome.valueOf(name);
		}
		catch(IllegalArgumentException e) {
			return Biome.valueOf(fallback);
		}
	}
	
	public static final Material CANDLE = Material.getMaterial("CANDLE") == null ? 
			Material.getMaterial("TORCH") : Material.getMaterial("CANDLE");
	
	private static Method setCandlesMethod = null;
	public static void placeCandle(SimpleBlock block, int numCandles, boolean lit) {
		if(Version.isAtLeast(17)) {
			Lightable candle = (Lightable) Bukkit.createBlockData(CANDLE);
			candle.setLit(lit);
			
			try {
				if(setCandlesMethod == null) {
					setCandlesMethod = Class.forName("org.bukkit.block.data.type.Candle").getMethod("setCandles", int.class);
				}
				setCandlesMethod.invoke(candle, numCandles);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			block.setBlockData(candle);
		}
		else
			block.setType(Material.TORCH);
	}
	
	//This isn't relevant anymore but im lazy
	public static Material DIRT_PATH() {
		return DIRT_PATH;
	}
	
	public static void downLCaveVines(int height, SimpleBlock base) {
		int realHeight = 0;
		while(!base.getRelative(0,-realHeight,0).getType().isSolid() && height > 0) {
			realHeight++;
			height--;
		}
		if(base.getRelative(0,-realHeight,0).getType().isSolid())
			realHeight--;
		
		if(realHeight <= 0) return;
		
		for(int i = realHeight; i > 0; i--) {
			Material vine = CAVE_VINES_PLANT;
			if(i == 1)
				vine = CAVE_VINES;
			
			if(BlockUtils.isAir(base.getRelative(0, -(realHeight - i), 0).getType()))
				base.getRelative(0, -(realHeight - i), 0).setBlockData(getCaveVine(vine, new Random().nextInt(3) == 0));
		}
	}
	
	public static void downLPointedDripstone(int height, SimpleBlock base) {
		int realHeight = 0;
		while(!base.getRelative(0,-realHeight,0).getType().isSolid() && height > 0) {
			realHeight++;
			height--;
		}
		if(base.getRelative(0,-realHeight,0).getType().isSolid())
			realHeight--;
		
		if(realHeight <= 0) return;
		
		for(int i = realHeight; i > 0; i--) {
			PointedDripstoneThickness thickness = PointedDripstoneThickness.middle;
			if(i == 1)
				thickness = PointedDripstoneThickness.tip;
			if(i == 2)
				thickness = PointedDripstoneThickness.frustum;
			if(i == realHeight && realHeight > 2)
				thickness = PointedDripstoneThickness.base;
			
			base.getRelative(0, -(realHeight - i), 0).setBlockData(getPointedDripstone(thickness, false, BlockFace.DOWN));
		}
	}
	
	public static void upLPointedDripstone(int height, SimpleBlock base) {
		int realHeight = 0;
		while(!base.getRelative(0,realHeight,0).getType().isSolid() && height > 0) {
			realHeight++;
			height--;
		}
		if(base.getRelative(0,realHeight,0).getType().isSolid())
			realHeight--;
		
		if(realHeight <= 0) return;
		
		for(int i = 0; i < realHeight; i++) {
			PointedDripstoneThickness thickness = PointedDripstoneThickness.middle;
			
			if(realHeight >= 4) {
				if(i == realHeight-1)
					thickness = PointedDripstoneThickness.tip;
				if(i == realHeight-2)
					thickness = PointedDripstoneThickness.frustum;
				if(i == 0)
					thickness = PointedDripstoneThickness.base;
			}else if(realHeight >= 3) {
				if(i == realHeight-1)
					thickness = PointedDripstoneThickness.tip;
				if(i == realHeight-2)
					thickness = PointedDripstoneThickness.frustum;
				if(i == 0)
					thickness = PointedDripstoneThickness.base;
			}else if(realHeight >= 2) {
				thickness = PointedDripstoneThickness.tip;
				if(i == 0)
					thickness = PointedDripstoneThickness.frustum;
			}else if(realHeight == 1) {
				thickness = PointedDripstoneThickness.tip;
			}
			
			base.getRelative(0, i, 0).setBlockData(getPointedDripstone(thickness, false, BlockFace.UP));
		}
	}
	
	public static BlockData getPointedDripstone(PointedDripstoneThickness thickness, boolean waterlogged, BlockFace direction) {
		BlockData data = Bukkit.createBlockData(OneOneSevenBlockHandler.POINTED_DRIPSTONE);
		String stringData = data.getAsString();
		stringData = StringUtils.replace(stringData, "thickness=tip", "thickness=" + thickness.toString());
		stringData = StringUtils.replace(stringData, "vertical_direction=up", "vertical_direction=" + direction.toString().toLowerCase());
		stringData = StringUtils.replace(stringData, "waterlogged=false", "waterlogged=" + waterlogged);
		
		data = Bukkit.createBlockData(stringData);
		return data;
	}
	
	public static BlockData getCaveVine(Material caveVine, boolean glowBerries) {
		BlockData data = Bukkit.createBlockData(caveVine);
		String stringData = data.getAsString();
		stringData = StringUtils.replace(stringData, "berries=false", "berries=" + glowBerries);
		
		data = Bukkit.createBlockData(stringData);
		return data;
	}

	private static final HashMap<String, Material> deepslateMap = new HashMap<>();
	public static Material deepSlateVersion(Material target) {
		if(!Version.isAtLeast(17)) //No deepslate in 1.16
			return target;
		
		Material mat = deepslateMap.get("DEEPSLATE_"+target.toString());
		
		if(mat == null) {
			mat =  Material.getMaterial("DEEPSLATE_"+target.toString());
		}
		
		if(mat == null)
			return target;
		else {
			deepslateMap.put("DEEPSLATE_"+target.toString(), mat);
			return mat;
		}
	}
	
	public static enum PointedDripstoneThickness{
		tip,
		frustum,
		middle,
		base
	}
}