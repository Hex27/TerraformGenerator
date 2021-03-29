package org.terraform.biome;

import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Range;

public enum BiomeClimate {

	//Tree-Dense Jungles
	HUMID_VEGETATION(Range.between(2.45, 2.5),Range.between(2.45, 2.5),2), 
	
	//Forests
	WARM_VEGETATION(Range.between(1.0, 2.5),Range.between(1.0, 2.5),1),
	
	//Savannas
	DRY_VEGETATION(Range.between(1.0, 2.5),Range.between(-2.5, -1.0),1),
	
	//Deserts
	HOT_BARREN(Range.between(2.45, 2.5),Range.between(-2.5, -2.45),2), 
	
	//Cold biomes - taigas, maybe eroded plains
	COLD(Range.between(-2.5, -1.0),Range.between(-2.5, 2.5),1), 
	
	//Any snowy biomes. 
	SNOWY(Range.between(-2.5, -2.47),Range.between(-2.5, 2.5),2), 	
	
	//Default climate.
	TRANSITION(Range.between(-2.5,2.5),Range.between(-2.5,2.5),0), 
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
				
				//Candidate with most specific moisture AND temperature range wins.
				//No tie breaking is done - don't include closely overlapping climates.
				if(candidate == null)
					candidate = climate;
				else if(candidate.priority < climate.priority)
					candidate = climate;
			}
		
		return candidate;
	}
	
}
