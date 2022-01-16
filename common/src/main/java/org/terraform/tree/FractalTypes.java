package org.terraform.tree;

public class FractalTypes {
    public enum Tree {
        FOREST,
        NORMAL_SMALL,
        AZALEA_TOP,
        TAIGA_BIG,
        TAIGA_SMALL,
        SCARLET_BIG,
        SCARLET_SMALL,
        SAVANNA_SMALL,
        SAVANNA_BIG,
        WASTELAND_BIG,
        SWAMP_TOP,
        SWAMP_BOTTOM,
        BIRCH_BIG,
        BIRCH_SMALL,
        CHERRY_SMALL,
        CHERRY_THICK,
        JUNGLE_BIG,
        JUNGLE_SMALL,
        JUNGLE_EXTRA_SMALL,
        COCONUT_TOP,
        DARK_OAK_SMALL,
        DARK_OAK_BIG_TOP,
        DARK_OAK_BIG_BOTTOM,
        FROZEN_TREE_BIG,
        FROZEN_TREE_SMALL,
        FIRE_CORAL,
        HORN_CORAL,
        BRAIN_CORAL,
        TUBE_CORAL,
        BUBBLE_CORAL,
        GIANT_PUMPKIN,
        ANDESITE_PETRIFIED_SMALL,
        GRANITE_PETRIFIED_SMALL,
        DIORITE_PETRIFIED_SMALL
        ;

        public static final Tree[] VALUES = Tree.values();
    }

    public enum Mushroom {
        TINY_BROWN_MUSHROOM,
        TINY_RED_MUSHROOM,
        SMALL_RED_MUSHROOM,
        SMALL_POINTY_RED_MUSHROOM,
        SMALL_BROWN_MUSHROOM,
        MEDIUM_BROWN_MUSHROOM,
        MEDIUM_RED_MUSHROOM,
        MEDIUM_BROWN_FUNNEL_MUSHROOM,
        GIANT_BROWN_MUSHROOM,
        GIANT_BROWN_FUNNEL_MUSHROOM,
        GIANT_RED_MUSHROOM;

        public static final Mushroom[] VALUES = Mushroom.values();
    }

    public enum MushroomCap {
        ROUND,
        FLAT,
        FUNNEL,
        POINTY;
        public static final MushroomCap[] VALUES = MushroomCap.values();
    }
}
