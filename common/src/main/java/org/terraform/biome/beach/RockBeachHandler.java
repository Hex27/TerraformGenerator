package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class RockBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.STONY_SHORE;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.STONE, 5, Material.GRAVEL, 35, Material.COBBLESTONE, 10),
                GenUtils.weightedRandomMaterial(rand, Material.STONE, 5, Material.GRAVEL, 35, Material.COBBLESTONE, 10),
                GenUtils.randMaterial(rand, Material.STONE, Material.COBBLESTONE, Material.GRAVEL),
                GenUtils.randMaterial(rand, Material.STONE, Material.COBBLESTONE, Material.GRAVEL)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {

    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}
}
