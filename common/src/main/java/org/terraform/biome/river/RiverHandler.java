package org.terraform.biome.river;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class RiverHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.RIVER;
    }

//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 2);
//		gen.setScale(0.005);
//		
//		return (int) (gen.noise(x, z, 0.5, 0.5)*7D+50D);
//	}

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[]{
        		Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }


    @Override
    public void populateSmallItems(@NotNull TerraformWorld world, @NotNull Random random, int rawX, int surfaceY, int rawZ, @NotNull PopulatorDataAbstract data) {
        if(surfaceY >= TerraformGenerator.seaLevel) //Don't apply to dry land
            return;

        //Set ground near sea level to sand
        if(surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.SAND);
        }else if(surfaceY >= TerraformGenerator.seaLevel - 4) {
            if(random.nextBoolean())
                data.setType(rawX, surfaceY, rawZ, Material.SAND);
        }

        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) return;

        // SEA GRASS/KELP
        riverVegetation(world, random, data, rawX, surfaceY, rawZ);

        // Generate clay
        if (GenUtils.chance(random, TConfigOption.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND.getInt(), 1000)) {
            BlockUtils.generateClayDeposit(rawX, surfaceY, rawZ, data, random);
        }
    }

    public static void riverVegetation(@NotNull TerraformWorld tw, @NotNull Random random, @NotNull PopulatorDataAbstract data, int rawX, int surfaceY, int rawZ){
        boolean growsKelp = tw.getHashedRand(rawX>>4, rawZ>>4, 97418).nextBoolean();
        if (GenUtils.chance(random, 10, 100)) {
            generateSeagrass(rawX, surfaceY + 1, rawZ, data);
            if (random.nextBoolean())
                generateTallSeagrass(rawX, surfaceY + 1, rawZ, data);
        } else if (GenUtils.chance(random, 3, 50) && growsKelp && surfaceY + 1 < TerraformGenerator.seaLevel - 10) {
            generateKelp(rawX, surfaceY + 1, rawZ, data, random);
        }
    }
    public static void generateSeagrass(int x, int y, int z, @NotNull PopulatorDataAbstract data) {
        if(data.getType(x,y,z) != Material.WATER) return;
        data.setType(x,y,z,Material.SEAGRASS);

    }
    public static void generateTallSeagrass(int x, int y, int z, @NotNull PopulatorDataAbstract data) {
        if(data.getType(x,y,z) != Material.WATER || data.getType(x,y,z) != Material.WATER) return;
        BlockUtils.setDoublePlant(data, x,y,z, Material.TALL_SEAGRASS);
    }
    private static void generateKelp(int x, int y, int z, @NotNull PopulatorDataAbstract data, Random random) {
        for (int ny = y; ny < TerraformGenerator.seaLevel - GenUtils.randInt(5, 15); ny++) {
            if(data.getType(x,ny,z) != Material.WATER) break;
            data.setType(x, ny, z, Material.KELP_PLANT);
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}


}
