package org.terraform.biome.beach;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

public class RockBeachHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return false;
	}

	@Override
	public Biome getBiome() {
		return Biome.SNOWY_BEACH;
	}

	@Override
	public Material[] getSurfaceCrust(Random rand) {
		return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.STONE, 35, Material.GRAVEL, 5, Material.COBBLESTONE, 10),
				GenUtils.weightedRandomMaterial(rand, Material.STONE, 35, Material.GRAVEL, 5, Material.COBBLESTONE, 10),
				GenUtils.randMaterial(rand, Material.STONE,Material.COBBLESTONE,Material.GRAVEL),
				GenUtils.randMaterial(rand, Material.STONE,Material.COBBLESTONE,Material.GRAVEL)};
	}

	@Override
	public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {

		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getHighestGround(data, x, z);
				if(data.getBiome(x,z) != getBiome()) continue;
				
				if(GenUtils.chance(random,7, 100)){
					makeIceSheet(x,y,z, data, random);
					break;
				}
				
				if(GenUtils.chance(random,1,100))
					data.setType(x,y+1,z,Material.COBBLESTONE_SLAB);
			}
		}
	}
	
	private void makeIceSheet(int x, int y, int z, PopulatorDataAbstract data, Random random){
		int length = GenUtils.randInt(6, 16);
		int nx = x;
		int nz = z;
		while(length > 0){
			length--;
			if(data.getType(nx,y,nz).isSolid() &&
					data.getType(nx,y+1,nz) == Material.AIR)
				data.setType(nx,y,nz,Material.ICE);
			
			switch (random.nextInt(5)) {  // The direction chooser
				case 0: nx++; break;
				case 2: nz++; break;
				case 3: nx--; break;
				case 5: nz--; break;
			}
			y = GenUtils.getHighestGround(data, nx, nz);
		}
	
	}
}
