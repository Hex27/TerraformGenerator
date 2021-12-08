package org.terraform.biome;

import org.terraform.utils.Range;

public enum BiomeClimate {

	//Tree-Dense areas
	HUMID_VEGETATION(Range.between(-0.35, 4.0),Range.between(0.0, 4.0),2), 
	
	//Savannas
	DRY_VEGETATION(Range.between(-0.35, 4.0),Range.between(-4.0, 0.0),1),
	
	//Deserts
	HOT_BARREN(Range.between(1.0, 4.0),Range.between(-4.0, -1.0),2), 
	
	//Cold biomes - taigas, maybe eroded plains
	COLD(Range.between(-4.0, -0.8),Range.between(-4.0, 4.0),1), 
	
	//Any snowy biomes. 
	SNOWY(Range.between(-4.0, -2.7),Range.between(-4.0, 4.0),2), 	
	
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
