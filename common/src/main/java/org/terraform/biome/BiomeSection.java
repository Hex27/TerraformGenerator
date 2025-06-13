package org.terraform.biome;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.*;

public class BiomeSection {
    // A BiomeSection is 128 blocks wide (Default of bitshift 7).
    public static final int bitshifts = TConfig.c.BIOME_SECTION_BITSHIFTS;
    public static final int sectionWidth = 1 << bitshifts;
    public static final int minSize = sectionWidth;
    public static final int dominanceThreshold = (int) (0.35 * sectionWidth);
    public static final int dominanceThresholdSquared = dominanceThreshold*dominanceThreshold;
    private final int x;
    private final int z;
    private final TerraformWorld tw;
    private float temperature;
    private float moisture;
    private int radius;
    private @Nullable BiomeBank biome;
    private FastNoise shapeNoise;

    /**
     * Block x and z
     */
    protected BiomeSection(TerraformWorld tw, int x, int z) {
        this.x = x >> bitshifts;
        this.z = z >> bitshifts;
        this.tw = tw;
    }

    protected BiomeSection(TerraformWorld tw, int x, int z, boolean useSectionCoords) {
        this.x = x;
        this.z = z;
        this.tw = tw;
    }

    /**
     * @return the width * width closest biome sections to this block point.
     */
    public static @NotNull Collection<BiomeSection> getSurroundingSections(TerraformWorld tw,
                                                                           int width,
                                                                           int blockX,
                                                                           int blockZ)
    {
        BiomeSection homeSection = BiomeBank.getBiomeSectionFromBlockCoords(tw, blockX, blockZ);
        Collection<BiomeSection> sections = new ArrayList<>();

        SimpleLocation center = homeSection.getCenter();
        int startX, startZ;
        if (width % 2 == 1) {
            startX = startZ = -width / 2;
        }
        else {
            startX = blockX >= center.getX() ? -width / 2 - 1 : -width / 2;
            startZ = blockZ >= center.getZ() ? -width / 2 - 1 : -width / 2;
        }

        for (int rx = startX; rx < startX + width; rx++) {
            for (int rz = startZ; rz < startZ + width; rz++) {
                sections.add(homeSection.getRelative(rx, rz));
            }
        }

        if (sections.size() != width * width) {
            TerraformGeneratorPlugin.logger.error("Section size was not " + (width * width) + ".");
        }

        return sections;
    }

    /**
     * @return the four closest biome sections to this block point
     */
    public static @NotNull Collection<BiomeSection> getSurroundingSections(TerraformWorld tw, int blockX, int blockZ) {
        Collection<BiomeSection> sections = new ArrayList<>();

        BiomeSection homeBiome = BiomeBank.getBiomeSectionFromBlockCoords(tw, blockX, blockZ);
        sections.add(homeBiome);

        SimpleLocation center = homeBiome.getCenter();
        if (blockX >= center.getX()) {
            if (blockZ >= center.getZ()) {
                sections.add(homeBiome.getRelative(1, 0));
                sections.add(homeBiome.getRelative(1, 1));
                sections.add(homeBiome.getRelative(0, 1));
            }
            else {
                sections.add(homeBiome.getRelative(1, 0));
                sections.add(homeBiome.getRelative(1, -1));
                sections.add(homeBiome.getRelative(0, -1));
            }
        }
        else {
            if (blockZ >= center.getZ()) {
                sections.add(homeBiome.getRelative(-1, 0));
                sections.add(homeBiome.getRelative(-1, 1));
                sections.add(homeBiome.getRelative(0, 1));
            }
            else {
                sections.add(homeBiome.getRelative(-1, 0));
                sections.add(homeBiome.getRelative(-1, -1));
                sections.add(homeBiome.getRelative(0, -1));
            }

        }
        return sections;
    }

    public static @NotNull BiomeSection getMostDominantSection(@NotNull TerraformWorld tw, int x, int z) {

        double dither = TConfig.c.BIOME_DITHER;
        Random locationBasedRandom = new Random(Objects.hash(tw.getSeed(), x, z));
        SimpleLocation target = new SimpleLocation(x, 0, z);
        BiomeSection homeSection = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);

        // Don't calculate if distance is very close to center
        if (target.distanceSqr(homeSection.getCenter()) <= dominanceThresholdSquared) {
            return homeSection;
        }

        Collection<BiomeSection> sections = BiomeSection.getSurroundingSections(tw, x, z);
        BiomeSection mostDominant = homeSection;

        for (BiomeSection sect : sections) {
            float dom = (float) (sect.getDominance(target) + GenUtils.randDouble(locationBasedRandom, -dither, dither));

            if (dom > mostDominant.getDominance(target) + GenUtils.randDouble(locationBasedRandom, -dither, dither)) {
                mostDominant = sect;
            }
        }

