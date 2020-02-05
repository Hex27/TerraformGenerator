package org.terraform.biome.beach;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.TreeDB;
import org.terraform.utils.GenUtils;

public class SandyBeachHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return false;
	}

	@Override
	public Biome getBiome() {
		return Biome.BEACH;
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
		return new Material[]{GenUtils.randMaterial(rand, Material.SAND,Material.SAND,Material.SAND,Material.SAND,Material.SAND,Material.SAND,Material.GRAVEL),
				GenUtils.randMaterial(rand, Material.SAND,Material.SAND,Material.SAND,Material.SAND,Material.SAND,Material.SAND,Material.GRAVEL),
				GenUtils.randMaterial(rand, Material.SANDSTONE,Material.SAND,Material.SAND,Material.SAND,Material.SAND,Material.SAND,Material.GRAVEL),
				GenUtils.randMaterial(rand, Material.SANDSTONE,Material.STONE),
				GenUtils.randMaterial(rand, Material.SANDSTONE,Material.STONE)};
	}

	@Override
	public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {

		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getTrueHighestBlock(data, x, z);
				if(data.getBiome(x,z) != getBiome()) continue;
				Material base = data.getType(x,y,z);
				y++;
				if(base != Material.SAND
						&& base != Material.GRASS_BLOCK) continue;
				if(GenUtils.chance(random,1, 200)){
					TreeDB.spawnCoconutTree(random,data,x, y, z);
					break;
				}
			}
		}
	}
	

	

}
