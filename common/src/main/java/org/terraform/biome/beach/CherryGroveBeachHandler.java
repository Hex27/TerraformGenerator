package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class CherryGroveBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.BEACH;
    }

    @Override
    public @NotNull CustomBiomeType getCustomBiome() {
        return CustomBiomeType.CHERRY_GROVE;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }

    @Override
    public void populateSmallItems(TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {

        boolean hasSugarcane = GenUtils.chance(random, 1, 100);

        Material base = data.getType(rawX, surfaceY, rawZ);

        // Remove submerged grass
        if (base == Material.GRASS_BLOCK && data.getType(rawX, surfaceY + 1, rawZ) == Material.WATER) {
            data.setType(rawX, surfaceY, rawZ, Material.DIRT);
        }

        if (base != Material.SAND && base != Material.GRASS_BLOCK) {
            return;
        }

        surfaceY++;

        // Spawn sugarcane
        if (hasSugarcane) {
            boolean hasWater = data.getType(rawX + 1, surfaceY - 1, rawZ) == Material.WATER;
            if (data.getType(rawX - 1, surfaceY - 1, rawZ) == Material.WATER) {
                hasWater = true;
            }
            if (data.getType(rawX, surfaceY - 1, rawZ + 1) == Material.WATER) {
                hasWater = true;
            }
            if (data.getType(rawX, surfaceY - 1, rawZ - 1) == Material.WATER) {
                hasWater = true;
            }

            if (hasWater) {
                PlantBuilder.SUGAR_CANE.build(random, data, rawX, surfaceY, rawZ, 3, 7);
            }
        }
    }

    @Override
    public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

    }
}
