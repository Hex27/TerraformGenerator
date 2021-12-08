package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class BadlandsBeachHandler extends BiomeHandler {
    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.BEACH;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
                Material.RED_SAND,
                Material.RED_SAND,
                GenUtils.randMaterial(rand, Material.RED_SAND, Material.RED_SANDSTONE),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {

    }

    @Override
    public BiomeHandler getTransformHandler() {
        return BiomeBank.BADLANDS.getHandler();
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}
}
