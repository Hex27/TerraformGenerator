package org.terraform.utils;

import java.util.Random;

import org.bukkit.Material;

public class BannerUtils {

    public static Material[] banners = new Material[] {
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
    public static Material[] wall_banners = new Material[] {
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

    public static Material randomBannerMaterial(Random rand) {
    	return banners[rand.nextInt(banners.length)];
    }

    public static Material randomWallBannerMaterial(Random rand) {
    	return wall_banners[rand.nextInt(wall_banners.length)];
    }
}
