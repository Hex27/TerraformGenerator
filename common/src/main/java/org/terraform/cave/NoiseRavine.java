package org.terraform.cave;

import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

public class NoiseRavine extends NoiseCaveAbstract {
    private static final int RAVINE_DEPTH = 50;

    /**
     * Use similar logic to Rivers to carve ravines, filtered against Y-height relative to the sea.
     */
    @Override
    public boolean canCarve(@NotNull TerraformWorld tw, int rawX, int y, int rawZ, double height, float filter) {
        if (height < TerraformGenerator.seaLevel) {
            return false; // Hard filter.
        }
        if (y < height - RAVINE_DEPTH) {
            return false;
        }
        FastNoise ravineNoise = NoiseCacheHandler.getNoise(
                tw,
                NoiseCacheHandler.NoiseCacheEntry.CAVE_XRAVINE_NOISE,
                world -> {
                    FastNoise n = new FastNoise(tw.getHashedRand(458930, 16328, 54981).nextInt());
                    n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
                    n.SetFrequency(0.007f);
                    n.SetFractalOctaves(3);
                    return n;
                }
        );

        // Stretch caves vertically so that they're not excessively spherical
        float ravine = ravineNoise.GetNoise(3 * rawX, y * 0.4f, 3 * rawZ);
        // Multiply by a filter that varies with depth relative to height.
        // At depth 50 blocks, force to 0
        ravine *= (float) (filter * 0.5885 * Math.log(RAVINE_DEPTH + 1 - (height - y))); // ASSUMPTION: y <= height.
        return ravine <= -1.3f;
    }
    //    private static float smallest = 0;
}
