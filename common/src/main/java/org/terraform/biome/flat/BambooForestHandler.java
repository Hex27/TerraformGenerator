package org.terraform.biome.flat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.type.Bamboo;
import org.bukkit.block.data.type.Bamboo.Leaves;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class BambooForestHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.BAMBOO_JUNGLE;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        FastNoise pathNoise = NoiseCacheHandler.getNoise(
        		world, 
        		NoiseCacheEntry.BIOME_BAMBOOFOREST_PATHNOISE, 
        		tw -> {
        	    	FastNoise n = new FastNoise((int) (tw.getSeed() * 13));
        	        n.SetNoiseType(NoiseType.SimplexFractal);
        	        n.SetFractalOctaves(3);
        	        n.SetFrequency(0.07f);
        	        return n;
        		});
    	

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;

                //Podzol Paths
                if (pathNoise.GetNoise(x, z) > 0.27) {
                    if (GenUtils.chance(random, 99, 100) &&
                            data.getBiome(x, z) == getBiome() &&
                            BlockUtils.isDirtLike(data.getType(x, y, z)))
                        data.setType(x, y, z, Material.PODZOL);
                }

                if (data.getType(x, y, z) == Material.GRASS_BLOCK ||
                        data.getType(x, y, z) == Material.PODZOL) {

                    //Grass and shrubbery
                    if (GenUtils.chance(random, 1, 3)) {
                        if (GenUtils.chance(random, 6, 10)) {
                            data.setType(x, y + 1, z, Material.GRASS);
                            if (random.nextBoolean()) {
                                BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                            }
                        } else {
                            if (GenUtils.chance(random, 7, 10))
                                data.setType(x, y + 1, z, Material.FERN);
                            else
                                BlockUtils.setDoublePlant(data, x, y + 1, z, Material.LARGE_FERN);
                        }
                    }
                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;

                if (data.getType(x, y, z) == Material.GRASS_BLOCK ||
                        data.getType(x, y, z) == Material.PODZOL) {
                	
                    //Small grass poffs
                    if (GenUtils.chance(random, 1, 50)) {
                        BlockUtils.replaceSphere(
                                random.nextInt(424444),
                                2, 3, 2,
                                new SimpleBlock(data, x, y + 1, z), false, Material.JUNGLE_LEAVES);
                    }

                    //Bamboo
                    if (GenUtils.chance(random, 1, 3) && BlockUtils.isDirtLike(data.getType(x, y, z))) {
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
