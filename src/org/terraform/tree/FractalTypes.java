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
        BROWN_GIANT_MUSHROOM,
        RED_GIANT_MUSHROOM;

        public static final Mushroom[] VALUES = Mushroom.values();
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
