package org.terraform.tree;

public class FractalTypes {
    public enum Tree {
        FOREST,
        NORMAL_SMALL,
        TAIGA_BIG,
        TAIGA_SMALL,
        SAVANNA_SMALL,
        SAVANNA_BIG,
        WASTELAND_BIG,
        SWAMP_TOP,
        SWAMP_BOTTOM,
        BIRCH_BIG,
        BIRCH_SMALL,
        JUNGLE_BIG,
        JUNGLE_SMALL,
        JUNGLE_EXTRA_SMALL,
        COCONUT_TOP,
        DARK_OAK_SMALL,
        DARK_OAK_BIG_TOP,
        DARK_OAK_BIG_BOTTOM,
        FROZEN_TREE_BIG,
        FROZEN_TREE_SMALL;

        public static final Tree[] VALUES = Tree.values();
    }

    public enum Mushroom {
        TINY_RED_MUSHROOM,
        SMALL_RED_MUSHROOM,
        SMALL_POINTY_RED_MUSHROOM,
        SMALL_BROWN_MUSHROOM,
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

    public enum Other {
        GIANT_PUMPKIN,
        FIRE_CORAL,
        BRAIN_CORAL,
        HORN_CORAL,
        TUBE_CORAL,
        BUBBLE_CORAL;

        public static final Other[] VALUES = Other.values();
    }
}
