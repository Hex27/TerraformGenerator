package org.terraform.cave;

import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

public class NoiseRavine extends NoiseCaveAbstract{
    /**
     * Use similar logic to Rivers to carve ravines, filtered against Y-height relative to the sea.
     */
    @Override
    public boolean canCarve(TerraformWorld tw, int rawX, int y, int rawZ, double height, float filter) {
        if(height < TerraformGenerator.seaLevel) return false; //Hard filter.

        FastNoise ravineNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheHandler.NoiseCacheEntry.CAVE_XRAVINE_NOISE, world -> {
            FastNoise n = new FastNoise(tw.getHashedRand(458930,16328,54981).nextInt());
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFrequency(0.005f);
            n.SetFractalOctaves(5);
            return n;
        });
        FastNoise ravineFilter = NoiseCacheHandler.getNoise(tw, NoiseCacheHandler.NoiseCacheEntry.CAVE_XRAVINE_DETAILS, world -> {
            FastNoise n = new FastNoise(tw.getHashedRand(156274631,456912,23458).nextInt());
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFrequency(0.002f);
            n.SetFractalOctaves(2);
            return n;
        });

        //Stretch Z-coord to make the caves long
        double ravineDepth = Math.min(1,Math.max(ravineFilter.GetNoise(rawX,rawZ),0))
                *(filter*(30 - 400 * Math.abs(ravineNoise.GetNoise(rawX,rawZ)))-22);

        //No ravine in this location
        if(ravineDepth < 0f) return false;

        //double detailsNoise = ravineDetails.GetNoise(rawX,rawZ);
        if(ravineDepth > 40)
        {
            //Taper off ravines that are too deep
            ravineDepth = 40 + 0.1*(ravineDepth-40);
        }
        //ASSUMPTION: Y <= height as guaranteed by caller
        return y > height-(ravineDepth);
    }
}
