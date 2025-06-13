package org.terraform.utils.datastructs;

import org.terraform.main.TerraformGeneratorPlugin;

/**
 * ALL INTEGERS HERE ARE 0-15 INCLUSIVE.
 *
 * This datastructure stores 16x16xworldheight booleans, associated with x,y,z coord pairs.
 */
public class CompressedChunkBools {
    //Each short is 16bits. This totals to 256 bits per y-layer
    short[][] matrix = new short[TerraformGeneratorPlugin.injector.getMaxY() - TerraformGeneratorPlugin.injector.getMinY() + 1][16];

    public void set(int x, int y, int z){
        int idY = y-TerraformGeneratorPlugin.injector.getMinY();
        matrix[idY][x] = (short) (matrix[idY][x] | (0b1 << z));
    }
    public void unSet(int x, int y, int z){
        int idY = y-TerraformGeneratorPlugin.injector.getMinY();
        matrix[idY][x] = (short) (matrix[idY][x] & (255 ^ (0b1 << z)));
    }

    public boolean isSet(int x, int y, int z){
        return (matrix[y-TerraformGeneratorPlugin.injector.getMinY()][x] & (0b1 << z)) > 0;
    }
}
