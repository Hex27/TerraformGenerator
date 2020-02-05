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

public class TaigaHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return false;
	}

	@Override
	public Biome getBiome() {
		return Biome.TAIGA;
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
		return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.GRASS_BLOCK, 35, Material.DIRT, 3, Material.PODZOL, 10),
				Material.DIRT,
				Material.DIRT,
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE)};
	}

	@Override
	public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

		//Rarely spawn huge taiga trees
		if(GenUtils.chance(random,1,10)){
			int treeX = GenUtils.randInt(random, 2,12) + data.getChunkX()*16;
			int treeZ = GenUtils.randInt(random, 2,12) + data.getChunkZ()*16;
			if(data.getBiome(treeX, treeZ) == getBiome()){
				int treeY = GenUtils.getHighestGround(data, treeX, treeZ);
			
				new FractalTreeBuilder(FractalTreeType.TAIGA_BIG).build(tw, data, treeX, treeY, treeZ);
			}
		}
		
		for(int i = 0; i < GenUtils.randInt(1,5); i++){
			int treeX = GenUtils.randInt(random, 0,15) + data.getChunkX()*16;
			int treeZ = GenUtils.randInt(random, 0,15) + data.getChunkZ()*16;
			if(data.getBiome(treeX, treeZ) == getBiome()){
				int treeY = GenUtils.getHighestGround(data, treeX, treeZ);
			
				new FractalTreeBuilder(FractalTreeType.TAIGA_SMALL).build(tw, data, treeX, treeY, treeZ);
			}
		}
		
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getTrueHighestBlock(data, x, z);
				if(data.getBiome(x,y,z) != getBiome()) continue;
				
				if(BlockUtils.isDirtLike(data.getType(x,y,z))){
					if(GenUtils.chance(random, 1, 10)){
						//TODO: Trees
//						Location loc = new Location(tw.getWorld(), x,y,z);
//						if(GenUtils.chance(random, 1,3))
//							tw.getWorld().generateTree(loc, TreeType.TALL_REDWOOD);
//						else
//							tw.getWorld().generateTree(loc, TreeType.REDWOOD);
						//data.setType(x,y+1,z,Material.PURPLE_WOOL);
					}else if(GenUtils.chance(random, 1, 20)){
						data.setType(x, y+1, z, Material.GRASS);
						if(random.nextBoolean()){
							BlockUtils.setDoublePlant(data, x, y+1, z, Material.TALL_GRASS);
						}else{
							data.setType(x, y+1, z,BlockUtils.pickFlower());
						}
					}
				}
			}
		}
	}
	
}
