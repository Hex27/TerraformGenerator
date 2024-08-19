package org.terraform.biome;

// Used for representing where in a biome section a particular point is.
// A biome subsection is 1/4 of a biome section, in the form of a triangle.

// Currently for mountains to detect their lowerbound heights.
public enum BiomeSubSection {
    POSITIVE_X, NEGATIVE_X, POSITIVE_Z, NEGATIVE_Z, NONE
}
