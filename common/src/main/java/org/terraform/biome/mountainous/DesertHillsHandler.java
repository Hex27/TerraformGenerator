package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeSection;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class DesertHillsHandler extends AbstractMountainHandler {
    @Override
    public boolean isOcean() {
        return false;
    }

	//Make these resemble dunes more, not massive mountains.
	@Override
	protected double getPeakMultiplier(BiomeSection section, Random sectionRandom) {
		return GenUtils.randDouble(sectionRandom, 1.1, 1.3);
	}

    @Override
    public Biome getBiome() {
        return Biome.DESERT;
    }
//
//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 8);
//		gen.setScale(0.005);
//		
//		return (int) ((gen.noise(x, z, 0.5, 0.5)*7D+50D)*1.5);
//	}

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{Material.SAND,
                Material.SAND,
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.SAND),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.SAND),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.SAND),
                Material.SANDSTONE,
                Material.SANDSTONE,
                Material.SANDSTONE,
                Material.SANDSTONE,
                Material.SANDSTONE,
                Material.SANDSTONE,
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.SAND, Material.STONE),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        
        FastNoise duneNoise = NoiseCacheHandler.getNoise(
        		world, 
        		NoiseCacheEntry.BIOME_DESERT_DUNENOISE, 
        		tw -> {
        	    	FastNoise n = new FastNoise((int) tw.getSeed());
        	        n.SetNoiseType(NoiseType.CubicFractal);
        	        n.SetFractalOctaves(3);
        	        n.SetFrequency(0.03f);
        	        return n;
        		});
    	

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int highest = GenUtils.getTrueHighestBlock(data, x, z);

                for (int y = highest; y > HeightMap.CORE.getHeight(world, x, z); y--) {
                    if (data.getBiome(x, z) != getBiome()) continue;
                    if (duneNoise.GetNoise(x, y, z) > 0)
                        if (data.getType(x, y, z).toString().endsWith("SAND")) {
                            if (TConfigOption.BIOME_DESERT_MOUNTAINS_YELLOW_CONCRETE_POWDER.getBoolean())
                                data.setType(x, y, z, Material.YELLOW_CONCRETE_POWDER);
                        } else if (data.getType(x, y, z).toString().endsWith("SANDSTONE")) {
                            if (TConfigOption.BIOME_DESERT_MOUNTAINS_YELLOW_CONCRETE.getBoolean())
                                data.setType(x, y, z, Material.YELLOW_CONCRETE);
                        }
                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}
}
