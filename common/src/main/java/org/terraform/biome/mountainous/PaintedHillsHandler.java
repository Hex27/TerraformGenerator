package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.HeightMap;
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
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class PaintedHillsHandler extends AbstractMountainHandler {

    // Birch Mountains must be shorter to allow trees to populate.
    @Override
    protected double getPeakMultiplier(@NotNull BiomeSection section, @NotNull Random sectionRandom) {
        return GenUtils.randDouble(sectionRandom, 1.05, 1.1);
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.SAVANNA;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.ORANGE_TERRACOTTA,
                Material.ORANGE_TERRACOTTA,
                Material.ORANGE_TERRACOTTA,
                Material.ORANGE_TERRACOTTA,
                Material.ORANGE_TERRACOTTA,
                Material.ORANGE_TERRACOTTA,
                Material.ORANGE_TERRACOTTA,
                Material.ORANGE_TERRACOTTA,
                Material.ORANGE_TERRACOTTA,
                GenUtils.randChoice(rand, Material.ORANGE_TERRACOTTA, Material.STONE),
                GenUtils.randChoice(rand, Material.ORANGE_TERRACOTTA, Material.STONE)
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
        correctDirt(new SimpleBlock(data, rawX, surfaceY, rawZ));

        FastNoise paintNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_PAINTEDHILLS_NOISE, world -> {
            FastNoise n = new FastNoise((int) (world.getSeed() * 4));
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(3);
            n.SetFrequency(0.03f);
            return n;
        });


        if (HeightMap.getTrueHeightGradient(data, rawX, rawZ, 3)
            < TConfig.c.MISC_TREES_GRADIENT_LIMIT)
        {
            data.setType(rawX, surfaceY, rawZ, Material.GRASS_BLOCK);

            if (random.nextBoolean()) {
                data.setType(rawX, surfaceY - 1, rawZ, Material.DIRT);
            }

            if (GenUtils.chance(random, 1, 30)) {
                PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
                if (random.nextBoolean()) {
                    PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY + 1, rawZ);
                }
                else {
                    PlantBuilder.DEAD_BUSH.build(data, rawX, surfaceY + 1, rawZ);
                }
            }
        }

        int terracottaDepth = 9;
        for (int i = 0; i < terracottaDepth; i++) {
            if (data.getType(rawX, surfaceY - i, rawZ) != Material.ORANGE_TERRACOTTA) {
                continue;
            }

            double noise = paintNoise.GetNoise(rawX, surfaceY - i, rawZ);
            Material mat;
            if (noise > 0.3) {
                mat = Material.RED_TERRACOTTA;
            }
            else if (noise > 0) {
                mat = Material.CYAN_TERRACOTTA;
            }
            else if (noise > -0.3) {
                mat = Material.LIGHT_BLUE_TERRACOTTA;
            }
            else {
                mat = Material.YELLOW_TERRACOTTA;
            }

            data.setType(rawX, surfaceY - i, rawZ, mat);
        }
    }


    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw, Random random, @NotNull PopulatorDataAbstract data) {
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 25);

        for (SimpleLocation sLoc : trees) {
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
                sLoc = sLoc.getAtY(treeY);
                if (data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()) != Material.GRASS_BLOCK) {
                    continue;
                }
                // Normal trees
                new FractalTreeBuilder(FractalTypes.Tree.SAVANNA_SMALL).build(
                        tw,
                        data,
                        sLoc.getX(),
                        sLoc.getY(),
                        sLoc.getZ()
                );
            }
        }
    }

    private void correctDirt(@NotNull SimpleBlock start) {
        for (int depth = 0; depth < 5; depth++) {
            for (BlockFace face : BlockUtils.directBlockFaces) {
                if (start.getRelative(face).getType() == Material.ORANGE_TERRACOTTA) {
                    start.setType(Material.ORANGE_TERRACOTTA);
                    break;
                }
            }
            start = start.getDown();
        }
    }

    /**
     * Savanna Mountains will not allow rivers to carve through them.
     */
    @Override
    public double calculateHeight(@NotNull TerraformWorld tw, int x, int z) {
        double height = HeightMap.CORE.getHeight(tw, x, z);// HeightMap.MOUNTAINOUS.getHeight(tw, x, z); // Added here

        // Let mountains cut into adjacent sections.
        double maxMountainRadius = ((double) BiomeSection.sectionWidth);
        // Double attrition height
        height += HeightMap.ATTRITION.getHeight(tw, x, z);

        BiomeSection sect = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);
        if (sect.getBiomeBank().getType() != BiomeType.MOUNTAINOUS) {
            sect = BiomeSection.getMostDominantSection(tw, x, z);
        }

        Random sectionRand = sect.getSectionRandom();
        double maxPeak = getPeakMultiplier(sect, sectionRand);

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

        // Add randomised peaks
        FastNoise jaggedPeaksNoise = NoiseCacheHandler.getNoise(tw,
                NoiseCacheEntry.BIOME_PAINTEDHILLS_ROCKS_NOISE,
                world -> {
                    FastNoise n = new FastNoise((int) (world.getSeed() * 2));
                    n.SetNoiseType(NoiseType.SimplexFractal);
                    n.SetFractalOctaves(5);
                    n.SetFrequency(0.05f);
                    return n;
                }
        );

        double noise = jaggedPeaksNoise.GetNoise(x, z);
        if (noise > 0.3) {
            height += Math.sqrt(noise) * 40;
        }


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

        return height;
    }
}
