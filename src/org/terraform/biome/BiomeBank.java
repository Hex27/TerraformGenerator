package org.terraform.biome;

import java.util.Random;

import org.terraform.biome.beach.MudflatsHandler;
import org.terraform.biome.beach.RockBeachHandler;
import org.terraform.biome.beach.SandyBeachHandler;
import org.terraform.biome.flat.BadlandsHandler;
import org.terraform.biome.flat.DesertHandler;
import org.terraform.biome.flat.ForestHandler;
import org.terraform.biome.flat.IceSpikesHandler;
import org.terraform.biome.flat.JungleHandler;
import org.terraform.biome.flat.PlainsHandler;
import org.terraform.biome.flat.SavannaHandler;
import org.terraform.biome.flat.SnowyTaigaHandler;
import org.terraform.biome.flat.SnowyWastelandHandler;
import org.terraform.biome.flat.TaigaHandler;
import org.terraform.biome.mountainous.BadlandsMountainHandler;
import org.terraform.biome.mountainous.BirchMountainsHandler;
import org.terraform.biome.mountainous.DesertMountainHandler;
import org.terraform.biome.mountainous.RockyMountainsHandler;
import org.terraform.biome.mountainous.SnowyMountainsHandler;
import org.terraform.biome.ocean.ColdOceansHandler;
import org.terraform.biome.ocean.FrozenOceansHandler;
import org.terraform.biome.ocean.LukewarmOceansHandler;
import org.terraform.biome.ocean.OceansHandler;
import org.terraform.biome.ocean.RiverHandler;
import org.terraform.biome.ocean.SwampHandler;
import org.terraform.biome.ocean.WarmOceansHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;

public enum BiomeBank {
	
	//MOUNTAINOUS
	ROCKY_MOUNTAINS(new RockyMountainsHandler(),BiomeType.MOUNTAINOUS),
	BADLANDS_MOUNTAINS(new BadlandsMountainHandler(),BiomeType.MOUNTAINOUS),
	SNOWY_MOUNTAINS(new SnowyMountainsHandler(),BiomeType.MOUNTAINOUS),
	BIRCH_MOUNTAINS(new BirchMountainsHandler(),BiomeType.MOUNTAINOUS),
	DESERT_MOUNTAINS(new DesertMountainHandler(),BiomeType.MOUNTAINOUS),
	
	//OCEANIC
	OCEAN(new OceansHandler(),BiomeType.OCEANIC),
	COLD_OCEAN(new ColdOceansHandler(),BiomeType.OCEANIC),
	FROZEN_OCEAN(new FrozenOceansHandler(),BiomeType.OCEANIC),
	WARM_OCEAN(new WarmOceansHandler(),BiomeType.OCEANIC),
	LUKEWARM_OCEAN(new LukewarmOceansHandler(),BiomeType.OCEANIC),
	SWAMP(new SwampHandler(),BiomeType.OCEANIC),
	//RIVER(new RiverHandler(),BiomeType.OCEANIC), //Special case, handle later
	
	//DEEP OCEANIC

	DEEP_OCEAN(new OceansHandler(),BiomeType.DEEP_OCEANIC),
	DEEP_COLD_OCEAN(new ColdOceansHandler(),BiomeType.DEEP_OCEANIC),
	DEEP_FROZEN_OCEAN(new FrozenOceansHandler(),BiomeType.DEEP_OCEANIC),
	DEEP_WARM_OCEAN(new WarmOceansHandler(),BiomeType.DEEP_OCEANIC),
	DEEP_LUKEWARM_OCEAN(new LukewarmOceansHandler(),BiomeType.DEEP_OCEANIC),
	
	//FLAT
	PLAINS(new PlainsHandler(),BiomeType.FLAT),
	SAVANNA(new SavannaHandler(),BiomeType.FLAT),
	FOREST(new ForestHandler(),BiomeType.FLAT),
	DESERT(new DesertHandler(),BiomeType.FLAT),
	JUNGLE(new JungleHandler(),BiomeType.FLAT),
	BADLANDS(new BadlandsHandler(),BiomeType.FLAT),
	TAIGA(new TaigaHandler(),BiomeType.FLAT),
	SNOWY_TAIGA(new SnowyTaigaHandler(),BiomeType.FLAT),
	SNOWY_WASTELAND(new SnowyWastelandHandler(),BiomeType.FLAT),
	ICE_SPIKES(new IceSpikesHandler(),BiomeType.FLAT),
	
	//BEACHES
	SANDY_BEACH(new SandyBeachHandler(),BiomeType.BEACH),
	ROCKY_BEACH(new RockBeachHandler(),BiomeType.BEACH),
	MUDFLATS(new MudflatsHandler(),BiomeType.BEACH), //Special case, handle later
	;
	private BiomeHandler handler;
	//Refers to max moisture and max temperature.
	private float moisture;
	private float temperature;
	private BiomeType type;
	BiomeBank(BiomeHandler handler, BiomeType type){
		this.handler = handler;
		this.type = type;
	}
	
	public BiomeType getType(){
		return type;
	}
	
	/**
	 * 
	 * @param height Ranges between 0-255
	 * @return a biome type
	 */
	public static BiomeBank calculateBiome(TerraformWorld tw, double temperature, double moisture, int height){
		Random random = tw.getHashedRand((int) (temperature*10000), (int) (moisture*10000), height);
		
		//GENERATE AN OCEAN
		if(height < TerraformGenerator.seaLevel){
			BiomeBank bank = BiomeGrid.calculateBiome(
					BiomeType.OCEANIC,
					temperature + GenUtils.randDouble(random, -0.1, 0.1),
					moisture + GenUtils.randDouble(random, -0.1, 0.1)
			);
			
			if(bank == SWAMP){
				if(height >= TerraformGenerator.seaLevel-GenUtils.randInt(random, 9,11)){
					//Shallow and warm areas are swamps.
					return SWAMP;
				}else
					bank = OCEAN;
			}
			if(height <= TConfigOption.DEEP_SEA_LEVEL.getInt()){
				bank = BiomeBank.valueOf("DEEP_"+bank.toString());
			}
				
			return bank;
		}
		
		//GENERATE HIGH-ALTITUDE AREAS
		if(height >= 80-GenUtils.randInt(random, 0,5)){
			return BiomeGrid.calculateBiome(
							BiomeType.MOUNTAINOUS,
							temperature + GenUtils.randDouble(random, -0.1, 0.1),
							moisture + GenUtils.randDouble(random, -0.1, 0.1)
					);
		}
		
		//GENERATE BEACHES
		if(height <= TerraformGenerator.seaLevel+GenUtils.randInt(random, 0,4)){
			return BiomeGrid.calculateBiome(
					BiomeType.BEACH,
					temperature + GenUtils.randDouble(random, -0.1, 0.1),
					moisture + GenUtils.randDouble(random, -0.1, 0.1)
			);
}
		
		//GENERATE LOW-ALTITUDE AREAS
		return BiomeGrid.calculateBiome(
				BiomeType.FLAT,
				temperature + GenUtils.randDouble(random, -0.1, 0.1),
				moisture + GenUtils.randDouble(random, -0.1, 0.1)
		);
	}

	/**
	 * @return the handler
	 */
	public BiomeHandler getHandler() {
		return handler;
	}
	/**
	 * @return the max allowed moisture
	 */
	public float getMaxMoisture() {
		return moisture;
	}
	/**
	 * @return the max allowed temperature
	 */
	public float getMaxTemperature() {
		return temperature;
	}

}
