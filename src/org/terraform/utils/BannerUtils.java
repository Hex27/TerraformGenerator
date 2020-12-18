package org.terraform.utils;

import org.bukkit.Material;

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

    public static Material randomBannerMaterial(Random rand) {
        return BANNERS[rand.nextInt(BANNERS.length)];
    }

    public static Material randomWallBannerMaterial(Random rand) {
        return WALL_BANNERS[rand.nextInt(WALL_BANNERS.length)];
    }
}
