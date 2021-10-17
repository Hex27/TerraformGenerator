package org.terraform.biome;

public enum BiomeType {
    OCEANIC(false),
    FLAT,
    MOUNTAINOUS,
    HIGH_MOUNTAINOUS,
    BEACH,
    DEEP_OCEANIC(false),
    RIVER(false);
	
	boolean isDry = true;
	
	BiomeType(){
		
	}
	
	BiomeType(boolean isDry){
		this.isDry = isDry;
	}

	public boolean isDry() {
		return isDry;
	}
}
