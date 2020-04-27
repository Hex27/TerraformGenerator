package org.terraform.biome.flat;

import java.util.Objects;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTreeType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class JungleHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return false;
	}

	@Override
	public Biome getBiome() {
		return Biome.JUNGLE;
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
		return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.GRASS_BLOCK, 35, Material.PODZOL, 5, Material.COARSE_DIRT, 1),
				Material.DIRT,
				Material.DIRT,
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE)};
	}

	@Override
	public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		
		FastNoise groundWoodNoise = new FastNoise((int) (tw.getSeed()*12));
		groundWoodNoise.SetNoiseType(NoiseType.SimplexFractal);
		groundWoodNoise.SetFractalOctaves(3);
		groundWoodNoise.SetFrequency(0.07f);
		
		//Most jungle chunks have a big jungle tree
		if(GenUtils.chance(random,6,10)){
			int treeX = GenUtils.randInt(random, 2,12) + data.getChunkX()*16;
			int treeZ = GenUtils.randInt(random, 2,12) + data.getChunkZ()*16;
			if(data.getBiome(treeX, treeZ) == getBiome()){
		
				int treeY = GenUtils.getHighestGround(data, treeX, treeZ);
			
				new FractalTreeBuilder(FractalTreeType.JUNGLE_BIG).build(tw, data, treeX, treeY, treeZ);
			}
		}else if(GenUtils.chance(random, 7,10)){
			int treeX = GenUtils.randInt(random, 2,12) + data.getChunkX()*16;
			int treeZ = GenUtils.randInt(random, 2,12) + data.getChunkZ()*16;
			if(data.getBiome(treeX, treeZ) == getBiome()){
				
				int treeY = GenUtils.getHighestGround(data, treeX, treeZ);
			
				new FractalTreeBuilder(FractalTreeType.JUNGLE_SMALL).build(tw, data, treeX, treeY, treeZ);
			}
		}
		
		for(int i = 0; i < GenUtils.randInt(1,5); i++){
			int treeX = GenUtils.randInt(random, 0,15) + data.getChunkX()*16;
			int treeZ = GenUtils.randInt(random, 0,15) + data.getChunkZ()*16;
			if(data.getBiome(treeX, treeZ) == getBiome()){
				int treeY = GenUtils.getHighestGround(data, treeX, treeZ);
			
				new FractalTreeBuilder(FractalTreeType.JUNGLE_SMALL).build(tw, data, treeX, treeY, treeZ);
			}
		}

		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				//Bukkit.broadcastMessage("1");
				int y = GenUtils.getHighestGround(data, x, z);
				if(groundWoodNoise.GetNoise(x,z) > 0.3){
					if(GenUtils.chance(random,99,100) &&
							data.getBiome(x,z) == getBiome() &&
							BlockUtils.isDirtLike(data.getType(x,y,z)))
						data.setType(x,y+1,z,Material.JUNGLE_WOOD);
				}
				//y++;
				//Bukkit.broadcastMessage("2");
				if(data.getType(x,y,z) == Material.GRASS_BLOCK){
					//Bukkit.broadcastMessage("3");
					if(GenUtils.chance(random, 4, 10)){
						if(data.getType(x, y+1, z) != Material.AIR){
							if(data.getType(x,y+1,z) == Material.JUNGLE_WOOD &&
									data.getType(x, y+2, z) == Material.AIR){
								data.setType(x,y+2,z,GenUtils.randMaterial(Material.RED_MUSHROOM,Material.BROWN_MUSHROOM));
							}
							continue;
						}
						//Grass & Flowers
						int rand = random.nextInt(3);
						switch(rand){
						case 0:
							BlockUtils.setDoublePlant(data, x, y+1, z, Material.TALL_GRASS);
							break;
						case 1:
							data.setType(x, y+1, z, BlockUtils.pickFlower());
							break;
						case 2:
							BlockUtils.setDoublePlant(data, x, y+1, z, BlockUtils.pickTallFlower());
							break;
						}
						
					}
					if(GenUtils.chance(random,1,200)){

						if(BlockUtils.isDirtLike(data.getType(x,y,z))){
							SimpleBlock base = new SimpleBlock(data,x,y+1,z);
							int rX = GenUtils.randInt(random,2,3);
							int rY = GenUtils.randInt(random,2,4);
							int rZ = GenUtils.randInt(random,2,3);
							BlockUtils.replaceSphere(Math.abs(Objects.hash(x,y,z)), rX, rY, rZ, base, false, Material.JUNGLE_LEAVES);
						}
					}
				}
			}
		}
	}
}
