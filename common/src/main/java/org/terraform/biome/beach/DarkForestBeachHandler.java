package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class DarkForestBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.BEACH;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.COARSE_DIRT,
                Material.COARSE_DIRT,
                GenUtils.randChoice(rand,
                        Material.STONE,
                        Material.COARSE_DIRT,
                        Material.COARSE_DIRT,
                        Material.COARSE_DIRT,
                        Material.COARSE_DIRT,
                        Material.COARSE_DIRT
                ),
                Material.STONE,
                Material.STONE
        };
    }

    @Override
    public void populateSmallItems(TerraformWorld world,
                                   Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   PopulatorDataAbstract data)
    {
    }

    @Override
    public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

    }
}
