package org.terraform.utils;

import com.google.common.collect.Lists;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.version.V_1_20_5;

import java.util.ArrayList;
import java.util.Random;

public class BannerUtils {

    private static final PatternType[] PATTERNS = {
            PatternType.BASE,
            PatternType.BORDER,
            PatternType.BRICKS,
            V_1_20_5.CIRCLE,
            PatternType.CREEPER,
            PatternType.CROSS,
            PatternType.CURLY_BORDER,
            PatternType.DIAGONAL_LEFT,
            PatternType.DIAGONAL_RIGHT,
            //            PatternType.DIAGONAL_UP_LEFT,
            //            PatternType.DIAGONAL_UP_RIGHT,
            //            PatternType.FLOW,
            PatternType.FLOWER,
            PatternType.GLOBE,
            PatternType.GRADIENT,
            PatternType.GRADIENT_UP,
            // PatternType.GUSTER,
            PatternType.HALF_HORIZONTAL,
            // PatternType.HALF_HORIZONTAL_BOTTOM,
            PatternType.HALF_VERTICAL,
            // PatternType.HALF_VERTICAL_RIGHT,
            PatternType.MOJANG,
            PatternType.PIGLIN,
            V_1_20_5.RHOMBUS,
            PatternType.SKULL,
            // PatternType.SMALL_STRIPES,
            PatternType.SQUARE_BOTTOM_LEFT,
            PatternType.SQUARE_BOTTOM_RIGHT,
            PatternType.SQUARE_TOP_LEFT,
            PatternType.SQUARE_TOP_RIGHT,
            PatternType.STRAIGHT_CROSS,
            PatternType.STRIPE_BOTTOM,
            PatternType.STRIPE_CENTER,
            PatternType.STRIPE_DOWNLEFT,
            PatternType.STRIPE_DOWNRIGHT,
            PatternType.STRIPE_LEFT,
            PatternType.STRIPE_MIDDLE,
            PatternType.STRIPE_RIGHT,
            PatternType.STRIPE_TOP,
            PatternType.TRIANGLE_BOTTOM,
            PatternType.TRIANGLE_TOP,
            PatternType.TRIANGLES_BOTTOM,
            PatternType.TRIANGLES_TOP
    };
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

    public static void generateBanner(@NotNull SimpleBlock base,
                                      @NotNull BlockFace facing,
                                      @NotNull Material type,
                                      @Nullable ArrayList<Pattern> patterns)
    {

        base.setType(type);
        Directional bd = ((Directional) base.getBlockData());
        bd.setFacing(facing);
        base.setBlockData(bd);

        Banner banner = (Banner) ((PopulatorDataPostGen) base.getPopData()).getBlockState(
                base.getX(),
                base.getY(),
                base.getZ()
        );
        if (patterns == null) {
            patterns = new ArrayList<>();
        }

        banner.setPatterns(patterns);
        banner.update();
    }

    public static void generateBanner(@NotNull Random rand,
                                      @NotNull SimpleBlock base,
                                      @NotNull BlockFace facing,
                                      boolean wallBanner)
    {

        Material type = null;
        if (wallBanner) {
            type = BannerUtils.randomWallBannerMaterial(rand);
        }
        else {
            BannerUtils.randomBannerMaterial(rand);
        }
        base.setType(type);
        if (!wallBanner) {
            Rotatable bd = ((Rotatable) base.getBlockData());
            bd.setRotation(facing);
            base.setBlockData(bd);
        }
        else {
            Directional bd = ((Directional) base.getBlockData());
            bd.setFacing(facing);
            base.setBlockData(bd);
        }

        Banner banner = (Banner) ((PopulatorDataPostGen) base.getPopData()).getBlockState(
                base.getX(),
                base.getY(),
                base.getZ()
        );
        ArrayList<Pattern> patterns = new ArrayList<>();

        for (int i = 1 + rand.nextInt(3); i < 4 + rand.nextInt(3); i++) {
            patterns.add(new Pattern(DyeColor.values()[rand.nextInt(DyeColor.values().length)],
                    PATTERNS[rand.nextInt(PATTERNS.length)]
            ));
        }
        banner.setPatterns(patterns);
        banner.update();
    }

    public static void generatePillagerBanner(@NotNull SimpleBlock base,
                                              @NotNull BlockFace facing,
                                              boolean wallBanner)
    {

        Material type;
        if (wallBanner) {
            type = Material.WHITE_WALL_BANNER;
        }
        else {
            type = Material.WHITE_BANNER;
        }
        base.setType(type);
        if (!wallBanner) {
            Rotatable bd = ((Rotatable) base.getBlockData());
            bd.setRotation(facing);
            base.setBlockData(bd);
        }
        else {
            Directional bd = ((Directional) base.getBlockData());
            bd.setFacing(facing);
            base.setBlockData(bd);
        }

        Banner banner = (Banner) ((PopulatorDataPostGen) base.getPopData()).getBlockState(
                base.getX(),
                base.getY(),
                base.getZ()
        );

        banner.setPatterns(getOminousBannerPatterns());
        banner.update();
    }


    public static void randomBannerMaterial(@NotNull Random rand) {
        rand.nextInt(BANNERS.length);
    }

    public static @NotNull Material randomWallBannerMaterial(@NotNull Random rand) {
        return WALL_BANNERS[rand.nextInt(WALL_BANNERS.length)];
    }

    /**
     * kms
     * <a href="https://minecraft.fandom.com/wiki/Banner/Patterns">...</a>
     * Pattern:"mr",Color:CYAN
     * Pattern:"bs",Color:LIGHT_GRAY
     * Pattern:"cs",Color:GRAY
     * Pattern:"bo",Color:LIGHT_GRAY
     * Pattern:"ms",Color:BLACK
     * Pattern:"hh",Color:LIGHT_GRAY
     * Pattern:"mc",Color:LIGHT_GRAY
     * Pattern:"bo",Color:BLACK
     */
    public static @NotNull ArrayList<Pattern> getOminousBannerPatterns() {
        return Lists.newArrayList(
            new Pattern(DyeColor.CYAN, V_1_20_5.RHOMBUS),
            new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_BOTTOM),
            new Pattern(DyeColor.GRAY, PatternType.STRIPE_CENTER),
            new Pattern(DyeColor.LIGHT_GRAY, PatternType.BORDER),
            new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE),
            new Pattern(DyeColor.LIGHT_GRAY, PatternType.HALF_HORIZONTAL),
            new Pattern(DyeColor.LIGHT_GRAY, V_1_20_5.CIRCLE), //
            new Pattern(DyeColor.BLACK, PatternType.BORDER)
        );
    }
}
