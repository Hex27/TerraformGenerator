package org.terraform.biome.mountainous;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class RockyMountainsHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return false;
	}

	@Override
	public Biome getBiome() {
		return Biome.MOUNTAINS;
	}
//
//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 8);
//		gen.setScale(0.005);
//		
//		return (int) ((gen.noise(x, z, 0.5, 0.5)*7D+50D)*1.5);
//	}

	@Override
	public Material[] getSurfaceCrust(Random rand) {
		return new Material[]{GenUtils.randMaterial(rand, Material.STONE,Material.STONE,Material.STONE,Material.STONE,Material.COBBLESTONE),
				GenUtils.randMaterial(rand, Material.COBBLESTONE,Material.STONE,Material.STONE),
				GenUtils.randMaterial(rand, Material.COBBLESTONE,Material.STONE,Material.STONE),
				GenUtils.randMaterial(rand, Material.COBBLESTONE,Material.STONE,Material.STONE),
				GenUtils.randMaterial(rand, Material.COBBLESTONE,Material.STONE,Material.STONE),};
	}

	@Override
	public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
//		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
//			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
//				int y = GenUtils.getTrueHighestBlock(data, x, z);
//				if(data.getBiome(x,y,z) != getBiome()) continue;
//				if(GenUtils.chance(random, 5, 100)){
//					//data.setType(x, y+1, z,GenUtils.randMaterial(random,Material.SNOW,Material.COBBLESTONE_SLAB));
//				}else if(GenUtils.chance(random, 5, 100)){
//					//TODO: Trees
////					world.generateTree(chunk.getBlock(x, y, z).getLocation(),
////							TreeType.REDWOOD);
//				}
//			}
//		}
	}

}
