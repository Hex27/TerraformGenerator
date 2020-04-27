package org.terraform.biome.flat;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTreeType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class PlainsHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return false;
	}

	@Override
	public Biome getBiome() {
		return Biome.PLAINS;
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
		return new Material[]{GenUtils.randMaterial(rand, Material.GRASS_BLOCK,Material.GRASS_BLOCK,Material.GRASS_BLOCK,Material.GRASS_BLOCK,Material.GRASS_BLOCK,Material.GRASS_BLOCK,Material.GRASS_BLOCK,Material.GRASS_BLOCK,Material.GRASS_BLOCK,Material.GRASS_BLOCK,Material.GRASS_BLOCK,Material.GRASS_PATH),
				Material.DIRT,
				Material.DIRT,
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE)};
	}

	@Override
	public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
		//Pumpkin Patch
		if(GenUtils.chance(1, 1000)){
			for(int i = 0; i < GenUtils.randInt(5, 10); i++){
				int[] loc = GenUtils.randomSurfaceCoordinates(random, data);
				if(data.getBiome(loc[0],loc[2]) != getBiome()) continue;
				data.setType(loc[0],loc[1]+1,loc[2],Material.PUMPKIN);
			}
		}
		
		//Melon Patch
		if(GenUtils.chance(1, 1000)){
			for(int i = 0; i < GenUtils.randInt(5, 10); i++){
				int[] loc = GenUtils.randomSurfaceCoordinates(random, data);
				if(data.getBiome(loc[0],loc[2]) != getBiome()) continue;
				data.setType(loc[0],loc[1]+1,loc[2],Material.MELON);
			}
		}

		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getTrueHighestBlock(data, x, z);
				if(data.getBiome(x,y,z) != getBiome()) continue;
				if(data.getType(x,y,z) == Material.GRASS_BLOCK){
					
					if(GenUtils.chance(random, 1, 10)){
						data.setType(x, y+1, z,Material.GRASS);
						if(random.nextBoolean()){
							BlockUtils.setDoublePlant(data, x, y+1, z, Material.TALL_GRASS);
						}else{
							data.setType(x,y+1,z,BlockUtils.pickFlower());
						}
					}
					if(GenUtils.chance(random,1,500)){
						new FractalTreeBuilder(FractalTreeType.NORMAL_SMALL).build(world, data, x, y+1, z);
						//TreeDB.spawnSmallTree(random, data, x, y+1, z, Material.OAK_LOG, Material.OAK_LEAVES);
					}
					
				}
				
				
			}
		}
	}

}
