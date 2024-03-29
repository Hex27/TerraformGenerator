package org.terraform.biome.river;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class ScarletForestRiverHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return Biome.RIVER;
    }
    @Override
    public CustomBiomeType getCustomBiome() {
        return CustomBiomeType.SCARLET_FOREST;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }


    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        boolean growsKelp = random.nextBoolean();

        if(surfaceY >= TerraformGenerator.seaLevel) //Don't apply to dry land
            return;

        //Set ground near sea level to concrete
        if(surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.WHITE_CONCRETE);
        }else if(surfaceY >= TerraformGenerator.seaLevel - 4) {
            if(random.nextBoolean())
                data.setType(rawX, surfaceY, rawZ, Material.WHITE_CONCRETE);
        }

        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) return;

        // SEA GRASS/KELP
        RiverHandler.riverVegetation(world, random, data, rawX, surfaceY, rawZ);

        // Generate clay
        if (GenUtils.chance(random, TConfigOption.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND.getInt(), 1000)) {
            BlockUtils.generateClayDeposit(rawX, surfaceY, rawZ, data, random);
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

	}


}
