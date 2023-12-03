package org.terraform.cave;

import org.terraform.data.TerraformWorld;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

public class XStretchedRavine extends NoiseCaveAbstract{
    /**
     * Ravine caves will use 2D noise to check if an abstract X/Z coord will carve
     * a cave of depth noiseVal.
     * <br>
     * Thus, ravines will IGNORE surface filter.
     * <br>
     * This particular variant will stretch the X coordinate massively to achieve
     * the signature long ravine look
     */
    @Override
    public boolean canCarve(TerraformWorld tw, int rawX, int y, int rawZ, double height, float surfaceFilter) {
        FastNoise cheeseNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheHandler.NoiseCacheEntry.CAVE_XRAVINE_NOISE, world -> {
            FastNoise n = new FastNoise((int) (tw.getSeed() - 578912));
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFrequency(0.01f);
            n.SetFractalOctaves(2);
            return n;
        });

        //Stretch X-coord to make the caves long
        float carveOrNot = cheeseNoise.GetNoise(rawX*0.1f,rawZ);

        //No ravine in this location
        if(carveOrNot > -0.4f) return false;


        return false;
    }
}
