package org.terraform.structure.dungeon;

import java.util.ArrayList;
import java.util.Random;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.structure.StructurePopulator;

public class SmallDungeonPopulator extends StructurePopulator{

	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
		ArrayList<BiomeBank> banks = new ArrayList<>();
		int numOceanic = 0;
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int height = new HeightMap().getHeight(tw, x, z);//GenUtils.getTrueHighestBlock(data, x, z);
				for(BiomeBank bank:BiomeBank.values()){
					BiomeBank currentBiome = tw.getBiomeBank(x, height, z);//BiomeBank.calculateBiome(tw,tw.getTemperature(x, z), height);
					
					if(currentBiome.toString().contains("OCEAN")) numOceanic++;
					
					if(bank == currentBiome){
						if(!banks.contains(bank))
							banks.add(bank);
						break;
					}
				}
			}
		}
		
		if(numOceanic/banks.size() == 1){
			//Only spawn these in full oceans
			if(!TConfigOption.STRUCTURES_DROWNEDDUNGEON_ENABLED.getBoolean())
				return;
			new DrownedDungeonPopulator().populate(tw,random,data);
		}else{
			if(!TConfigOption.STRUCTURES_UNDERGROUNDDUNGEON_ENABLED.getBoolean())
				return;
			new UndergroundDungeonPopulator().populate(tw, random, data);
		}
	}
	
	@Override
	public boolean canSpawn(Random rand,TerraformWorld tw, int chunkX, int chunkZ,ArrayList<BiomeBank> biomes) {

		MegaChunk mc = new MegaChunk(chunkX,chunkZ);
		int[][] allCoords = getCoordsFromMegaChunk(tw,mc);
		for(int[] coords:allCoords){
			if(coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ){
				return true;
			}
		}
		return false;
	}
	
	//Each mega chunk has 10 dungeons
	protected int[][] getCoordsFromMegaChunk(TerraformWorld tw,MegaChunk mc){
		return new int[][]{
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),1317324)),
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),131732)),
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),13176)),
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),131327)),
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),17328))
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
