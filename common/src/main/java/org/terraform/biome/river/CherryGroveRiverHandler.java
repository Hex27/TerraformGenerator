package org.terraform.biome.river;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class CherryGroveRiverHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.RIVER;
    }

    @Override
    public @NotNull CustomBiomeType getCustomBiome() {
        return CustomBiomeType.CHERRY_GROVE;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.DIRT,
                Material.DIRT,
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE, Material.DIRT),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }


    @Override
    public void populateSmallItems(@NotNull TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        if (surfaceY >= TerraformGenerator.seaLevel) // Don't apply to dry land
        {
            return;
        }

        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) {
            return;
        }

        // SEA GRASS/KELP
        RiverHandler.riverVegetation(world, random, data, rawX, surfaceY, rawZ);

        // Generate clay
        if (GenUtils.chance(random, TConfig.c.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND, 1000)) {
            BlockUtils.generateClayDeposit(rawX, surfaceY, rawZ, data, random);
        }
    }

    @Override
    public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        // TODO Auto-generated method stub

    }


}
