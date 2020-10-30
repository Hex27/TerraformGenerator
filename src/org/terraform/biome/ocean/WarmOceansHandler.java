package org.terraform.biome.ocean;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class WarmOceansHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return Biome.WARM_OCEAN;
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
        return new Material[]{GenUtils.randMaterial(rand, Material.DIRT, Material.SAND, Material.SAND, Material.SAND, Material.GRAVEL, Material.SAND),
                GenUtils.randMaterial(rand, Material.DIRT, Material.SAND, Material.SAND, Material.SAND, Material.GRAVEL, Material.SAND),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.GRAVEL, Material.SAND),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        //boolean growCorals = random.nextBoolean();

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, y + 1, z) != getBiome()) continue;
                if (!BlockUtils.isStoneLike(data.getType(x, y, z))) continue;
                if (GenUtils.chance(random, 10, 100)) { //SEA GRASS/KELP
                    data.setType(x, y + 1, z, Material.SEAGRASS);
                    if (random.nextBoolean() && y < TerraformGenerator.seaLevel - 3)
                        BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_SEAGRASS);
                }
                //else if(GenUtils.chance(random, 5, 100) && growCorals){
                //	CoralGenerator.generateCoral(data,x,y+1,z);
                //}
            }
        }
    }

}