        return mostDominant;
    }

    protected void doCalculations() {
        this.radius = GenUtils.randInt(getSectionRandom(), minSize / 2, 5 * minSize / 4);
        this.shapeNoise = new FastNoise(Objects.hash(tw.getSeed(), x, z));
        shapeNoise.SetNoiseType(NoiseType.SimplexFractal);
        shapeNoise.SetFractalOctaves(3);
        shapeNoise.SetFrequency(0.01f);
        this.biome = this.parseBiomeBank();
    }

    public @NotNull Random getSectionRandom() {
        return new Random(Objects.hash(tw.getSeed(), x, z));
    }

    public @NotNull Random getSectionRandom(int multiplier) {
        return new Random((long) multiplier * Objects.hash(tw.getSeed(), x, z));
    }

    public @NotNull BiomeSection getRelative(int x, int z) {
        return BiomeBank.getBiomeSectionFromSectionCoords(this.tw, this.x + x, this.z + z, true);
    }

    public @NotNull BiomeSection getRelative(BiomeSubSection subSect) {
        return getRelative(subSect.relX, subSect.relZ);
    }

    public @NotNull BiomeBank getBiomeBank() {
        assert biome != null;
        return biome;
    }

    private @NotNull BiomeBank parseBiomeBank() {
        temperature = 3f * 2.5f * tw.getTemperatureOctave().GetNoise(this.x, this.z);
        moisture = 3f * 2.5f * tw.getMoistureOctave().GetNoise(this.x, this.z);

        return BiomeBank.selectBiome(
                this,
                temperature,
                moisture
        );
    }

    /**
     * Will be used to calculate which biome section has dominance in a certain
     * block
     */
    public float getDominance(@NotNull SimpleLocation target) {
        return getDominanceBasedOnRadius(target.getX(), target.getZ());
    }

    public float getDominanceBasedOnRadius(int blockX, int blockZ) {
        SimpleLocation center = this.getCenter();

        int xOffset = center.getX() - blockX;
        int zOffset = center.getZ() - blockZ;

        double equationResult = Math.pow(xOffset, 2) / Math.pow(radius, 2)
                                + Math.pow(zOffset, 2) / Math.pow(radius, 2)
                                + 0.7 * shapeNoise.GetNoise(xOffset, zOffset);

        return (float) (1 - 1 * (equationResult));

    }

    public @NotNull SimpleLocation getCenter() {
        int x = ((this.x << bitshifts)) + sectionWidth / 2;
        int z = ((this.z << bitshifts)) + sectionWidth / 2;
        return new SimpleLocation(x, 0, z);
    }

    /**
     * @return Block coords of lowest coord pair in the section's square
     */
    public @NotNull SimpleLocation getLowerBounds() {
        int x = ((this.x << bitshifts));
        int z = ((this.z << bitshifts));
        return new SimpleLocation(x, 0, z);
    }

    /**
     * @return Block coords of highest coord pair in the section's square
     */
    public @NotNull SimpleLocation getUpperBounds() {
        int x = ((this.x << bitshifts)) + sectionWidth;
        int z = ((this.z << bitshifts)) + sectionWidth;
        return new SimpleLocation(x, 0, z);
    }

    /**
     * @param radius in biomesection coords
     * @return surrounding biome sections at radius distance away
     */
    public @NotNull Collection<BiomeSection> getRelativeSurroundingSections(int radius) {
        if (radius == 0) {
            BiomeSection target = this;
            return List.of(target);
        }
        //     xxxxx
        // xxx  x   x
        // xox  x o x
        // xxx  x   x
        //     xxxxx
        ArrayList<BiomeSection> candidates = new ArrayList<>();

        // Lock rX, iterate rZ
        for (int rx : new int[] {-radius, radius}) {
            for (int rz = -radius; rz <= radius; rz++) {
                candidates.add(this.getRelative(rx, rz));
            }
        }

        // Lock rZ, iterate rX
        for (int rz : new int[] {-radius, radius}) {
            for (int rx = 1 - radius; rx <= radius - 1; rx++) {
                candidates.add(this.getRelative(rx, rz));
            }
        }

        return candidates;
    }

    /**
     * @return the subsection within this biome section that the coordinates belong in.
     * Works even if the coords are outside the biome section.
     *
     * 12/6/2025 WHAT THE FUCK IS THIS
     */
    public @NotNull BiomeSubSection getSubSection(int rawX, int rawZ) {
        // if(new BiomeSection(tw, rawX, rawZ).equals(this)) {
        SimpleLocation sectionCenter = this.getCenter();
        int relXFromCenter = rawX - sectionCenter.getX();
        int relZFromCenter = rawZ - sectionCenter.getZ();

        if (relXFromCenter > 0) {
            if (relXFromCenter >= Math.abs(relZFromCenter)) {
                return BiomeSubSection.POSITIVE_X;
            }
        }

        if (relXFromCenter <= 0) {
            if (Math.abs(relXFromCenter) >= Math.abs(relZFromCenter)) {
                return BiomeSubSection.NEGATIVE_X;
            }
        }

        if (relZFromCenter > 0) {
            if (relZFromCenter >= Math.abs(relXFromCenter)) {
                return BiomeSubSection.POSITIVE_Z;
            }
        }

        if (relZFromCenter <= 0) {
            if (Math.abs(relZFromCenter) >= Math.abs(relXFromCenter)) {
                return BiomeSubSection.NEGATIVE_Z;
            }
        }

        return BiomeSubSection.NONE;

        // }

    }

    @Override
    public int hashCode() {
        int prime = 13;
        int result = 5;

        result = prime * result + x;
        result = prime * result + z;
        result = prime * result + tw.getName().hashCode();

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BiomeSection other) {
            return this.tw.getName().equals(other.tw.getName())
                   && this.x == other.x
                   && this.z == other.z;
        }
        return false;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public @NotNull String toString() {
        return "(" + x + "," + z + ")";
    }

    public @NotNull BiomeClimate getClimate() {
        return BiomeClimate.selectClimate(temperature, moisture);
    }

    public float getTemperature() {
        return temperature;
    }

    public float getMoisture() {
        return moisture;
    }

    public TerraformWorld getTw() {
        return tw;
    }

    public double getOceanLevel() {
        return tw.getOceanicNoise().GetNoise(x, z) * 50.0;
    }

    public double getMountainLevel() {
        return tw.getMountainousNoise().GetNoise(x, z) * 50.0;
    }
}
