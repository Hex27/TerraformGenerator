package org.terraform.utils.version;

import org.bukkit.Material;

public class OneOneSevenBlockHandler {

	private static Material DIRT_PATH = null;
	
	public static Material DIRT_PATH() {
		if(DIRT_PATH == null) {
			try {
				DIRT_PATH = Material.valueOf("DIRT_PATH");
			}
			catch(IllegalArgumentException e) {
				DIRT_PATH = Material.valueOf("GRASS_PATH");
			}
		}
		
		return DIRT_PATH;
	}

}