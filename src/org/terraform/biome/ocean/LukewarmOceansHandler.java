package org.terraform.biome.ocean;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

public class LukewarmOceansHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return true;
	}

	@Override
	public Biome getBiome() {
		return Biome.LUKEWARM_OCEAN;
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
		return new Material[]{GenUtils.randMaterial(rand, Material.DIRT,Material.SAND,Material.SAND,Material.SAND,Material.GRAVEL,Material.SAND),
				GenUtils.randMaterial(rand, Material.DIRT,Material.SAND,Material.SAND,Material.SAND,Material.GRAVEL,Material.SAND),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE,Material.GRAVEL,Material.SAND),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE)};
	}

	@Override
	public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
		//boolean growCorals = random.nextBoolean();

		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getTrueHighestBlock(data, x, z);
				if(data.getBiome(x,y,z) != getBiome()) continue;
				if(!BlockUtils.isStoneLike(data.getType(x, y, z))) continue;
				if(GenUtils.chance(random, 10, 100)){ //SEA GRASS/KELP
					data.setType(x, y+1, z,Material.SEAGRASS);
					if(random.nextBoolean() && y < TerraformGenerator.seaLevel-3)
						BlockUtils.setDoublePlant(data, x, y+1, z, Material.TALL_SEAGRASS);
				}else if(GenUtils.chance(random, 17, 100)){
					CoralGenerator.generateCoral(data,x,y+1,z);
				}
				if(GenUtils.chance(random, 2, 100)){
					BlockUtils.generateClayDeposit(x,y,z,data,random);
				}
			}
		}
	}
	
	private void generateClayDeposit(int x, int y, int z, Chunk chunk, Random random){
		 //CLAY DEPOSIT
		int length = GenUtils.randInt(4, 8);
		int nx = x;
		int ny = y;
		int nz = z;
		while(length > 0){
			length--;
			if(chunk.getBlock(nx,ny,nz).getType() == Material.SAND||
					chunk.getBlock(nx,ny,nz).getType() == Material.GRAVEL)
				chunk.getBlock(nx,ny,nz).setType(Material.CLAY,false);
			
			switch (random.nextInt(5)) {  // The direction chooser
				case 0: nx++; break;
				case 1: ny++; break;
				case 2: nz++; break;
				case 3: nx--; break;
				case 4: ny--; break;
				case 5: nz--; break;
			}
			
			if(nx > 15) nx = 15;
			if(nx < 0) nx = 0;
			if(nz > 15) nz = 15;
			if(nz < 0) nz = 0;
			if(ny > y) ny = y;
			if(ny < 2) ny = 2;
		}
	
	}


}
