package org.terraform.utils.datastructs;

import org.terraform.main.TerraformGeneratorPlugin;

/**
 * ALL INTEGERS HERE ARE 0-15 INCLUSIVE.
 *
 * This datastructure stores 16x16xworldheight booleans, associated with x,y,z coord pairs.
 */
public class CompressedChunkBools {
    /*
        Each short is 16bits. This totals to 256 bits per y-layer.

        A dirty hack only a mother could love.

        Based on a test where at most 5% of mountain slopes
        have little flat areas at the max (320) height:

            Server: paper-1.21.5-93
            Seed: -1565193744182814265
            WorldBorder: 4,050 x 4,050
            TerraformGenerator: 19.1.1

            plugins/TerraformGenerator/config.yml
                land-height-amplifier: 1.7
                temperature-frequency: 0.015
                moisture-frequency: 0.015
                oceanic-threshold: 11.0

        (y - TerraformGeneratorPlugin.injector.getMinY()) may be up to 418.

        So, we need 418 - 385 + 1  = 34 of additional matrix length to
        avoid java.lang.ArrayIndexOutOfBoundsException; 50 with headroom.
     */
    short[][] matrix = new short[50 + TerraformGeneratorPlugin.injector.getMaxY() - TerraformGeneratorPlugin.injector.getMinY() + 1][16];

    public void set(int x, int y, int z){
        matrix[y-TerraformGeneratorPlugin.injector.getMinY()][x] |= 0b1 << z;
    }
    public void unSet(int x, int y, int z){
        matrix[y-TerraformGeneratorPlugin.injector.getMinY()][x] &= 255 ^ (0b1 << z);
    }

    public boolean isSet(int x, int y, int z){
        return (matrix[y-TerraformGeneratorPlugin.injector.getMinY()][x] & (0b1 << z)) > 0;
    }
}
