package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
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

public class BirchMountainsHandler extends AbstractMountainHandler {

    // Birch Mountains must be shorter to allow trees to populate.
    @Override
    protected double getPeakMultiplier(@NotNull BiomeSection section, @NotNull Random sectionRandom) {
        return GenUtils.randDouble(sectionRandom, 1.1, 1.3);
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.JAGGED_PEAKS;
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
    public void populateSmallItems(TerraformWorld tw,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        setRock(new SimpleBlock(data, rawX, 0, rawZ).getGround());

        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK) {

            if (GenUtils.chance(random, 1, 10)) {
                PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
                if (random.nextBoolean()) {
                    PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY + 1, rawZ);
                }
                else {
                    BlockUtils.pickFlower().build(data, rawX, surfaceY + 1, rawZ);
                }
            }
        }
    }

    /**
     * Replace steep areas with various rocks.
     */
    private void setRock(@NotNull SimpleBlock target) {
        if (HeightMap.getTrueHeightGradient(target.getPopData(), target.getX(), target.getZ(), 3)
            > TConfig.c.MISC_TREES_GRADIENT_LIMIT)
        {
            Material rock = Material.ANDESITE;
            if (HeightMap.getTrueHeightGradient(target.getPopData(), target.getX(), target.getZ(), 3)
                > TConfig.c.MISC_TREES_GRADIENT_LIMIT * 2)
            {
                rock = Material.DIORITE;
            }
            while (BlockUtils.isExposedToNonSolid(target)) {
                target.setType(rock);
                target = target.getDown();
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 7);

        for (SimpleLocation sLoc : trees) {
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
                sLoc.setY(treeY);
                // Rarely spawn huge taiga trees
                if (TConfig.c.TREES_BIRCH_BIG_ENABLED && GenUtils.chance(random, 1, 20)) {
                    new FractalTreeBuilder(FractalTypes.Tree.BIRCH_BIG).build(
                            tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY(),
                            sLoc.getZ()
                    );

                }
                else { // Normal trees
                    new FractalTreeBuilder(FractalTypes.Tree.BIRCH_SMALL).build(
                            tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY(),
                            sLoc.getZ()
                    );

                }
            }
        }
    }

    @Override
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.ROCKY_BEACH;
    }

    /**
     * Birch Mountains will allow rivers to carve through them.
     */
    @Override
    public double calculateHeight(@NotNull TerraformWorld tw, int x, int z) {
        double coreRawHeight;
        double height = HeightMap.CORE.getHeight(tw, x, z);// HeightMap.MOUNTAINOUS.getHeight(tw, x, z); // Added here

        // Let mountains cut into adjacent sections.
        double maxMountainRadius = ((double) BiomeSection.sectionWidth);
        // Double attrition height
        height += HeightMap.ATTRITION.getHeight(tw, x, z);
        coreRawHeight = height;

        BiomeSection sect = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);
        if (sect.getBiomeBank().getType() != BiomeType.MOUNTAINOUS) {
            sect = BiomeSection.getMostDominantSection(tw, x, z);
        }

        Random sectionRand = sect.getSectionRandom();
        double maxPeak = getPeakMultiplier(sect, sectionRand);

        // Let's just not offset the peak. This seems to give a better result.
        SimpleLocation mountainPeak = sect.getCenter();

        double distFromPeak = (1.42 * maxMountainRadius) - Math.sqrt(Math.pow(x - mountainPeak.getX(), 2) + Math.pow(
                z
                - mountainPeak.getZ(),
                2
        ));

        double heightMultiplier = maxPeak * (distFromPeak / maxMountainRadius);

        if (heightMultiplier < 1) {
            heightMultiplier = 1;
        }

        height = height * heightMultiplier;

        // If the height is too high, just force it to smooth out
        if (height > 200) {
            height = 200 + (height - 200) * 0.5;
        }
        if (height > 230) {
            height = 230 + (height - 230) * 0.3;
        }
        if (height > 240) {
            height = 240 + (height - 240) * 0.1;
        }
        if (height > 250) {
            height = 250 + (height - 250) * 0.05;
        }

        // Let rivers forcefully carve through birch mountains if they're deep enough.
        double riverDepth = HeightMap.getRawRiverDepth(tw, x, z); // HeightMap.RIVER.getHeight(tw, x, z);

        if (coreRawHeight - riverDepth <= TerraformGenerator.seaLevel - 4) {
            double makeup = 0;
            // Ensure depth
            if (coreRawHeight - riverDepth > TerraformGenerator.seaLevel - 10) {
                makeup = (coreRawHeight - riverDepth) - (TerraformGenerator.seaLevel - 10);
            }
            height = coreRawHeight - makeup;// - riverDepth;
        }

        return height;
    }
}
