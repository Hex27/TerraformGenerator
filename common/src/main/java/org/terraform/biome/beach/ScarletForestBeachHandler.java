package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class ScarletForestBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.BEACH;
    }

    @Override
    public CustomBiomeType getCustomBiome() {
        return CustomBiomeType.SCARLET_FOREST;
    }
    
    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.WHITE_CONCRETE,
                Material.WHITE_CONCRETE,
                Material.WHITE_CONCRETE,
                GenUtils.randMaterial(rand, Material.WHITE_CONCRETE, Material.STONE),
                GenUtils.randMaterial(rand, Material.WHITE_CONCRETE, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {

        //WHAT THE FUCK DO YOU MEAN REMOVE SUBMERGED GRASS??? WHY WAS IT BEING SET???
//        Material base = data.getType(x, y, z);
//
//        //Remove submerged grass
//        if(base == Material.GRASS_BLOCK && data.getType(x, y+1, z) == Material.WATER)
//            data.setType(x,y,z,Material.DIRT);
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

	}
}
