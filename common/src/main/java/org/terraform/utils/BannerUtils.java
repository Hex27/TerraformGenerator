package org.terraform.utils;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;

import java.util.ArrayList;
import java.util.Random;

public class BannerUtils {
    private static final Material[] BANNERS = {
            Material.RED_BANNER,
            Material.ORANGE_BANNER,
            Material.YELLOW_BANNER,
            Material.LIME_BANNER,
            Material.GREEN_BANNER,
            Material.CYAN_BANNER,
            Material.BLUE_BANNER,
            Material.PURPLE_BANNER,
            Material.MAGENTA_BANNER,
            Material.BLACK_BANNER,
            Material.BROWN_BANNER,
            Material.PINK_BANNER,
            Material.WHITE_BANNER,
    };
    private static final Material[] WALL_BANNERS = {
            Material.RED_WALL_BANNER,
            Material.ORANGE_WALL_BANNER,
            Material.YELLOW_WALL_BANNER,
            Material.LIME_WALL_BANNER,
            Material.GREEN_WALL_BANNER,
            Material.CYAN_WALL_BANNER,
            Material.BLUE_WALL_BANNER,
            Material.PURPLE_WALL_BANNER,
            Material.MAGENTA_WALL_BANNER,
            Material.BLACK_WALL_BANNER,
            Material.BROWN_WALL_BANNER,
            Material.PINK_WALL_BANNER,
            Material.WHITE_WALL_BANNER,
    };
    
    public static Banner generateBanner(SimpleBlock base, BlockFace facing, Material type, ArrayList<Pattern> patterns) {

        base.setType(type);
        Directional bd = ((Directional) base.getBlockData());
        bd.setFacing(facing);
        base.setBlockData(bd);
    
        Banner banner = (Banner) ((PopulatorDataPostGen) base.getPopData()).getBlockState(base.getX(), base.getY(), base.getZ());
        if(patterns == null) {
            patterns = new ArrayList<Pattern>();
        }
        
        banner.setPatterns(patterns);
        banner.update();
        return banner;
    }
    
    public static Banner generateBanner(Random rand, SimpleBlock base, BlockFace facing, boolean wallBanner) {

        Material type = null;
        if (wallBanner)
            type = BannerUtils.randomWallBannerMaterial(rand);
        else
            BannerUtils.randomBannerMaterial(rand);
        base.setType(type);
        if (!wallBanner) {
            Rotatable bd = ((Rotatable) base.getBlockData());
            bd.setRotation(facing);
            base.setBlockData(bd);
        } else {
            Directional bd = ((Directional) base.getBlockData());
            bd.setFacing(facing);
            base.setBlockData(bd);
        }

        Banner banner = (Banner) ((PopulatorDataPostGen) base.getPopData()).getBlockState(base.getX(), base.getY(), base.getZ());
        ArrayList<Pattern> patterns = new ArrayList<Pattern>();

        for (int i = 1 + rand.nextInt(3); i < 4 + rand.nextInt(3); i++) {
            patterns.add(new Pattern(
                    DyeColor.values()[rand.nextInt(DyeColor.values().length)],
                    PatternType.values()[rand.nextInt(PatternType.values().length)]
            ));
        }
        banner.setPatterns(patterns);
        banner.update();
        return banner;
    }
    
    public static Banner generatePillagerBanner(SimpleBlock base, BlockFace facing, boolean wallBanner) {

        Material type = null;
        if (wallBanner)
            type = Material.WHITE_WALL_BANNER;
        else
            type = Material.WHITE_BANNER;
        base.setType(type);
        if (!wallBanner) {
            Rotatable bd = ((Rotatable) base.getBlockData());
            bd.setRotation(facing);
            base.setBlockData(bd);
        } else {
            Directional bd = ((Directional) base.getBlockData());
            bd.setFacing(facing);
            base.setBlockData(bd);
        }

        Banner banner = (Banner) ((PopulatorDataPostGen) base.getPopData()).getBlockState(base.getX(), base.getY(), base.getZ());
        
        banner.setPatterns(getOminousBannerPatterns());
        banner.update();
        return banner;
    }


    public static Material randomBannerMaterial(Random rand) {
        return BANNERS[rand.nextInt(BANNERS.length)];
    }

    public static Material randomWallBannerMaterial(Random rand) {
        return WALL_BANNERS[rand.nextInt(WALL_BANNERS.length)];
    }

	/**
	 * kms
	 * https://minecraft.fandom.com/wiki/Banner/Patterns
		Pattern:"mr",Color:CYAN
		Pattern:"bs",Color:LIGHT_GRAY
		Pattern:"cs",Color:GRAY
		Pattern:"bo",Color:LIGHT_GRAY
		Pattern:"ms",Color:BLACK
		Pattern:"hh",Color:LIGHT_GRAY
		Pattern:"mc",Color:LIGHT_GRAY
		Pattern:"bo",Color:BLACK
	 */
    public static ArrayList<Pattern> getOminousBannerPatterns(){
    	return new ArrayList<Pattern>() {{
			add(new Pattern(DyeColor.CYAN, PatternType.RHOMBUS_MIDDLE));
			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_BOTTOM));
			add(new Pattern(DyeColor.GRAY, PatternType.STRIPE_CENTER));
			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.BORDER));
			add(new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE));
			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.HALF_HORIZONTAL));
			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.CIRCLE_MIDDLE));
			add(new Pattern(DyeColor.BLACK, PatternType.BORDER));
		}};
    }
}
