package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeSection;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class DesertHillsHandler extends AbstractMountainHandler {
    @Override
    public boolean isOcean() {
        return false;
    }

    // Make these resemble dunes more, not massive mountains.
    @Override
    protected double getPeakMultiplier(@NotNull BiomeSection section, @NotNull Random sectionRandom) {
        return GenUtils.randDouble(sectionRandom, 1.1, 1.3);
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.DESERT;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.SAND,
                Material.SAND,
                GenUtils.randChoice(rand, Material.SANDSTONE, Material.SAND),
                GenUtils.randChoice(rand, Material.SANDSTONE, Material.SAND),
                GenUtils.randChoice(rand, Material.SANDSTONE, Material.SAND),
                Material.SANDSTONE,
                Material.SANDSTONE,
                Material.SANDSTONE,
                Material.SANDSTONE,
                Material.SANDSTONE,
                Material.SANDSTONE,
                GenUtils.randChoice(rand, Material.SANDSTONE, Material.SAND, Material.STONE),
                GenUtils.randChoice(rand, Material.SANDSTONE, Material.STONE)
        };
    }

    @Override
    public void populateSmallItems(TerraformWorld world,
                                   Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {

        FastNoise duneNoise = NoiseCacheHandler.getNoise(world, NoiseCacheEntry.BIOME_DESERT_DUNENOISE, tw -> {
            FastNoise n = new FastNoise((int) tw.getSeed());
            n.SetNoiseType(NoiseType.CubicFractal);
            n.SetFractalOctaves(3);
            n.SetFrequency(0.03f);
            return n;
        });

        for (int y = surfaceY; y > HeightMap.CORE.getHeight(world, rawX, rawZ); y--) {
            if (duneNoise.GetNoise(rawX, y, rawZ) > 0) {
                if (data.getType(rawX, y, rawZ) == Material.SAND || data.getType(rawX, y, rawZ) == Material.RED_SAND) {
                    if (TConfig.c.BIOME_DESERT_MOUNTAINS_YELLOW_CONCRETE_POWDER) {
                        data.setType(rawX, y, rawZ, Material.YELLOW_CONCRETE_POWDER);
                    }
                }
                else if (data.getType(rawX, y, rawZ) == Material.SANDSTONE
                         || data.getType(rawX, y, rawZ) == Material.RED_SANDSTONE)
                {
                    if (TConfig.c.BIOME_DESERT_MOUNTAINS_YELLOW_CONCRETE) {
                        data.setType(rawX, y, rawZ, Material.YELLOW_CONCRETE);
                    }
                }
            }
        }
    }

    @Override
    public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        // TODO Auto-generated method stub

    }
}
