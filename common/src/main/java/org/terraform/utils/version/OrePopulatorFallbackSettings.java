package org.terraform.utils.version;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.populators.OrePopulator;

public class OrePopulatorFallbackSettings {

	public static void repairOreSettings(OrePopulator[] ORE_POPS) {
		
    	if(Version.isAtLeast(17)) {
    		ORE_POPS[0] = new OrePopulator(
    				OneOneSevenBlockHandler.COPPER_ORE, 
    				40, 
    				8,
                    5, 
                    3, 
                    48,
                    96,
                    false);
    		ORE_POPS[1] = new OrePopulator(OneOneSevenBlockHandler.DEEPSLATE, 
    				80, 
    				45,
                    25, 
                    2, 
                    10,
                    15,
                    true);
    		ORE_POPS[2] = new OrePopulator(OneOneSevenBlockHandler.TUFF, 
    				40, 
    				20,
                    10,
                    3,
                    7,
                    10,
                    true);
    	}
    	
		ORE_POPS[3] = new OrePopulator(Material.COAL_ORE,
			             50,
			             25,
			             25,
						 5,
			             35,
			             256,
			             true);
			            
		ORE_POPS[4] = new OrePopulator(Material.IRON_ORE,
			             50,
			             10,
			             30,
			             5,
			             30,
			             64,
			             false);
			            
		ORE_POPS[5] = new OrePopulator(Material.GOLD_ORE,
			             40,
			             10,
			             15,
			             0,
			             16,
			             32,
			             false);
			            
		ORE_POPS[6] = new OrePopulator(Material.DIAMOND_ORE,
						 30,
						 7,
			             3,
			             5,
			             11,
						 15,
			             false);
			            
			            
			            //Emeralds only spawn in mountainous biomes (except deserts)
		ORE_POPS[7] = new OrePopulator(Material.EMERALD_ORE,
			             30,
			             7,
			             3,
			             5,
			             12,
			             15,
			             false,
			             BiomeBank.BIRCH_MOUNTAINS,
			             BiomeBank.ROCKY_MOUNTAINS,
			             BiomeBank.SNOWY_MOUNTAINS);
			            
			            
		ORE_POPS[8] = new OrePopulator(Material.LAPIS_ORE,
			             30,
			             6,
			             15,
			             14,
			             23,
			             33,
			             false);
			            
		ORE_POPS[9] = new OrePopulator(Material.REDSTONE_ORE,
			             40,
			             10,
			             15,
			             5,
			             11,
			             15,
			             false);
			            
			            
			            //Non-ores
		ORE_POPS[10] = new OrePopulator(Material.GRAVEL,
			             75,
			             45,
			             16,
			             5,
			             255,
			             255,
			             true);
			            
		ORE_POPS[11] = new OrePopulator(Material.ANDESITE,
			             80,
			             45,
			             30,
			             5,
			             255,
			             255,
			             true);
			            
		ORE_POPS[12] = new OrePopulator(Material.DIORITE,
			             80,
			             45,
			             30,
			             5,
			             255,
			             255,
			             true);
			            
		ORE_POPS[13] = new OrePopulator(Material.GRANITE,
			             80,
			             45,
			             30,
			             5,
			             255,
			             255,
			             true);
	}
	
}
