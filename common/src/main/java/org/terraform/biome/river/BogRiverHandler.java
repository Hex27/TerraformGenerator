package org.terraform.biome.river;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.biome.flat.MuddyBogHandler;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class BogRiverHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
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
                Material.DIRT,
                Material.DIRT,
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE, Material.DIRT),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public void transformTerrain(@NotNull ChunkCache cache,
                                 @NotNull TerraformWorld tw,
                                 Random random,
                                 ChunkGenerator.@NotNull ChunkData chunk,
                                 int x,
                                 int z,
                                 int chunkX,
                                 int chunkZ)
    {
        int rawX = chunkX * 16 + x;
        int rawZ = chunkZ * 16 + z;

        FastNoise sinkin = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_MUDDYBOG_HEIGHTMAP, world -> {
            FastNoise n = new FastNoise((int) tw.getSeed());
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(4);
            n.SetFrequency(0.005f);
            return n;
        });

        double noise = sinkin.GetNoise(rawX, rawZ);
        if (noise > -0.2) {
            noise += 0.5;
            if (noise > 1.05) {
                noise = 1.05;
            }
            if (cache.getTransformedHeight(x, z) < TerraformGenerator.seaLevel) {
                double maxHeight = (TerraformGenerator.seaLevel - cache.getTransformedHeight(x, z)) + 2.0;
                int height = (int) Math.round((maxHeight * noise));

                if (tw.getBiomeBank(rawX, rawZ) != BiomeBank.BOG_RIVER
                    && tw.getBiomeBank(rawX, rawZ) != BiomeBank.MUDDY_BOG
                    && tw.getBiomeBank(rawX, rawZ) != BiomeBank.BOG_BEACH)
                {
                    height = 0;
                }

                for (int newHeight = 1; newHeight <= height; newHeight++) {
                    chunk.setBlock(x, cache.getTransformedHeight(x, z) + newHeight, z, Material.DIRT);
                }

                if (height >= 1) {
                    cache.writeTransformedHeight(x, z, (short) (cache.getTransformedHeight(x, z) + height));
                }
                if (cache.getTransformedHeight(x, z) >= TerraformGenerator.seaLevel) {
                    chunk.setBlock(x, cache.getTransformedHeight(x, z), z, Material.GRASS_BLOCK);
                }
            }
        }
    }

    @Override
    public void populateSmallItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {

        // Dry decorations
        new MuddyBogHandler().populateSmallItems(tw, random, rawX, surfaceY, rawZ, data);

        // Water decorations

        SimpleBlock block = new SimpleBlock(data, rawX, surfaceY, rawZ);
        if (BlockUtils.isWet(block.getUp())) {

            // SEA GRASS/KELP
            RiverHandler.riverVegetation(tw, random, data, rawX, surfaceY, rawZ);

            // Generate clay
            if (GenUtils.chance(random, TConfig.c.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND, 1000)) {
                BlockUtils.generateClayDeposit(rawX, surfaceY, rawZ, data, random);
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
