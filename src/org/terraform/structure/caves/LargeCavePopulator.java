package org.terraform.structure.caves;

import java.util.ArrayList;
import java.util.Random;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.structure.StructurePopulator;
import org.terraform.utils.GenUtils;

public class LargeCavePopulator extends StructurePopulator{

	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
		if(!TConfigOption.STRUCTURES_LARGECAVE_ENABLED.getBoolean())
			return;
		MegaChunk mc = new MegaChunk(data.getChunkX(),data.getChunkZ());

		int[] spawnCoords = new int[]{data.getChunkX()*16,data.getChunkZ()*16};
		int[][] allCoords = getCoordsFromMegaChunk(tw,mc);
		for(int[] coords:allCoords){
			if(coords[0] >> 4 == data.getChunkX() && coords[1] >> 4 == data.getChunkZ()){
				spawnCoords = coords;
				break;
			}
		}
		
		int x = spawnCoords[0];//data.getChunkX()*16 + random.nextInt(16);
		int z = spawnCoords[1];//data.getChunkZ()*16 + random.nextInt(16);
		Random rand = tw.getHashedRand(x, z, 999323);
		
		int highest = GenUtils.getHighestGround(data,x,z);
		int rY = (highest - 20)/2; //5 block padding bottom, 15 padding top.
		
		if(rand.nextBoolean())
			new GenericLargeCavePopulator().createLargeCave(tw,rand,data,rY,x,rY+6,z);
		else
			new MushroomCavePopulator().createLargeCave(tw,rand,data,rY,x,rY+6,z);
	}
	
	@Override
	public boolean canSpawn(Random rand,TerraformWorld tw, int chunkX, int chunkZ,ArrayList<BiomeBank> biomes) {
		
		MegaChunk mc = new MegaChunk(chunkX,chunkZ);
		int[][] allCoords = getCoordsFromMegaChunk(tw,mc);
		for(int[] coords:allCoords){
			if(coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ){
				return GenUtils
						.chance(rand,
								TConfigOption.STRUCTURES_LARGECAVE_CHANCE.getInt(),
								100);
			}
		}
		return false;
	}
	
	//Each mega chunk has 1 large cave
	protected int[][] getCoordsFromMegaChunk(TerraformWorld tw,MegaChunk mc){
		return new int[][]{
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),78889279)),
				//mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),7245322)),
		};
	}

	@Override
	public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
		MegaChunk mc = new MegaChunk(rawX,0,rawZ);
		
		double minDistanceSquared = Integer.MAX_VALUE;
		int[] min = null;
		for(int nx = -1; nx <= 1; nx++){
			for(int nz = -1; nz <= 1; nz++){
				for(int[] loc:getCoordsFromMegaChunk(tw,mc.getRelative(nx, nz))){
					double distSqr = Math.pow(loc[0]-rawX,2) + Math.pow(loc[1]-rawZ,2);
					if(distSqr < minDistanceSquared){
						minDistanceSquared = distSqr;
						min = loc;
					}
				}
			}
		}
		return min;
	}
	
}
