package org.terraform.structure.village.plains.house;

import java.util.Random;

public enum PlainsVillageHouseVariant {
    WOODEN,
    CLAY,
    COBBLESTONE;

    public static PlainsVillageHouseVariant roll(Random rand) {
        int index = rand.nextInt(PlainsVillageHouseVariant.values().length);
        return PlainsVillageHouseVariant.values()[index];
    }
}
