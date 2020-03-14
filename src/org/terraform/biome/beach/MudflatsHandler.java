package org.terraform.biome.beach;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class MudflatsHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return false;
	}

	@Override
	public Biome getBiome() {
		return Biome.SWAMP;
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
		return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.PODZOL, 35, Material.GRASS_BLOCK, 10),
				GenUtils.randMaterial(rand, Material.DIRT),
				GenUtils.randMaterial(rand, Material.DIRT),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE)};
	}

	@Override
	public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {

		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getHighestGround(data, x, z);
				if(data.getBiome(x,z) != getBiome()) continue;
				y++;
				if(data.getType(x, y, z) != Material.AIR) continue;
				if(GenUtils.chance(5,100)){
					if(random.nextBoolean())
						BlockUtils.setDoublePlant(data, x, y, z, Material.TALL_GRASS);
					else
						data.setType(x,y,z,Material.GRASS);
				}
			}
		}
	}
}
