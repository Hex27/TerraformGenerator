package org.terraform.coregen;

import org.terraform.biome.BiomeSection;
import org.terraform.data.CoordPair;

import java.util.HashMap;

public record SectionBlurCache(BiomeSection sect, float[][] intermediate, float[][] blurred) {

    public void fillCache(){
        HashMap<CoordPair, Float> dominantBiomeHeights = new HashMap<>();

        // Box blur across the biome section
        // For every point in the biome section, blur across the X axis.
        for (int relX = sect.getLowerBounds().getX() - HeightMap.MASK_RADIUS;
             relX <= sect.getUpperBounds().getX() + HeightMap.MASK_RADIUS; relX++) {
            for (int relZ = sect.getLowerBounds().getZ() - HeightMap.MASK_RADIUS;
                 relZ <= sect.getUpperBounds().getZ() + HeightMap.MASK_RADIUS;
                 relZ++) {
                int arrIdX = (relX - (sect.getLowerBounds().getX() - HeightMap.MASK_RADIUS));
                int arrIdZ = (relZ - (sect.getLowerBounds().getZ() - HeightMap.MASK_RADIUS));
                float lineTotalHeight = 0;
                for (int offsetX = -HeightMap.MASK_RADIUS; offsetX <= HeightMap.MASK_RADIUS; offsetX++) {
                    lineTotalHeight += HeightMap.getDominantBiomeHeight(
                            sect.getTw(), relX + offsetX, relZ, dominantBiomeHeights);
                }

                // Temporarily cache these X-Blurred values into chunkcache.
                intermediate[arrIdX][arrIdZ] = lineTotalHeight;
            }
        }

        // For every point in the biome section, blur across the Z axis.
        for (int relX = sect.getLowerBounds().getX(); relX <= sect.getUpperBounds().getX(); relX++) {
            for (int relZ = sect.getLowerBounds().getZ(); relZ <= sect.getUpperBounds().getZ(); relZ++) {
                int arrIdX = (relX - (sect.getLowerBounds().getX() - HeightMap.MASK_RADIUS));
                int arrIdZ = (relZ - (sect.getLowerBounds().getZ() - HeightMap.MASK_RADIUS));

               float lineTotalHeight = 0;
                for (int offsetZ = -HeightMap.MASK_RADIUS; offsetZ <= HeightMap.MASK_RADIUS; offsetZ++) {
                    int querIdZ = (relZ + offsetZ - (sect.getLowerBounds().getZ() - HeightMap.MASK_RADIUS));
                    //wasted calculations get repeated on different sections at the boundary
                    // But not much we can do short of overhauling the system
                    lineTotalHeight += intermediate[arrIdX][querIdZ];
                }
                // final blurred value
                blurred[arrIdX][arrIdZ] = lineTotalHeight / HeightMap.MASK_VOLUME;
            }
        }
    }

    public float getBlurredHeight(int blockX, int blockZ){
        int arrIdX = (blockX - (sect.getLowerBounds().getX() - HeightMap.MASK_RADIUS));
        int arrIdZ = (blockZ - (sect.getLowerBounds().getZ() - HeightMap.MASK_RADIUS));
        return blurred[arrIdX][arrIdZ];
    }

    @Override
    public int hashCode(){
        return sect.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof SectionBlurCache s){
            return s.sect.equals(sect);
        }
        return false;
    }
}
