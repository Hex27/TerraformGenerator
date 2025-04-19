package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class ScarletForestHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.FOREST;
    }

    @Override
    public @NotNull CustomBiomeType getCustomBiome() {
        return CustomBiomeType.SCARLET_FOREST;
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
        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK) {

            if (GenUtils.chance(random, 1, 10)) { // Grass
                if (GenUtils.chance(random, 6, 10)) {
                    PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    if (random.nextBoolean()) {
                        PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
                else {
                    if (GenUtils.chance(random, 7, 10)) {
                        PlantBuilder.POPPY.build(data, rawX, surfaceY + 1, rawZ);
                    }
                    else {
                        PlantBuilder.ROSE_BUSH.build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw, Random random, @NotNull PopulatorDataAbstract data) {

        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 16);

        for (SimpleLocation sLoc : trees) {

            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);

            if (tw.getBiomeBank(sLoc.getX(), sLoc.getZ()) == BiomeBank.SCARLET_FOREST
                && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ())))
            {
                if (TConfig.c.TREES_SCARLET_BIG_ENABLED) {
                    new FractalTreeBuilder(FractalTypes.Tree.SCARLET_BIG).build(tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY(),
                            sLoc.getZ()
                    );
                }
                else {
                    new FractalTreeBuilder(FractalTypes.Tree.SCARLET_SMALL).build(tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY(),
                            sLoc.getZ()
                    );
                }

                BlockUtils.lambdaCircularPatch(random.nextInt(132798),7f, new SimpleBlock(data, sLoc),
                 (b)->{if(BlockUtils.isDirtLike(b.getType()))b.setType(Material.PODZOL);});
            }
        }

        SimpleLocation[] smalltrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 7);

        for (SimpleLocation sLoc : smalltrees) {

            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);

            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() && BlockUtils.isDirtLike(data.getType(sLoc.getX(),
                    sLoc.getY(),
                    sLoc.getZ())))
            {
                new FractalTreeBuilder(FractalTypes.Tree.SCARLET_SMALL).build(
                        tw,
                        data,
                        sLoc.getX(),
                        sLoc.getY(),
                        sLoc.getZ()
                );
            }
        }
    }

    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.SCARLET_FOREST_BEACH;
    }

    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.SCARLET_FOREST_RIVER;
    }

}
