package org.terraform.cave;

import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;
import org.terraform.utils.noise.BezierCurve;
import org.terraform.utils.noise.BresenhamLine;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

import java.util.Random;

public class NoiseRavine extends NoiseCaveAbstract{
    /**
     * Use similar logic to Rivers to carve ravines, filtered against Y-height relative to the sea.
     */
    @Override
    public boolean canCarve(TerraformWorld tw, int rawX, int y, int rawZ, double height, float filter) {
        if(height < TerraformGenerator.seaLevel || filter == 0) return false; //Hard filter.

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

        //Use this method, but treat the chunks as blocks.
        //This way, you get a Vector2f of chunks containing ravines.
        Vector2f[] ravineOrigins = GenUtils.vectorRandomObjectPositions(tw.getHashedRand(156274631,456912,23458).nextInt(),
                rawX >> 8, rawZ >> 8, 7, 2);

        //ravineOrigin contains *chunk coordinates*, not block coordinates
        for(Vector2f ravineOrigin:ravineOrigins){
            //With ravineOrigin's coordinates as the seed, calculate the needed components
            //to procedurally re-form a BezierCurve
            Random ravineRandom = tw.getHashedRand(13278,(int)ravineOrigin.x,(int)ravineOrigin.y);
            double ravineRadius = GenUtils.randInt(ravineRandom,15,25); //In blocks
            double angleOne = 2*Math.PI*ravineRandom.nextDouble();
            double angleTwo = 2*Math.PI*ravineRandom.nextDouble();

            //Form and iterate Bezier Curve with the chunk as the centre.
            //These are chunk coords, convert them before use
            Vector2f p1 =  new Vector2f((float) (ravineRadius*Math.cos(angleOne)) + ravineOrigin.x,
                    (float) (ravineRadius*Math.sin(angleOne)) + ravineOrigin.y);

            Vector2f p2 = new Vector2f((float) (ravineRadius*Math.cos(angleTwo)) + ravineOrigin.x,
                    (float) (ravineRadius*Math.sin(angleTwo)) + ravineOrigin.y);

            //For each block within the curve, check if anything can be carved in the ravine
            for(float progress = 0; progress <= 1; progress += 0.0625){
                Vector2f target = BezierCurve.quadratic(progress,p1,ravineOrigin,p2);
                int blockX = ((int) target.x)*16 + (int)((target.x-((int)target.x))*16);
                int blockZ = ((int) target.y)*16 + (int)((target.y-((int)target.y))*16);
                if(blockX != rawX || blockZ != rawZ) continue;

                if(progress )

            }
        }

        return false;
    }
}
