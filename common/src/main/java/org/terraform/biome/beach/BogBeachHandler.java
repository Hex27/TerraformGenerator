package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.biome.flat.MuddyBogHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class BogBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.SWAMP;
    }

    @Override
    public @NotNull CustomBiomeType getCustomBiome() {
        return CustomBiomeType.MUDDY_BOG;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.GRASS_BLOCK,
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
        SimpleBlock block = new SimpleBlock(data, rawX, surfaceY, rawZ);
        if (!BlockUtils.isWet(block.getUp())) {
            if (GenUtils.chance(random, 1, 85)) {
                PlantBuilder.DEAD_BUSH.build(block.getUp());
            }
            else if (GenUtils.chance(random, 1, 85)) {
                PlantBuilder.BROWN_MUSHROOM.build(block.getUp());
            }
            else if (GenUtils.chance(random, 1, 85)) {
                PlantBuilder.GRASS.build(block.getUp());
            }
            else if (GenUtils.chance(random, 1, 85)) {
                PlantBuilder.TALL_GRASS.build(block.getUp());
            }
            else { // Possible Sugarcane
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    if (GenUtils.chance(random, 1, 75) && BlockUtils.isWet(block.getRelative(face))) {
                        PlantBuilder.SUGAR_CANE.build(block.getUp(), random, 2, 5);
                    }
                }
            }

        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {
        new MuddyBogHandler().populateLargeItems(tw, random, data);
    }
}
