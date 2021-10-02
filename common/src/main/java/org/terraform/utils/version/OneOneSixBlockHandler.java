package org.terraform.utils.version;

import org.bukkit.Material;

public class OneOneSixBlockHandler {
	
	public static final Material SMOOTH_BASALT = Material.getMaterial("SMOOTH_BASALT") == null ? 
			Material.getMaterial("STONE") : Material.getMaterial("SMOOTH_BASALT");
	public static final Material TARGET = Material.getMaterial("TARGET") == null ? 
			Material.getMaterial("RED_WOOL") : Material.getMaterial("TARGET");
	
	private static Material CHAIN = Material.getMaterial("CHAIN");
    public static Material getChainMaterial() {
    	if(CHAIN == null)
    		CHAIN = Material.IRON_BARS;
        return CHAIN;
    }

}