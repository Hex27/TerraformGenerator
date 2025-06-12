package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.TreeDB;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SwampHandler extends BiomeHandler {

    @Override
    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.SWAMP;
    }

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.SWAMP;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                GenUtils.randChoice(rand, Material.GRASS_BLOCK, Material.PODZOL, Material.PODZOL),
                GenUtils.randChoice(rand, Material.DIRT),
                GenUtils.randChoice(rand, Material.DIRT, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return BiomeBank.MANGROVE.getHandler();
    }

    @Override
    public void populateSmallItems(TerraformWorld tw,
                                   Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   PopulatorDataAbstract data)
    {
        BiomeBank.MANGROVE.getHandler().populateSmallItems(tw, random, rawX, surfaceY, rawZ, data);
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {

        int treeX, treeY, treeZ;
        if (GenUtils.chance(random, 8, 10)) {
            treeX = GenUtils.randInt(random, 2, 12) + data.getChunkX() * 16;
            treeZ = GenUtils.randInt(random, 2, 12) + data.getChunkZ() * 16;

            if (data.getBiome(treeX, treeZ) == getBiome()) {
                treeY = GenUtils.getHighestGround(data, treeX, treeZ);

                if (treeY > TerraformGenerator.seaLevel - 6) {
                    // Don't do gradient checks for swamp trees, the mud is uneven.
                    // just make sure it's submerged
                    TreeDB.spawnBreathingRoots(tw, new SimpleBlock(data, treeX, treeY, treeZ), Material.OAK_LOG);
                    FractalTypes.Tree.SWAMP_TOP.build(tw, new SimpleBlock(data, treeX, treeY, treeZ), (t) -> {
                        t.setCheckGradient(false);
                        t.setRootMaterial(Material.OAK_WOOD);
                        t.setBranchMaterial(Material.OAK_LOG);
                        t.getFractalLeaves().setMaterial(Material.OAK_LEAVES);
                        t.getFractalLeaves().setMangrovePropagules(false);
                    });
                }
            }
        }
    }

    @Override
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.MUDFLATS;
    }

    @Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {

        double height = HeightMap.CORE.getHeight(tw, x, z) - 10;

        // If the height is too low, force it back to 3.
        if (height <= 0) {
            height = 3;
        }

        return height;
    }

}
