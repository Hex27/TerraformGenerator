package org.terraform.cave;

import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;
import org.terraform.utils.noise.BezierCurve;
import org.terraform.utils.noise.BresenhamLine;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

import java.util.Random;

public class NoiseRavine extends NoiseCaveAbstract{
    private static final int RAVINE_DEPTH = 50;
    /**
     * Use similar logic to Rivers to carve ravines, filtered against Y-height relative to the sea.
     */
    @Override
    public boolean canCarve(TerraformWorld tw, int rawX, int y, int rawZ, double height, float filter) {
        if(height < TerraformGenerator.seaLevel) return false; //Hard filter.
        if(y < height-RAVINE_DEPTH) return false;
        FastNoise ravineNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheHandler.NoiseCacheEntry.CAVE_XRAVINE_NOISE, world -> {
            FastNoise n = new FastNoise(tw.getHashedRand(458930,16328,54981).nextInt());
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFrequency(0.007f);
            n.SetFractalOctaves(3);
            return n;
        });
        FastNoise ravineFilter = NoiseCacheHandler.getNoise(tw, NoiseCacheHandler.NoiseCacheEntry.CAVE_XRAVINE_DETAILS, world -> {
            FastNoise n = new FastNoise(tw.getHashedRand(156274631,456912,23458).nextInt());
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFrequency(0.03f);
            n.SetFractalOctaves(2);
            return n;
        });

        float xzStretcher = ravineFilter.GetNoise(rawX,rawZ);
        float sign = xzStretcher/Math.abs(xzStretcher);
        xzStretcher = sign*0.5f*Math.min(1, Math.max(0,Math.abs(xzStretcher)));

        //Stretch caves vertically so that they're not excessively spherical
        float ravine = ravineNoise.GetNoise(3*rawX,y*0.4f,3*rawZ);
        //Multiply by a filter that varies with depth relative to height.
        //At depth 50 blocks, force to 0
        ravine *= filter*0.5885*Math.log(RAVINE_DEPTH+1-(height-y)); //ASSUMPTION: y <= height.
//        if(ravine < smallest){
//            smallest = ravine;
//            TerraformGeneratorPlugin.logger.info("SMALLEST: " + smallest);
//        }
        return ravine <= -1.3f;
    }
//    private static float smallest = 0;
}
