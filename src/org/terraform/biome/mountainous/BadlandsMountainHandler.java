package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class BadlandsMountainHandler extends BiomeHandler {
    public static final Material[] terracottas = {
            Material.WHITE_TERRACOTTA,
            Material.TERRACOTTA,
            Material.LIGHT_GRAY_TERRACOTTA,
            Material.YELLOW_TERRACOTTA,
            Material.ORANGE_TERRACOTTA,
            Material.BROWN_TERRACOTTA,
            Material.RED_TERRACOTTA,
    };

    @Override
    public boolean isOcean() {
        return false;
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
    public Biome getBiome() {
        return Biome.BADLANDS_PLATEAU;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.RED_SAND, 35, Material.SAND, 5),
                GenUtils.weightedRandomMaterial(rand, Material.RED_SAND, 35, Material.SAND, 5),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.RED_SANDSTONE, Material.RED_SAND),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE)};
    }

    @Override
    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
            	oneUnit(world,random,data,x,z,false);
            }
        }
    }
    
    /**
     * Performs badlands plateau generation for one x/z coord.
     * @param world
     * @param random
     * @param data
     * @param x
     * @param z
     */
    public static void oneUnit(TerraformWorld world, Random random, PopulatorDataAbstract data, int x, int z, boolean force) {
        int highest = GenUtils.getTrueHighestBlock(data, x, z);
        int threshold = TConfigOption.BIOME_MOUNTAIN_HEIGHT.getInt()-20;
        if(force) 
        	threshold = highest-GenUtils.randInt(random, 3,6);
        for (int y = highest; y > threshold; y--) {
            if (data.getBiome(x, y, z) != Biome.BADLANDS_PLATEAU && !force) continue;
            if (!data.getType(x, y, z).toString().contains("SAND"))
                continue;
            //if (GenUtils.chance(1, 50)) continue;
            int multiplier = 0;
            if(GenUtils.chance(random, 1,50)) multiplier++;
            if(GenUtils.chance(random, 1,100)) multiplier++;
            
            Material terra = terracottas[(multiplier+y) % terracottas.length];
            data.setType(x, y, z, terra);
        }
    }
}
