package org.terraform.biome.river;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class FrozenRiverHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return Biome.FROZEN_RIVER;
    }

//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 2);
//		gen.setScale(0.005);
//		
//		return (int) (gen.noise(x, z, 0.5, 0.5)*7D+50D);
//	}

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }


    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        boolean growsKelp = world.getHashedRand(rawX>>4,rawZ>>4,14979813).nextBoolean();

        if(surfaceY >= TerraformGenerator.seaLevel) //Don't apply to dry land
            return;

        //Ice
        if (!data.getType(rawX, TerraformGenerator.seaLevel, rawZ).isSolid())
            data.setType(rawX, TerraformGenerator.seaLevel, rawZ, Material.ICE);

        //Set ground near sea level to gravel
        if(surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.GRAVEL);
        }else if(surfaceY >= TerraformGenerator.seaLevel - 4) {
            if(random.nextBoolean())
                data.setType(rawX, surfaceY, rawZ, Material.GRAVEL);
        }

        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) return;

        RiverHandler.riverVegetation(world, random, data, rawX, surfaceY, rawZ);

        if (GenUtils.chance(random, TConfigOption.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND.getInt(), 1000)) {
            BlockUtils.generateClayDeposit(rawX, surfaceY, rawZ, data, random);
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}


}
