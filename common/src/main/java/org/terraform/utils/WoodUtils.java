package org.terraform.utils;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;

public class WoodUtils {
	
	public static enum WoodSpecies{
		OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK
    }
	
	public static enum WoodType{
		PLANKS("%WOOD%_PLANKS"),
		SAPLING("%WOOD%_SAPLING"),
		POTTED_SAPLING("POTTED_%WOOD%_SAPLING"),
		LOG("%WOOD%_LOG"),
		STRIPPED_LOG("STRIPPED_%WOOD%_LOG"),
		WOOD("%WOOD%_WOOD"),
		STRIPPED_WOOD("STRIPPED_%WOOD%_WOOD"),
		LEAVES("%WOOD%_LEAVES"),
		SLAB("%WOOD%_SLAB"),
		PRESSURE_PLATE("%WOOD%_PRESSURE_PLATE"),
		FENCE("%WOOD%_FENCE"),
		TRAPDOOR("%WOOD%_TRAPDOOR"),
		FENCE_GATE("%WOOD%_FENCE_GATE"),
		STAIRS("%WOOD%_STAIRS"),
		BUTTON("%WOOD%_BUTTON"),
		DOOR("%WOOD%_DOOR"),
		SIGN("%WOOD%_SIGN"),
		WALL_SIGN("%WOOD%_WALL_SIGN"),
		BOAT("%WOOD%_BOAT"),
		;
		String template;
		WoodType(String template){
			this.template = template;
		}
		
		/**
		 * Converts an oak material to a WoodType template.
		 * Do not use for any other wood.
		 * <br><br>
		 * The dark oak check is there because schematics only check for the 
		 * keyword "oak"
		 * @param oak material to convert to an WoodType enum
		 * @return
		 */
		public static WoodType parse(Material oak) {
			return WoodType.valueOf(oak.toString()
					.replace("DARK_OAK","OAK")
					.replace("OAK_",""));
		}

		//I am the pinnacle of optimisation
		//Fear my absolutely unbeatable timings
		public Material getWood(WoodSpecies species) {
			return Material.getMaterial(template.replace("%WOOD%", species.toString()));
		}
	}
	
    public static Material getWoodForBiome(BiomeBank biome, WoodType wood) {
        switch (biome) {
            case BADLANDS:
    		case BADLANDS_RIVER:
            case SAVANNA:
            case DESERT_MOUNTAINS:
            case DESERT:
    		case DESERT_RIVER:
    		case BADLANDS_BEACH:
            case BADLANDS_CANYON:
                return wood.getWood(WoodSpecies.ACACIA);
            case BIRCH_MOUNTAINS:
            case SCARLET_FOREST:
                return wood.getWood(WoodSpecies.BIRCH);
            case COLD_OCEAN:
            case WARM_OCEAN:
            case SWAMP:
            case PLAINS:
            case OCEAN:
            case MUDFLATS:
            case CORAL_REEF_OCEAN:
    		case DEEP_LUKEWARM_OCEAN:
    		case DEEP_OCEAN:
    		case DEEP_WARM_OCEAN:
    		case DEEP_DRY_OCEAN:
    		case DEEP_HUMID_OCEAN:
    		case DRY_OCEAN:
    		case HUMID_OCEAN:
    		case RIVER:
    		case ERODED_PLAINS:
            case FOREST:
                return wood.getWood(WoodSpecies.OAK);
            case FROZEN_OCEAN:
            case TAIGA:
            case SNOWY_WASTELAND:
            case SNOWY_TAIGA:
            case SNOWY_MOUNTAINS:
            case ROCKY_MOUNTAINS:
            case ROCKY_BEACH:
    		case FROZEN_RIVER:
    		case DEEP_COLD_OCEAN:
    		case DEEP_FROZEN_OCEAN:
    		case ICY_BEACH:
            case ICE_SPIKES:
                return wood.getWood(WoodSpecies.SPRUCE);
            case SANDY_BEACH:
            case JUNGLE:
    		case JUNGLE_RIVER:
    		case BAMBOO_FOREST:
                return wood.getWood(WoodSpecies.JUNGLE);
			case BLACK_OCEAN:
			case DEEP_BLACK_OCEAN:
			case CHERRY_GROVE:
			case DARK_FOREST:
			case DARK_FOREST_RIVER:
			case DARK_FOREST_BEACH:
                return wood.getWood(WoodSpecies.DARK_OAK);
		default:
			break;
        }
        return wood.getWood(WoodSpecies.OAK);
    }

}
