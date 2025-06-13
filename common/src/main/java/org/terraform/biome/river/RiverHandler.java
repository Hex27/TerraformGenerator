package org.terraform.biome.river;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class RiverHandler extends BiomeHandler {

    public static void riverVegetation(@NotNull TerraformWorld tw,
                                       @NotNull Random random,
                                       @NotNull PopulatorDataAbstract data,
                                       int rawX,
                                       int surfaceY,
                                       int rawZ)
    {
        boolean growsKelp = tw.getHashedRand(rawX >> 4, rawZ >> 4, 97418).nextBoolean();
        if (GenUtils.chance(random, 10, 100)) {
            generateSeagrass(rawX, surfaceY + 1, rawZ, data);
            if (random.nextBoolean()) {
                generateTallSeagrass(rawX, surfaceY + 1, rawZ, data);
            }
        }
        else if (GenUtils.chance(random, 3, 50) && growsKelp && surfaceY + 1 < TerraformGenerator.seaLevel - 10) {
            generateKelp(rawX, surfaceY + 1, rawZ, data, random);
        }
    }

    public static void generateSeagrass(int x, int y, int z, @NotNull PopulatorDataAbstract data) {
        if (data.getType(x, y, z) != Material.WATER) {
            return;
        }
        PlantBuilder.SEAGRASS.build(data, x, y, z);

    }

    public static void generateTallSeagrass(int x, int y, int z, @NotNull PopulatorDataAbstract data) {
        if (data.getType(x, y, z) != Material.WATER
            || data.getType(x, y+1, z) != Material.WATER) {
            return;
        }
        PlantBuilder.TALL_SEAGRASS.build(data, x, y, z);
    }

    private static void generateKelp(int x, int y, int z, @NotNull PopulatorDataAbstract data, Random random) {
        for (int ny = y; ny < TerraformGenerator.seaLevel - GenUtils.randInt(5, 15); ny++) {
            if (data.getType(x, ny, z) != Material.WATER) {
                break;
            }
            PlantBuilder.KELP_PLANT.build(data, x, ny, z);
        }
    }

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.RIVER;
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

        // Set ground near sea level to sand
        if (surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.SAND);
        }
        else if (surfaceY >= TerraformGenerator.seaLevel - 4) {
            if (random.nextBoolean()) {
                data.setType(rawX, surfaceY, rawZ, Material.SAND);
            }
        }

        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) {
            return;
        }

        // SEA GRASS/KELP
        riverVegetation(world, random, data, rawX, surfaceY, rawZ);

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
