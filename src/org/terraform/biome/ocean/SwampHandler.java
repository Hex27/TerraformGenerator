package org.terraform.biome.ocean;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.WitchHutPopulator;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTreeType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class SwampHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return true;
	}

	@Override
	public Biome getBiome() {
		return Biome.SWAMP;
	}
	
	private FastNoise mudNoise;
	
	public FastNoise getMudNoise(TerraformWorld tw){
		if(mudNoise == null){
			mudNoise = new FastNoise((int) (tw.getSeed()*4));
			mudNoise.SetNoiseType(NoiseType.SimplexFractal);
			mudNoise.SetFrequency(0.05f);
			mudNoise.SetFractalOctaves(4);
		}
		return mudNoise;
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
		return new Material[]{GenUtils.randMaterial(rand, Material.GRASS_BLOCK, Material.PODZOL, Material.PODZOL),
				GenUtils.randMaterial(rand, Material.DIRT),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE,Material.GRAVEL,Material.SAND),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE)};
	}


	@Override
	public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		final int seaLevel = TerraformGenerator.seaLevel;
		int treeX = 0, treeY, treeZ = 0;
		if(GenUtils.chance(random,3,10)){
			treeX = GenUtils.randInt(random, 2,12) + data.getChunkX()*16;
			treeZ = GenUtils.randInt(random, 2,12) + data.getChunkZ()*16;
			
			if(data.getBiome(treeX, treeZ) == getBiome()){
				treeY = GenUtils.getHighestGround(data, treeX, treeZ);
				new FractalTreeBuilder(FractalTreeType.SWAMP_BOTTOM)
				.build(tw, data, treeX, treeY-3, treeZ);
				new FractalTreeBuilder(FractalTreeType.SWAMP_TOP)
				.build(tw, data, treeX, treeY-2, treeZ);
			}
		}
		
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getTrueHighestBlock(data, x, z);
				if(data.getBiome(x,y,z) != getBiome()) continue;
				if(!BlockUtils.isStoneLike(data.getType(x, y, z))) continue;
				if(y < seaLevel){
					double noise = getMudNoise(tw).GetNoise(x, z);
					if(noise < 0) noise = 0;
					int att = (int) Math.round(noise*10);
					if(att + y > seaLevel)
						att = seaLevel - y;
					for(int i = 1; i<= att; i++){
						if(i < att)
							data.setType(x,y+i,z,getSurfaceCrust(random)[1]);
						else
							data.setType(x,y+i,z,getSurfaceCrust(random)[0]);
					}
					y+=att;
				}
				
				if(GenUtils.chance(random, 1, 40)){
					int minHeight = 3;
					if(y < seaLevel){
						minHeight = seaLevel-y;
					}
					
					BlockUtils.spawnPillar(random, data, x, y+1, z, Material.OAK_LOG, minHeight, minHeight+3);
				}
				
				if(GenUtils.chance(random, 10, 100) && y < TerraformGenerator.seaLevel-3){ //SEA GRASS/KELP
					data.setType(x, y+1, z,Material.SEAGRASS);
					if(random.nextBoolean())
						BlockUtils.setDoublePlant(data, x, y+1, z, Material.TALL_SEAGRASS);
				}
				
				if(GenUtils.chance(random, 2, 100)){
					BlockUtils.generateClayDeposit(x,y,z,data,random);
				}
			}	
		}
		
//		WitchHutPopulator whp = new WitchHutPopulator();
//		if(whp.canSpawn(random, tw, data.getChunkX(),data.getChunkZ(),new ArrayList<BiomeBank>(){{
//			add(BiomeBank.SWAMP);
//		}})){
//			whp.populate(tw, random, data);
//		}
	}
	

}
