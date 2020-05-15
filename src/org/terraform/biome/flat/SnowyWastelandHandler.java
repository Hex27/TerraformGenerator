package org.terraform.biome.flat;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Snowable;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class SnowyWastelandHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return false;
	}

	@Override
	public Biome getBiome() {
		return Biome.SNOWY_TUNDRA;
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
		return new Material[]{Material.SNOW_BLOCK,
				Material.SNOW_BLOCK,
				GenUtils.randMaterial(rand, Material.SNOW_BLOCK, Material.SNOW_BLOCK, Material.DIRT,Material.DIRT),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE)};
	}

	@Override
	public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
		
		for(int i = 0; i < GenUtils.randInt(0, 2); i++){
			int[] loc = GenUtils.randomSurfaceCoordinates(random, data);
			//Block b = loc.getBlock().getRelative(0,1,0);
			if(data.getBiome(loc[0],loc[2]) != getBiome()) continue;
			if(data.getType(loc[0],loc[1],loc[2]) != Material.DIRT) continue;
			
			BlockUtils.spawnPillar(random,data, loc[0],loc[1],loc[2],Material.SPRUCE_LOG,3,6);
			//Ugly af.
//			if(GenUtils.chance(1,3))
//				new FractalTreeBuilder(FractalTreeType.WASTELAND_COLLAPSED)
//				.setSnowy(true).build(world,data,loc[0],loc[1]+1,loc[2]);
//				//BlockUtils.spawnPillar(random,data, loc[0],loc[1],loc[2],Material.SPRUCE_LOG,3,6);
//			
//			if(GenUtils.chance(1,30))
//				new FractalTreeBuilder(FractalTreeType.WASTELAND_BIG)
//				.setSnowy(true).build(world,data,loc[0],loc[1]+1,loc[2]);
		}

		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getTrueHighestBlock(data, x, z);
				if(data.getBiome(x,y,z) != getBiome()) continue;
				
				if(data.getType(x,y,z) == Material.DIRT){
					if(GenUtils.chance(random, 1, 20)){
						data.setType(x, y+1, z,Material.DEAD_BUSH);
					}
				}
				if(data.getType(x,y+1,z) == Material.AIR){
					data.setType(x,y+1,z,Material.SNOW);
					if(data.getBlockData(x,y,z) instanceof Snowable){
						Snowable snowable = (Snowable) data.getBlockData(x,y,z);
						snowable.setSnowy(true);
						data.setBlockData(x,y,z,snowable);
					}
				}
			}
		}
	}
	
}
