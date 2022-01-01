package org.terraform.biome;

import org.terraform.utils.Range;
import org.terraform.main.config.TConfigOption;

public enum BiomeClimate {

	//Tree-Dense areas
	HUMID_VEGETATION(Range.between(
			TConfigOption.CLIMATE_HUMIDVEGETATION_MINTEMP.getDouble(),
			TConfigOption.CLIMATE_HUMIDVEGETATION_MAXTEMP.getDouble()),
			Range.between(
					TConfigOption.CLIMATE_HUMIDVEGETATION_MINMOIST.getDouble(),
					TConfigOption.CLIMATE_HUMIDVEGETATION_MAXMOIST.getDouble()),2), 
	
	//Savannas
	DRY_VEGETATION(Range.between(
			TConfigOption.CLIMATE_DRYVEGETATION_MINTEMP.getDouble(),
			TConfigOption.CLIMATE_DRYVEGETATION_MAXTEMP.getDouble()),
			Range.between(
					TConfigOption.CLIMATE_DRYVEGETATION_MINMOIST.getDouble(),
					TConfigOption.CLIMATE_DRYVEGETATION_MAXMOIST.getDouble()),1),
	
	//Deserts
	HOT_BARREN(Range.between(
			TConfigOption.CLIMATE_HOTBARREN_MINTEMP.getDouble(),
			TConfigOption.CLIMATE_HOTBARREN_MAXTEMP.getDouble()),
			Range.between(
					TConfigOption.CLIMATE_HOTBARREN_MINMOIST.getDouble(),
					TConfigOption.CLIMATE_HOTBARREN_MAXMOIST.getDouble()),2), 
	
	//Cold biomes - taigas, maybe eroded plains
	COLD(Range.between(
			TConfigOption.CLIMATE_COLD_MINTEMP.getDouble(),
			TConfigOption.CLIMATE_COLD_MAXTEMP.getDouble()),
			Range.between(
					TConfigOption.CLIMATE_COLD_MINMOIST.getDouble(),
					TConfigOption.CLIMATE_COLD_MAXMOIST.getDouble()),1), 
	
	//Any snowy biomes. 
	SNOWY(Range.between(
			TConfigOption.CLIMATE_SNOWY_MINTEMP.getDouble(),
			TConfigOption.CLIMATE_SNOWY_MAXTEMP.getDouble()),
			Range.between(
					TConfigOption.CLIMATE_SNOWY_MINMOIST.getDouble(),
					TConfigOption.CLIMATE_SNOWY_MAXMOIST.getDouble()),2), 	
	
	//Default climate.
	TRANSITION(Range.between(-4.0,4.0),Range.between(-4.0,4.0),0), 
	; 
	
	Range<Double> temperatureRange;
	Range<Double> moistureRange;
	int priority; //Higher priority means override.
	BiomeClimate(Range<Double> temperatureRange, Range<Double> moistureRange, int priority){
		this.temperatureRange = temperatureRange;
		this.moistureRange = moistureRange;
		this.priority = priority;
	}

	public Range<Double> getTemperatureRange() {
		return temperatureRange;
	}

	public Range<Double> getMoistureRange() {
		return moistureRange;
	}
	
	private static boolean isInRange(double val, Range<Double> r) {
		return r.getMaximum() >= val && r.getMinimum() <= val;
	}
	
	public static BiomeClimate selectClimate(double temp, double moist) {
		
		BiomeClimate candidate = BiomeClimate.TRANSITION;
		
		for(BiomeClimate climate:BiomeClimate.values())
			if(isInRange(temp,climate.getTemperatureRange())
					&& isInRange(moist,climate.getMoistureRange())) {
				
				//If there are multiple climate ranges that apply to this, then
				//the climate with the highest priority will win.
				if(candidate == null)
					candidate = climate;
				else if(candidate.priority < climate.priority)
					candidate = climate;
			}
		
		return candidate;
	}
	
}
