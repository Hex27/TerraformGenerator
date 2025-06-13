package org.terraform.biome;

// Used for representing where in a biome section a particular point is.
// A biome subsection is 1/4 of a biome section, in the form of a triangle.

// Currently for mountains to detect their lowerbound heights.
public enum BiomeSubSection {
    POSITIVE_X(1, 0),
    NEGATIVE_X(-1, 0),
    POSITIVE_Z(0, 1),
    NEGATIVE_Z(0, -1),
    NONE(0, 0);
    public final int relX;
    public final int relZ;
    BiomeSubSection(int relX,int relZ){
        this.relX = relX;
        this.relZ = relZ;
    }
}
