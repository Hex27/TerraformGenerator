package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SandyBeachHandler extends BiomeHandler {

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
                Material.SAND,
                Material.SAND,
                GenUtils.randChoice(rand,
                        Material.SANDSTONE,
                        Material.SAND,
                        Material.SAND,
                        Material.SAND,
                        Material.SAND,
                        Material.SAND
                ),
                GenUtils.randChoice(rand, Material.SANDSTONE, Material.STONE),
                GenUtils.randChoice(rand, Material.SANDSTONE, Material.STONE)
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
    public void populateLargeItems(@NotNull TerraformWorld tw, Random random, @NotNull PopulatorDataAbstract data) {

        SimpleLocation[] coconutTrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 20);

        // Big trees and giant mushrooms
        for (SimpleLocation sLoc : coconutTrees) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()
                && (BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()))
                    || data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()) == Material.SAND))
            {
                TreeDB.spawnCoconutTree(tw, data, sLoc.getX(), sLoc.getY() + 1, sLoc.getZ());
            }
        }

    }
}
