package org.terraform.data;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MegaChunk {
    public static final int megaChunkBlockWidth = BiomeSection.sectionWidth
                                                  * TConfig.c.STRUCTURES_MEGACHUNK_NUMBIOMESECTIONS;
    private int x, z;

    public MegaChunk(@NotNull SimpleChunkLocation sLoc) {
        this(sLoc.getX(), sLoc.getZ());
    }

    public MegaChunk(int x, int y, int z) {
        this.x = blockCoordsToMega(x);
        this.z = blockCoordsToMega(z);
    }

    // A megachunk consists of a bunch of biome sections.
    // The big structures spawn right in the middle of them.
    public MegaChunk(int chunkX, int chunkZ) {
        this(chunkX * 16, 0, chunkZ * 16);

    }

    private static int blockCoordsToMega(int coord) {
        if (coord >= 0) {
            //This used to be (int) (double) (coord / megaChunkBlockWidth); for whatever reason
            return coord / megaChunkBlockWidth;
        }
        else {
            return (int) (-1.0 * (Math.ceil(((double) Math.abs(coord)) / ((double) megaChunkBlockWidth))));
        }
    }

    /**
     * @return lower bounds of block coords within the megachunk.
     */
    private static int megaToBlockCoords(int coord) {
        return coord * (megaChunkBlockWidth);
    }

    public @NotNull MegaChunk getRelative(int x, int z) {
        MegaChunk mc = new MegaChunk(0, 0);
        mc.x = this.x + x;
        mc.z = this.z + z;
        return mc;
    }

    /**
     * @return A random pair of xz block coords within the mega chunk
     */
    public int[] getRandomCoords(@NotNull Random rand) {

        int lowX = megaToBlockCoords(this.x);
        int lowZ = megaToBlockCoords(this.z);
        int highX = lowX + megaChunkBlockWidth - 1;
        int highZ = lowZ + megaChunkBlockWidth - 1;

        // Pad the sides. Never generate on the side of a mega chunk.
        int x = GenUtils.randInt(rand, lowX + megaChunkBlockWidth / 10, highX - megaChunkBlockWidth / 10);
        int z = GenUtils.randInt(rand, lowZ + megaChunkBlockWidth / 10, highZ - megaChunkBlockWidth / 10);
        return new int[] {x, z};
    }

    /**
     * @return A random pair of xz block coords within the mega chunk. This pair of coords WILL be in the middle of a chunk.
     */
    public int[] getRandomCenterChunkBlockCoords(@NotNull Random rand) {

        int lowX = this.getLowerCornerChunkCoords()[0];
        int lowZ = this.getLowerCornerChunkCoords()[1];
        int highX = this.getUpperCornerChunkCoords()[0];
        int highZ = this.getUpperCornerChunkCoords()[1];

        // Pad the sides. Never generate on the side of a mega chunk.
        int x = GenUtils.randInt(rand, lowX, highX);
        int z = GenUtils.randInt(rand, lowZ, highZ);
        return new int[] {x * 16 + 7, z * 16 + 7};
    }

    public int[] getCenterBlockCoords() {

        int lowX = megaToBlockCoords(this.x);
        int lowZ = megaToBlockCoords(this.z);
        // TerraformGeneratorPlugin.logger.info("MC(" + this.x + "," + this.z + "):(" + (lowX + megaChunkBlockWidth/2) + "," + (lowZ + megaChunkBlockWidth/2) + ")");
        return new int[] {lowX + megaChunkBlockWidth / 2, lowZ + megaChunkBlockWidth / 2};
    }

    /**
     * Used for structure spawning. They need the center of biome sections.
     */
    public int[] getCenterBiomeSectionBlockCoords() {

        int lowX = getCenterBlockCoords()[0];
        int lowZ = getCenterBlockCoords()[1];

        int sectionX = lowX >> BiomeSection.bitshifts;
        int sectionZ = lowZ >> BiomeSection.bitshifts;

        int centerOfSectionX = (sectionX << BiomeSection.bitshifts) + BiomeSection.sectionWidth / 2;
        int centerOfSectionZ = (sectionZ << BiomeSection.bitshifts) + BiomeSection.sectionWidth / 2;

        // TerraformGeneratorPlugin.logger.info("MC(" + this.x + "," + this.z + "):(" + (lowX + megaChunkBlockWidth/2) + "," + (lowZ + megaChunkBlockWidth/2) + ")");
        return new int[] {centerOfSectionX, centerOfSectionZ};
    }

    public int[] getCenterBiomeSectionChunkCoords() {
        int[] coords = getCenterBiomeSectionBlockCoords();

        return new int[] {coords[0] >> 4, coords[1] >> 4};
    }

    public int[] getUpperCornerBlockCoords() {

        int upperX = megaToBlockCoords(this.x) + megaChunkBlockWidth - 1;
        int upperZ = megaToBlockCoords(this.z) + megaChunkBlockWidth - 1;
        return new int[] {upperX, upperZ};
    }

    public int[] getLowerCornerBlockCoords() {

        int lowX = megaToBlockCoords(this.x);
        int lowZ = megaToBlockCoords(this.z);
        return new int[] {lowX, lowZ};
    }

    public int[] getCenterChunkCoords() {
        int[] coords = getCenterBlockCoords();

        return new int[] {coords[0] >> 4, coords[1] >> 4};
    }

    public int[] getLowerCornerChunkCoords() {
        int[] coords = getLowerCornerBlockCoords();

        return new int[] {coords[0] >> 4, coords[1] >> 4};
    }

    public int[] getUpperCornerChunkCoords() {
        int[] coords = getUpperCornerBlockCoords();

        return new int[] {coords[0] >> 4, coords[1] >> 4};
    }

    public @NotNull BiomeSection getCenterBiomeSection(TerraformWorld tw) {
        int[] coords = getCenterBiomeSectionBlockCoords();
        return BiomeBank.getBiomeSectionFromBlockCoords(tw, coords[0], coords[1]);
    }

    public boolean containsXZBlockCoords(int x, int z) {
        MegaChunk mc = new MegaChunk(x, 0, z);
        return mc.equals(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MegaChunk megaChunk) {
            return this.x == megaChunk.x && this.z == megaChunk.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 5;

        result = prime * result + x;
        result = prime * result + z;

        return result;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public @NotNull String toString() {
        return x + "," + z;
    }
}
