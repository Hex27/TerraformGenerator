package org.terraform.tree;

import org.bukkit.Material;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MushroomCapMaker {
    public static void spawnRoundCap(int seed, float r, SimpleBlock block, boolean hardReplace, Material... type) {
        Random rand = new Random(seed);
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(FastNoise.NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        float belowY = -0.3f * r;
        float lowThreshold = Math.min((float) (0.6 / 5 * r), 0.6f); // When radius < 5 mushrooms less hollow

        for (int x = Math.round(-r); x <= Math.round(r); x++) {
            for (int y = Math.round(belowY); y <= Math.round(r); y++) {
                for (int z = Math.round(-r); z <= Math.round(r); z++) {
                    float factor = 1 - y / belowY;

                    if (y < 0 && factor + 0.5 * Math.abs(noise.GetNoise(x, y, z)) < 0.6) {
                        continue;
                    }

                    SimpleBlock rel = block.getRelative(x, y, z);
                    double equationResult = Math.pow(x, 2) / Math.pow(r, 2)
                            + Math.pow(y, 2) / Math.pow(r, 2)
                            + Math.pow(z, 2) / Math.pow(r, 2);
                    if (equationResult <= 1 + 0.25 * Math.abs(noise.GetNoise(rel.getX(), rel.getY(), rel.getZ()))
                            && equationResult >= lowThreshold) {

                        if (hardReplace || !rel.getType().isSolid()) {
                            rel.setType(GenUtils.randMaterial(rand, type));
                            BlockUtils.correctSurroundingMushroomData(rel);
                        }
                    }
                }
            }
        }
    }

    public static void spawnSphericalCap(int seed, float r, float ry, SimpleBlock block, boolean hardReplace, Material... type) {
        Random rand = new Random(seed);
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(FastNoise.NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        float belowY = -0.45f * ry;

        for (int x = Math.round(-r); x <= Math.round(r); x++) {
            for (int y = Math.round(belowY); y <= Math.round(ry); y++) {
                for (int z = Math.round(-r); z <= Math.round(r); z++) {
                    float factor = 1 - y / belowY;

                    if (y < 0 && factor + 0.5 * Math.abs(noise.GetNoise(x, y, z)) < 0.6) {
                        continue;
                    }

                    SimpleBlock rel = block.getRelative(x, y, z);
                    double equationResult = Math.pow(x, 2) / Math.pow(r, 2)
                            + Math.pow(y, 2) / Math.pow(ry, 2)
                            + Math.pow(z, 2) / Math.pow(r, 2);
                    if (equationResult <= 1 + 0.25 * Math.abs(noise.GetNoise(rel.getX(), rel.getY(), rel.getZ()))
                            && equationResult >= 0.4) {

                        if (hardReplace || !rel.getType().isSolid()) {
                            rel.setType(GenUtils.randMaterial(rand, type));
                            BlockUtils.correctSurroundingMushroomData(rel);
                        }
                    }
                }
            }
        }
    }
}
