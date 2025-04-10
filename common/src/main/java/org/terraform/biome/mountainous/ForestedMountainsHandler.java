package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.flat.JungleHandler;
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
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CylinderBuilder;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class ForestedMountainsHandler extends AbstractMountainHandler {

    private static void dirtStack(@NotNull PopulatorDataAbstract data, @NotNull Random rand, int x, int y, int z) {
        data.setType(x, y, z, Material.GRASS_BLOCK);

        if (GenUtils.chance(rand, 1, 10)) {
            PlantBuilder.GRASS.build(data, x, y + 1, z);
        }

        int depth = GenUtils.randInt(rand, 3, 7);
        for (int i = 1; i < depth; i++) {
            if (!BlockUtils.isStoneLike(data.getType(x, y - i, z))) {
                break;
            }
            data.setType(x, y - i, z, Material.DIRT);
            if (BlockUtils.isExposedToNonSolid(new SimpleBlock(data, x, y - i, z))) {
                depth++;
            }
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.JUNGLE;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                GenUtils.randChoice(
                        rand,
                        Material.STONE,
                        Material.STONE,
                        Material.STONE,
                        Material.STONE,
                        Material.COBBLESTONE
                ),
                GenUtils.randChoice(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randChoice(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randChoice(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randChoice(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
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

        // Carve under-mountain river holes
        if (rawX % 3 == 0 && rawZ % 3 == 0) {
            if (HeightMap.CORE.getHeight(tw, rawX, rawZ) - HeightMap.getRawRiverDepth(tw, rawX, rawZ)
                < TerraformGenerator.seaLevel - 4)
            {
                new SphereBuilder(random,
                        new SimpleBlock(data, rawX, TerraformGenerator.seaLevel, rawZ),
                        Material.AIR
                ).setRadius(5).setStaticWaterLevel(TerraformGenerator.seaLevel).setHardReplace(true).build();

                if (GenUtils.chance(random, 1, 30)) {
                    int cylY = TerraformGenerator.seaLevel + (surfaceY - TerraformGenerator.seaLevel) / 2 + 4;
                    new CylinderBuilder(random, new SimpleBlock(data, rawX, cylY, rawZ), Material.AIR).setRadius(5)
                                                                                                      .setRY((surfaceY
                                                                                                              - TerraformGenerator.seaLevel)
                                                                                                             / 2f + 2)
                                                                                                      .setHardReplace(
                                                                                                              true)
                                                                                                      .build();
                }
            }
        }

        // Don't touch submerged blocks for the other decorations
        if (surfaceY < TerraformGenerator.seaLevel) {
            return;
        }

        // Make patches of dirt that extend on the mountain sides
        if (GenUtils.chance(random, 1, 25)) {
            dirtStack(data, random, rawX, surfaceY, rawZ);
            for (int nx = -2; nx <= 2; nx++) {
                for (int nz = -2; nz <= 2; nz++) {
                    if (GenUtils.chance(random, 1, 5)) {
                        continue;
                    }
                    surfaceY = GenUtils.getHighestGround(data, rawX + nx, rawZ + nz);

                    // Another check, make sure relative position isn't underwater.
                    if (surfaceY < TerraformGenerator.seaLevel) {
                        continue;
                    }
                    dirtStack(data, random, rawX + nx, surfaceY, rawZ + nz);
                }
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {

        FastNoise groundWoodNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_JUNGLE_GROUNDWOOD, world -> {
            FastNoise n = new FastNoise((int) (world.getSeed() * 12));
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(3);
            n.SetFrequency(0.07f);
            return n;
        });

        FastNoise groundLeavesNoise = NoiseCacheHandler.getNoise(tw,
                NoiseCacheEntry.BIOME_JUNGLE_GROUNDLEAVES,
                world -> {
                    FastNoise n = new FastNoise((int) (world.getSeed() * 2));
                    n.SetNoiseType(NoiseType.SimplexFractal);
                    n.SetFrequency(0.07f);
                    return n;
                }
        );

        SimpleLocation[] bigTrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 20);

        if (TConfig.c.TREES_JUNGLE_BIG_ENABLED) {
            for (SimpleLocation sLoc : bigTrees) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
                sLoc = sLoc.getAtY(treeY);
                if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()
                    && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ())))
                {
                    new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_BIG).skipGradientCheck()
                                                                        .build(tw,
                                                                                data,
                                                                                sLoc.getX(),
                                                                                sLoc.getY(),
                                                                                sLoc.getZ()
                                                                        );
                }
            }
        }


        // Small jungle trees
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 9);

        for (SimpleLocation sLoc : trees) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);

            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() && BlockUtils.isDirtLike(data.getType(sLoc.getX(),
                    sLoc.getY(),
                    sLoc.getZ())))
            {
                TreeDB.spawnSmallJungleTree(true, tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
            }
        }

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);

                // Fades noise, see below
                int distanceToSeaOrMountain = Math.min(y - TerraformGenerator.seaLevel, 80 - y);

                if (distanceToSeaOrMountain > 0) {
                    float leavesNoiseValue = groundLeavesNoise.GetNoise(x, z);
                    float groundWoodNoiseValue = groundWoodNoise.GetNoise(x, z);

                    // If close to mountain level or sea level (=river), fade noise linearly
                    // so that beaches are clear.
                    if (distanceToSeaOrMountain <= 4) {
                        leavesNoiseValue -= -0.25f * distanceToSeaOrMountain + 1;
                        groundWoodNoiseValue -= -0.25f * distanceToSeaOrMountain + 1;
                    }

                    // Generate some ground leaves
                    if (leavesNoiseValue > -0.12 && Math.random() > 0.85) {
                        JungleHandler.createBush(data, leavesNoiseValue, x, y, z);
                    }
                    // Also generate it very commonly on steep areas.
                    else if (GenUtils.chance(random, 1, 10)
                             && HeightMap.getTrueHeightGradient(data, x, z, 2)
                                > 2) // Some random ones where there is no noise.
                    {
                        JungleHandler.createBush(data, 0, x, y, z);
                    }

                    // Generate random wood, or "roots" on the ground
                    if (groundWoodNoiseValue > 0.3) {
                        data.lsetType(x, y + 1, z, Material.JUNGLE_WOOD);
                    }
                }

                // Generate mushrooms
                if (data.getBiome(x, z) == getBiome() && BlockUtils.isDirtLike(data.getType(x, y, z))) {
                    if (data.getType(x, y + 1, z) == Material.JUNGLE_WOOD
                        && BlockUtils.isAir(data.getType(x, y + 2, z))
                        && GenUtils.chance(2, 9))
                    {
                        PlantBuilder.build(data, x, y + 2, z, PlantBuilder.RED_MUSHROOM, PlantBuilder.BROWN_MUSHROOM);
                    }
                }
            }
        }
    }

}
