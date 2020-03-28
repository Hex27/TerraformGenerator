package org.terraform.biome;

import java.util.Random;

import org.terraform.biome.beach.MudflatsHandler;
import org.terraform.biome.beach.RockBeachHandler;
import org.terraform.biome.beach.SandyBeachHandler;
import org.terraform.biome.flat.BadlandsHandler;
import org.terraform.biome.flat.DesertHandler;
import org.terraform.biome.flat.ForestHandler;
import org.terraform.biome.flat.IceSpikesHandler;
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
import org.terraform.biome.ocean.SwampHandler;
import org.terraform.biome.ocean.WarmOceansHandler;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

public enum BiomeBank {
	
	//MOUNTAINOUS
	ROCKY_MOUNTAINS(new RockyMountainsHandler()),
	BADLANDS_MOUNTAINS(new BadlandsMountainHandler()),
	SNOWY_MOUNTAINS(new SnowyMountainsHandler()),
	BIRCH_MOUNTAINS(new BirchMountainsHandler()),
	DESERT_MOUNTAINS(new DesertMountainHandler()),
	
	//OCEANIC
	OCEAN(new OceansHandler()),
	COLD_OCEAN(new ColdOceansHandler()),
	FROZEN_OCEAN(new FrozenOceansHandler()),
	WARM_OCEAN(new WarmOceansHandler()),
	LUKEWARM_OCEAN(new LukewarmOceansHandler()),
	SWAMP(new SwampHandler()),
	
	//FLAT
	PLAINS(new PlainsHandler()),
	SAVANNA(new SavannaHandler()),
	FOREST(new ForestHandler()),
	DESERT(new DesertHandler()),
	BADLANDS(new BadlandsHandler()),
	TAIGA(new TaigaHandler()),
	SNOWY_TAIGA(new SnowyTaigaHandler()),
	SNOWY_WASTELAND(new SnowyWastelandHandler()),
	ICE_SPIKES(new IceSpikesHandler()),
	
	//BEACHES
	SANDY_BEACH(new SandyBeachHandler()),
	ROCKY_BEACH(new RockBeachHandler()),
	MUDFLATS(new MudflatsHandler()),
	;
	private BiomeHandler handler;
	
	BiomeBank(BiomeHandler handler){
		this.handler = handler;
	}
	
	/**
	 * 
	 * @param height Ranges between 0-255
	 * @param moisture Ranges between 0-100
	 * @return a biome type
	 */
	public static BiomeBank calculateBiome(TerraformWorld tw, double temperature, int height){
		Random random = tw.getRand((long) (10000*temperature+height));
		//GENERATE AN OCEAN
		if(height < TerraformGenerator.seaLevel){
			if(height >= TerraformGenerator.seaLevel-GenUtils.randInt(random, 9,11)){
				//Shallow and warm areas are swamps.
				if(temperature > 0.7 + GenUtils.randDouble(random,-0.2,0.2) &&
						temperature < 1.5 + GenUtils.randDouble(random,-0.2,0.2)) 
					return SWAMP;
			}
			if(temperature >= 1.7 + GenUtils.randDouble(random,-0.2,0.2))
				return WARM_OCEAN;
			else if(temperature >= 1 + GenUtils.randDouble(random,-0.2,0.2))
				return LUKEWARM_OCEAN;
			else if(temperature >= 0 + GenUtils.randDouble(random,-0.2,0.2))
				return OCEAN;
			else if(temperature >= -1.5 + GenUtils.randDouble(random,-0.2,0.2))
				return COLD_OCEAN;
			else
				return FROZEN_OCEAN;
		}
		
		//GENERATE HIGH-ALTITUDE AREAS
		if(height >= 80-GenUtils.randInt(random, 0,5)){
//			if(height > 110-GenUtils.randInt(random, 0,10) || temperature <= 0)
//				return SNOWY_MOUNTAINS;
			if(temperature >= 2.5 + GenUtils.randDouble(random,-0.1,0.1))
				return BADLANDS_MOUNTAINS;
			if(temperature >= 2 + GenUtils.randDouble(random,-0.1,0.1))
				return DESERT_MOUNTAINS;
			//VOLCANOES SHOULD SPAWN HERE! PREPARE A VOLCANO BIOME.
			if(temperature >= -1 + GenUtils.randDouble(random,-0.1,0.1))
				return ROCKY_MOUNTAINS;
			if(temperature >= -2 + GenUtils.randDouble(random,-0.1,0.1))
				return BIRCH_MOUNTAINS;
			return SNOWY_MOUNTAINS;
		}
		
		//GENERATE BEACHES
		if(height <= TerraformGenerator.seaLevel+GenUtils.randInt(random, 0,4)){
			if(temperature >= 1.5 + GenUtils.randDouble(random,-0.1,0.1))
				return SANDY_BEACH;
			else if(temperature >= 1 + GenUtils.randDouble(random,-0.1,0.1))
				return MUDFLATS;
			else if(temperature >= -0.89 + GenUtils.randDouble(random,-0.1,0.1))
				return SANDY_BEACH;
			else
				return ROCKY_BEACH;
		}
		
		//GENERATE LOW-ALTITUDE AREAS
		if(temperature >= 2.5 + GenUtils.randDouble(random,-0.1,0.1)) return BADLANDS;
		if(temperature >= 2 + GenUtils.randDouble(random,-0.1,0.1)) return DESERT;
		if(temperature >= 1.3 + GenUtils.randDouble(random,-0.1,0.1)) return SAVANNA;
		if(temperature >= 0.4 + GenUtils.randDouble(random,-0.1,0.1)) return PLAINS;
		if(temperature >= -0.3 + GenUtils.randDouble(random,-0.1,0.1)) return FOREST;
		if(temperature >= -1 + GenUtils.randDouble(random,-0.1,0.1)) return TAIGA;
		//Begin snowing @ -1
		if(temperature >= -2 + GenUtils.randDouble(random,-0.1,0.1)) return SNOWY_TAIGA;
		if(temperature >= -2.5  + GenUtils.randDouble(random,-0.1,0.1)) return ICE_SPIKES;
		return SNOWY_WASTELAND;
	}

	/**
	 * @return the handler
	 */
	public BiomeHandler getHandler() {
		return handler;
	}

}
