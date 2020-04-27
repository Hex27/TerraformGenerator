package org.terraform.structure;

import java.util.ArrayList;
import java.util.Random;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.structure.animalfarm.AnimalFarmPopulator;
import org.terraform.structure.farmhouse.FarmhousePopulator;

public class VillageHousePopulator extends StructurePopulator {

	@Override
	public boolean canSpawn(Random rand,TerraformWorld tw, int chunkX, int chunkZ,ArrayList<BiomeBank> biomes) {

		MegaChunk mc = new MegaChunk(chunkX,chunkZ);
		int[] coords = getCoordsFromMegaChunk(tw,mc);
		return coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ;
	}
	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
		ArrayList<BiomeBank> banks = new ArrayList<>();
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int height = new HeightMap().getHeight(tw, x, z);//GenUtils.getTrueHighestBlock(data, x, z);
				for(BiomeBank bank:BiomeBank.values()){
					BiomeBank currentBiome = tw.getBiomeBank(x, height, z);//BiomeBank.calculateBiome(tw,tw.getTemperature(x, z), height);
					
					if(bank == currentBiome){
						if(!banks.contains(bank))
							banks.add(bank);
						break;
					}
				}
			}
		}
		
		if(banks.contains(BiomeBank.LUKEWARM_OCEAN)
				||banks.contains(BiomeBank.WARM_OCEAN)
				||banks.contains(BiomeBank.OCEAN)
				||banks.contains(BiomeBank.COLD_OCEAN)
				||banks.contains(BiomeBank.FROZEN_OCEAN)
				||banks.contains(BiomeBank.SWAMP)){
			//Ships
		}else if(banks.contains(BiomeBank.DESERT)
				|| banks.contains(BiomeBank.DESERT_MOUNTAINS)
				|| banks.contains(BiomeBank.BADLANDS)
				|| banks.contains(BiomeBank.BADLANDS_MOUNTAINS)
				|| banks.contains(BiomeBank.SNOWY_WASTELAND)
				|| banks.contains(BiomeBank.ICE_SPIKES)){
			if(!TConfigOption.STRUCTURES_ANIMALFARM_ENABLED.getBoolean())
				return;
			
			new AnimalFarmPopulator().populate(tw,random,data);
		}else if(banks.contains(BiomeBank.FOREST)
				|| banks.contains(BiomeBank.PLAINS)
				|| banks.contains(BiomeBank.TAIGA)
				|| banks.contains(BiomeBank.SAVANNA)
				|| banks.contains(BiomeBank.SNOWY_TAIGA)){

			if(!TConfigOption.STRUCTURES_FARMHOUSE_ENABLED.getBoolean())
				return;
			
			new FarmhousePopulator().populate(tw, random, data);
		}
	}

	protected int[] getCoordsFromMegaChunk(TerraformWorld tw,MegaChunk mc){
		return mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),2392224));
	}

	@Override
	public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
		MegaChunk mc = new MegaChunk(rawX,0,rawZ);
		
		double minDistanceSquared = Integer.MAX_VALUE;
		int[] min = null;
		for(int nx = -1; nx <= 1; nx++){
			for(int nz = -1; nz <= 1; nz++){
				int[] loc = getCoordsFromMegaChunk(tw,mc.getRelative(nx, nz));
				double distSqr = Math.pow(loc[0]-rawX,2) + Math.pow(loc[1]-rawZ,2);
				if(distSqr < minDistanceSquared){
					minDistanceSquared = distSqr;
					min = loc;
				}
			}
		}
		return min;
	}

	

}
