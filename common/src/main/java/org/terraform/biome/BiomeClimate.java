package org.terraform.biome;

import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Range;

public enum BiomeClimate {

	//Tree-Dense forests
	TROPICAL(Range.between(1.1, 2.5),Range.between(1.1, 2.5)), 
	
	//Deserts
	DRY(Range.between(1.1, 2.5),Range.between(-2.5, -0.9)), 
	
	//Normal tree forests and birch mountains
	CONTINENTAL(Range.between(-2.5, -0.9),Range.between(-2.5, -0.9)), 
	
	//Snowy
	POLAR(Range.between(-2.5, -0.9),Range.between(1.1, 2.5)), 	
	
	//Used between different climates. 
	//This normally includes flat areas like plains and savannas
	TRANSITION(Range.between(-1.0,1.0),Range.between(-1.0,1.0)), 
	; 
	
	Range<Double> temperatureRange;
	Range<Double> moistureRange;
	
	BiomeClimate(Range<Double> temperatureRange, Range<Double> moistureRange){
		this.temperatureRange = temperatureRange;
		this.moistureRange = moistureRange;
	}

	public Range<Double> getTemperatureRange() {
		return temperatureRange;
	}

	public Range<Double> getMoistureRange() {
		return moistureRange;
	}
	
	public static BiomeClimate selectClimate(double temp, double moist) {
		
		for(BiomeClimate climate:BiomeClimate.values())
			if(climate.getTemperatureRange().contains(temp)
					&& climate.getMoistureRange().contains(moist))
				return climate;
		return BiomeClimate.TRANSITION;
	}
	
}
