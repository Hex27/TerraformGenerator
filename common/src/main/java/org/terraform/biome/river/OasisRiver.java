package org.terraform.biome.river;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.beach.OasisBeach;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

/**
 * Functions for generating lush rivers in badlands and deserts
 */
public class OasisRiver {
    /**
     * @return true if (x, z) is inside oasis
     */
    private static boolean isOasisRiver(TerraformWorld tw, int x, int z, BiomeBank targetBiome) {
        double lushRiverNoiseValue = OasisBeach.getOasisNoise(tw, x, z);
        int height = HeightMap.getBlockHeight(tw, x, z);
        BiomeBank biome = BiomeBank.calculateHeightIndependentBiome(tw, x, z);

        return lushRiverNoiseValue > OasisBeach.oasisThreshold
               && height < TerraformGenerator.seaLevel
               && targetBiome == biome;
    }

    public static void generateOasisRiver(TerraformWorld tw,
                                          @NotNull Random random,
                                          @NotNull PopulatorDataAbstract data,
                                          int x,
                                          int z,
                                          BiomeBank targetBiome)
    {
        if (!isOasisRiver(tw, x, z, targetBiome)) {
            return;
        }

        int riverBottom = GenUtils.getHighestGround(data, x, z);

        // Lily pads
        JungleRiverHandler.generateLilyPad(tw, random, data, x, z, riverBottom);

        // Kelp and sea grass
        if (random.nextInt(4) == 0) {
            JungleRiverHandler.generateKelp(x, riverBottom + 1, z, data, random);
        }
        else if (random.nextInt(5) == 0) {
            if (random.nextBoolean()) {
                data.setType(x, riverBottom + 1, z, Material.SEAGRASS);
            }
            else if (riverBottom + 1 < TerraformGenerator.seaLevel) {
                BlockUtils.setDoublePlant(data, x, riverBottom + 1, z, Material.TALL_SEAGRASS);
            }
        }
        else if (random.nextInt(13) == 0) {
            data.setType(x, riverBottom + 1, z, Material.SEA_PICKLE);
        }
    }
}
