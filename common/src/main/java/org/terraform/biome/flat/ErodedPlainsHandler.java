package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class ErodedPlainsHandler extends BiomeHandler {
    static final BiomeHandler plainsHandler = BiomeBank.PLAINS.getHandler();
    static final boolean slabs = TConfig.c.MISC_USE_SLABS_TO_SMOOTH;
    static BiomeBlender biomeBlender;

    private static @NotNull BiomeBlender getBiomeBlender(TerraformWorld tw) {
        if (biomeBlender == null) {
            biomeBlender = new BiomeBlender(tw, true, true).setRiverThreshold(4).setBlendBeaches(false);
        }
        return biomeBlender;
    }

    @Override
    public boolean isOcean() {
        return plainsHandler.isOcean();
    }

    @Override
    public Biome getBiome() {
        return plainsHandler.getBiome();
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return plainsHandler.getSurfaceCrust(rand);
    }

    @Override
    public void populateSmallItems(TerraformWorld world,
                                   Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   PopulatorDataAbstract data)
    {
        plainsHandler.populateSmallItems(world, random, rawX, surfaceY, rawZ, data);
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public void transformTerrain(@NotNull ChunkCache cache,
                                 TerraformWorld tw,
                                 Random random,
                                 ChunkGenerator.@NotNull ChunkData chunk,
                                 int x,
                                 int z,
                                 int chunkX,
                                 int chunkZ)
    {

        FastNoise noise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_ERODEDPLAINS_CLIFFNOISE, world -> {
            FastNoise n = new FastNoise();
            n.SetNoiseType(FastNoise.NoiseType.CubicFractal);
            n.SetFractalOctaves(3);
            n.SetFrequency(0.02f);
            return n;
        });

        FastNoise details = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_ERODEDPLAINS_DETAILS, world -> {
            FastNoise n = new FastNoise();
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFrequency(0.03f);
            return n;
        });


        double threshold = 0.1;
        int heightFactor = 10;

        int rawX = chunkX * 16 + x;
        int rawZ = chunkZ * 16 + z;

        double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
        int height = (int) preciseHeight;

        double noiseValue = Math.max(0, noise.GetNoise(rawX, rawZ))
                            * getBiomeBlender(tw).getEdgeFactor(BiomeBank.ERODED_PLAINS, rawX, rawZ);
        double detailsValue = details.GetNoise(rawX, rawZ);

        double d = (noiseValue / threshold) - (int) (noiseValue / threshold) - 0.5;
        double platformHeight = (int) (noiseValue / threshold) * heightFactor
                                + (64 * Math.pow(d, 7) * heightFactor)
                                + detailsValue * heightFactor * 0.5;

        short newHeight = (short) (height + (int) Math.round(platformHeight));
        if (newHeight < height) {
            return; // Does not make changes if the platform is lower.
        }

        cache.writeTransformedHeight(x, z, (short) ((int) Math.round(platformHeight) + height));
        for (int y = height + 1; y <= newHeight; y++) {
            Material material = GenUtils.randChoice(Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.COBBLESTONE,
                    Material.COBBLESTONE,
                    Material.MOSSY_COBBLESTONE,
                    Material.ANDESITE
            );
            if (slabs
                && material != Material.GRASS_BLOCK
                && y == newHeight
                && platformHeight - (int) platformHeight >= 0.5)
            {
                material = Material.getMaterial(material.name() + "_SLAB");
            }
            assert material != null;
            chunk.setBlock(x, y, z, material);
        }
        if (detailsValue < 0.2 && GenUtils.chance(3, 4)) {
            chunk.setBlock(x, newHeight, z, Material.GRASS_BLOCK);
        }
    }

    @Override
    public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        plainsHandler.populateLargeItems(tw, random, data);
    }
}
