package org.terraform.structure.village.plains.house;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public enum PlainsVillageHouseVariant {
    WOODEN, CLAY, COBBLESTONE;

    public static PlainsVillageHouseVariant roll(@NotNull Random rand) {
        int index = rand.nextInt(PlainsVillageHouseVariant.values().length);
        return PlainsVillageHouseVariant.values()[index];
    }
}
