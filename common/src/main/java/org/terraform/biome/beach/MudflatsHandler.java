package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.V_1_19;

import java.util.Random;

public class MudflatsHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return V_1_19.MANGROVE_SWAMP;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                GenUtils.weightedRandomMaterial(rand, V_1_19.MUD, 35, Material.GRASS_BLOCK, 10),
                GenUtils.randChoice(rand, Material.DIRT),
                GenUtils.randChoice(rand, Material.DIRT),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }

    @Override
    public void populateSmallItems(TerraformWorld tw,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        surfaceY++;
        if (data.getType(rawX, surfaceY, rawZ) != Material.AIR) {
            return;
        }
        if (GenUtils.chance(5, 100)) {
            if (random.nextBoolean()) {
                PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY, rawZ);
            }
            else {
                PlantBuilder.GRASS.build(data, rawX, surfaceY, rawZ);
            }
        }
    }

    @Override
    public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        // does nothing
    }
}
