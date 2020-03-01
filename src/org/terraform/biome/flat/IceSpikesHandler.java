package org.terraform.biome.flat;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Snowable;
import org.bukkit.util.Vector;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class IceSpikesHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return false;
	}

	@Override
	public Biome getBiome() {
		return Biome.ICE_SPIKES;
	}

	@Override
	public Material[] getSurfaceCrust(Random rand) {
		return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.ICE,5, Material.SNOW_BLOCK, 25),
				Material.ICE,
				GenUtils.randMaterial(rand, Material.ICE,Material.DIRT),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE)};
	}

	@Override
	public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {

		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getTrueHighestBlock(data, x, z);
				if(data.getBiome(x,z) != getBiome()) continue;
				
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

		for(int i = 0; i < GenUtils.randInt(random, 1, 3); i++){
			int[] loc = GenUtils.randomSurfaceCoordinates(random, data);
			if(data.getType(loc[0],loc[1],loc[2]) == Material.SNOW_BLOCK){
				if(GenUtils.chance(random,1,10)){ //big spike
					genSpike(world,random,data,loc[0],loc[1],loc[2],
							 GenUtils.randInt(3, 10), //radius
							 GenUtils.randInt(30,50));
				}else //Small spike
					genSpike(world,random,data,loc[0],loc[1],loc[2],
							 GenUtils.randInt(3, 7), //radius
							 GenUtils.randInt(3,10));
			}
		}
	}
	
	public static void genSpike(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, int baseRadius, int height){
		//Vector one to two;
		Vector base = new Vector(x,y,z);
		Vector base2 = new Vector(x+GenUtils.randInt(random, -2*baseRadius, 2*baseRadius),y+height,z+GenUtils.randInt(random, -2*baseRadius, 2*baseRadius));
		Vector v = base2.subtract(base);
		Vector unitV = v.clone().multiply(1/v.length());
		int segments = height;
		SimpleBlock one = new SimpleBlock(data,x,y,z);
		double radius = baseRadius;
		for(int i=0; i<=segments; i++){
			Vector seg = v.clone().multiply((float) ((float)i)/((float)segments));
			SimpleBlock segment = one.getRelative(seg);
//			segment.setHardReplace();
//			segment.setType(type);
			BlockUtils.replaceSphere((int) (tw.getSeed()*12), (float)radius, 2, (float)radius, segment, false, true, Material.PACKED_ICE, Material.ICE);
//			Block segment = one.getLocation().add(seg).getBlock();
//			segment.setType(type);
			radius = ((double)baseRadius)*(1- ((double)i)/((double)segments));
		}
	}
		
}
