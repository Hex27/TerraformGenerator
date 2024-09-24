package org.terraform.populators;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.ArrayList;
import java.util.Random;

public class AmethystGeodePopulator {

    private final int geodeRadius;
    private final double frequency;
    private final int minDepth;
    private final int minDepthBelowSurface;


    public AmethystGeodePopulator(int geodeRadius, double frequency, int minDepth, int minDepthBelowSurface) {
        this.geodeRadius = geodeRadius;
        this.frequency = TConfig.c.FEATURE_ORES_ENABLED ? frequency : 0;
        this.minDepth = minDepth;
        this.minDepthBelowSurface = minDepthBelowSurface;
    }

    public static void placeGeode(int seed, float r, @NotNull SimpleBlock block) {
        if (r <= 1) {
            return;
        }
        ArrayList<SimpleBlock> amethystBlocks = new ArrayList<>();
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -r; x <= r; x++) {
            for (float y = -r; y <= r; y++) {
                for (float z = -r; z <= r; z++) {
                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    double outerCrust = (Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) / Math.pow(r, 2);
                    double innerCrust = (Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) / Math.pow(r - 1.0, 2);
                    double amethystCrust = (Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) / Math.pow(r - 2.2, 2);
                    double airHollower = (Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) / Math.pow(r - 3.3, 2);

                    double noiseVal = 1 + 0.4 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ());

                    if (airHollower <= noiseVal) {
                        if (BlockUtils.isWet(rel)) {
                            rel.setType(Material.WATER);
                        }
                        else {
                            rel.setType(Material.CAVE_AIR);
                        }
                        // Only do the other stuff if this isn't air.
                    }
                    else if (rel.isSolid()) {
                        if (amethystCrust <= noiseVal) {
                            rel.setType(Material.AMETHYST_BLOCK, Material.BUDDING_AMETHYST);
                            amethystBlocks.add(rel);
                        }
                        else if (innerCrust <= noiseVal) {
                            rel.setType(Material.CALCITE);
                        }
                        else if (outerCrust <= noiseVal) {
                            rel.setType(Material.SMOOTH_BASALT);
                        }
                    }
                }
            }
        }

        // Place crystals
        for (SimpleBlock rel : amethystBlocks) {
            for (BlockFace face : BlockUtils.sixBlockFaces) {
                if (GenUtils.chance(1, 6)) {
                    continue;
                }
                SimpleBlock target = rel.getRelative(face);
                if (BlockUtils.isAir(target.getType()) && GenUtils.chance(1, 6)) {
                    new DirectionalBuilder(Material.AMETHYST_CLUSTER).setFacing(face).apply(target);
                }
            }
        }
    }

    public void populate(@NotNull TerraformWorld world, @NotNull Random random, @NotNull PopulatorDataAbstract data) {
        if (GenUtils.chance(random, (int) (frequency * 10000.0), 10000)) {
            int x = GenUtils.randInt(random, 0, 15) + data.getChunkX() * 16;
            int z = GenUtils.randInt(random, 0, 15) + data.getChunkZ() * 16;
            int upperHeightRange = GenUtils.getHighestGround(data, x, z) - minDepthBelowSurface;
            if (upperHeightRange > minDepth) {
                upperHeightRange = minDepth;
            }

            upperHeightRange = Math.min(world.getBiomeBank(x, z).getHandler().getMaxHeightForCaves(world, x, z),
                    upperHeightRange
            );

            if (upperHeightRange < 14) {
                return;
            }

            // Elevate 14 units up.
            int y = GenUtils.randInt(random, 14, upperHeightRange);
            placeGeode(random.nextInt(9999), geodeRadius, new SimpleBlock(data, x, y, z));
        }
    }
}
