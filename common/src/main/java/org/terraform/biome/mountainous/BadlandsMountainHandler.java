package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class BadlandsMountainHandler extends AbstractMountainHandler {
    /**
     * Performs badlands plateau generation for one x/z coord.
     */
    public static void oneUnit(TerraformWorld world, Random random, PopulatorDataAbstract data, int x, int z, boolean force) {
        int highest = GenUtils.getTrueHighestBlock(data, x, z);
        int threshold = TConfigOption.BIOME_MOUNTAIN_HEIGHT.getInt() - 20;
        if (force)
            threshold = highest - GenUtils.randInt(random, 3, 6);
        for (int y = highest; y > threshold; y--) {
            if (data.getBiome(x, z) != Biome.BADLANDS_PLATEAU && !force) continue;
            if (!data.getType(x, y, z).toString().endsWith("SAND"))
                continue;

            data.setType(x, y, z, BlockUtils.getTerracotta(y));
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.BADLANDS_PLATEAU;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.RED_SAND,
        		Material.RED_SAND,
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.RED_SAND),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                oneUnit(world, random, data, x, z, false);
            }
        }
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return BiomeBank.BADLANDS.getHandler();
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.BADLANDS_BEACH;
	}
}
