package org.terraform.biome;

public enum BiomeType {
    OCEANIC(false),
    FLAT(true),
    MOUNTAINOUS(true),
    HIGH_MOUNTAINOUS(true),
    BEACH(true),
    DEEP_OCEANIC(false),
    RIVER(false),
    ;

    private final boolean isDry;

    BiomeType(boolean isDry) {
        this.isDry = isDry;
    }

    public boolean isDry() {
        return isDry;
    }
}
