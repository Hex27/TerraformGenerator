package org.terraform.cave;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeSection;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

public class CheeseCave extends NoiseCaveAbstract {
    @Override
    public boolean canCarve(@NotNull TerraformWorld tw, int rawX, int y, int rawZ, double height, float surfaceFilter) {
        FastNoise cheeseNoise = NoiseCacheHandler.getNoise(
                tw,
                NoiseCacheHandler.NoiseCacheEntry.CAVE_CHEESE_NOISE,
                world -> {
                    FastNoise n = new FastNoise((int) (tw.getSeed() + 723891));
                    n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
                    n.SetFrequency(0.03f);
                    n.SetFractalOctaves(2);
                    return n;
                }
        );

        // Stretch caves horizontally so that they're not excessively spherical
        float cheese = cheeseNoise.GetNoise(rawX * 0.5f, y, rawZ * 0.5f);

        var mc = new MegaChunk(rawX, y, rawZ);
        var center = mc.getCenterBiomeSectionBlockCoords();
        float distSquared = (float) center.distanceSquared(rawX,rawZ);
        if(distSquared <= 40000f){ //40000 = 200*200
            //quadratically reduce as it gets closer to the structure
            cheese *= (1-TConfig.c.CAVES_STRUCTURE_SUPPRESSION_THRESHOLD)
                      +(TConfig.c.CAVES_STRUCTURE_SUPPRESSION_THRESHOLD) * (distSquared / 40000f);
        }

        return surfaceFilter * cheese <= -0.3f;
    }
}
