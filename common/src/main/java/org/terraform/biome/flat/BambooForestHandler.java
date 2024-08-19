package org.terraform.biome.flat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.type.Bamboo;
import org.bukkit.block.data.type.Bamboo.Leaves;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class BambooForestHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.BAMBOO_JUNGLE;
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
        FastNoise pathNoise = NoiseCacheHandler.getNoise(world, NoiseCacheEntry.BIOME_BAMBOOFOREST_PATHNOISE, tw -> {
            FastNoise n = new FastNoise((int) (tw.getSeed() * 13));
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(3);
            n.SetFrequency(0.07f);
            return n;
        });

        // Podzol Paths
        if (pathNoise.GetNoise(rawX, rawZ) > 0.27) {
            if (GenUtils.chance(random, 99, 100) && data.getBiome(rawX, rawZ) == getBiome() && BlockUtils.isDirtLike(
                    data.getType(rawX, surfaceY, rawZ)))
            {
                data.setType(rawX, surfaceY, rawZ, Material.PODZOL);
            }
        }

        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK
            || data.getType(rawX, surfaceY, rawZ) == Material.PODZOL)
        {

            // Grass and shrubbery
            if (GenUtils.chance(random, 1, 3)) {
                if (GenUtils.chance(random, 6, 10)) {
                    PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    if (random.nextBoolean()) {
                        PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
                else {
                    if (GenUtils.chance(random, 7, 10)) {
                        PlantBuilder.FERN.build(data, rawX, surfaceY + 1, rawZ);
                    }
                    else {
                        PlantBuilder.LARGE_FERN.build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
            }
        }
    }

    @Override
    public void populateLargeItems(TerraformWorld tw, @NotNull Random random, @NotNull PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, z) != getBiome()) {
                    continue;
                }

                if (data.getType(x, y, z) == Material.GRASS_BLOCK || data.getType(x, y, z) == Material.PODZOL) {

                    // Small grass poffs
                    if (TConfig.arePlantsEnabled() && GenUtils.chance(random, 1, 50)) {
                        BlockUtils.replaceSphere(random.nextInt(424444),
                                2,
                                3,
                                2,
                                new SimpleBlock(data, x, y + 1, z),
                                false,
                                Material.JUNGLE_LEAVES
                        );
                    }

                    // Bamboo
                    if (TConfig.arePlantsEnabled()
                        && GenUtils.chance(random, 1, 3)
                        && BlockUtils.isDirtLike(data.getType(x, y, z)))
                    {
                        int h = BlockUtils.spawnPillar(random, data, x, y + 1, z, Material.BAMBOO, 12, 16);
                        Bamboo bambooHead = (Bamboo) Bukkit.createBlockData(Material.BAMBOO);
                        bambooHead.setLeaves(Leaves.LARGE);
                        data.setBlockData(x, y + h, z, bambooHead);

                        bambooHead = (Bamboo) Bukkit.createBlockData(Material.BAMBOO);
                        bambooHead.setLeaves(Leaves.LARGE);
                        data.setBlockData(x, y + h - 1, z, bambooHead);

                        bambooHead = (Bamboo) Bukkit.createBlockData(Material.BAMBOO);
                        bambooHead.setLeaves(Leaves.SMALL);
                        data.setBlockData(x, y + h - 2, z, bambooHead);
                    }
                }
            }
        }
    }
}
