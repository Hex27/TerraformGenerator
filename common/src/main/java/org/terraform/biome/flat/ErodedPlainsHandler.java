package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class ErodedPlainsHandler extends BiomeHandler {
    static BiomeBlender biomeBlender;
    static BiomeHandler plainsHandler = BiomeBank.PLAINS.getHandler();
    static boolean slabs = TConfigOption.MISC_USE_SLABS_TO_SMOOTH.getBoolean();

    @Override
    public boolean isOcean() {
        return plainsHandler.isOcean();
    }

    @Override
    public Biome getBiome() {
        return plainsHandler.getBiome();
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) { return plainsHandler.getSurfaceCrust(rand); }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        plainsHandler.populateSmallItems(world, random, data);
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public void transformTerrain(TerraformWorld tw, Random random, ChunkGenerator.ChunkData chunk, int chunkX, int chunkZ) {
        FastNoise noise = new FastNoise();
        noise.SetNoiseType(FastNoise.NoiseType.CubicFractal);
        noise.SetFractalOctaves(3);
        noise.SetFrequency(0.02f);

        FastNoise details = new FastNoise();
        details.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        details.SetFrequency(0.03f);

        double threshold = 0.1;
        int heightFactor = 10;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
                int height = (int) preciseHeight;
                double noiseValue = Math.max(0, noise.GetNoise(rawX, rawZ)) * getBiomeBlender(tw).getEdgeFactor(BiomeBank.ERODED_PLAINS, rawX, rawZ);
                double detailsValue = details.GetNoise(rawX, rawZ);

                double d = (noiseValue / threshold) - (int) (noiseValue / threshold) - 0.5;
                double platformHeight = (int) (noiseValue / threshold) * heightFactor
                        + (64 * Math.pow(d, 7) * heightFactor)
                        + detailsValue * heightFactor * 0.5;

                for (int y = 1; y <= (int) Math.round(platformHeight); y++) {
                    Material material = GenUtils.randMaterial(Material.STONE, Material.STONE, Material.STONE, Material.STONE,
                            Material.COBBLESTONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, Material.ANDESITE);
                    if (slabs && material != Material.GRASS_BLOCK && y == (int) Math.round(platformHeight) &&
                            platformHeight - (int) platformHeight >= 0.5) material = Material.getMaterial(material.name() + "_SLAB");
                    chunk.setBlock(x, height + y, z, material);
                }
                if (detailsValue < 0.2 && GenUtils.chance(3, 4)) chunk.setBlock(x, height + (int) Math.round(platformHeight), z, Material.GRASS_BLOCK);
            }
        }
    }

    private static BiomeBlender getBiomeBlender(TerraformWorld tw) {
        if (biomeBlender == null) biomeBlender = new BiomeBlender(tw, true, true, true)
                .setBiomeThreshold(0.3).setRiverThreshold(4).setMountainThreshold(4).setBlendBeaches(false);
        return biomeBlender;
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		plainsHandler.populateLargeItems(tw, random, data);
	}
}
