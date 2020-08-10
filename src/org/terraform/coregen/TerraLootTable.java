package org.terraform.coregen;

public enum TerraLootTable {
	SPAWN_BONUS_CHEST("chests/spawn_bonus_chest"),
    END_CITY_TREASURE("chests/end_city_treasure"),
    SIMPLE_DUNGEON("chests/simple_dungeon"),
    VILLAGE_WEAPONSMITH("chests/village/village_weaponsmith"),
    VILLAGE_TOOLSMITH("chests/village/village_toolsmith"),
    VILLAGE_ARMORER("chests/village/village_armorer"),
    VILLAGE_CARTOGRAPHER("chests/village/village_cartographer"),
    VILLAGE_MASON("chests/village/village_mason"),
    VILLAGE_SHEPHERD("chests/village/village_shepherd"),
    VILLAGE_BUTCHER("chests/village/village_butcher"),
    VILLAGE_FLETCHER("chests/village/village_fletcher"),
    VILLAGE_FISHER("chests/village/village_fisher"),
    VILLAGE_TANNERY("chests/village/village_tannery"),
    VILLAGE_TEMPLE("chests/village/village_temple"),
    VILLAGE_DESERT_HOUSE("chests/village/village_desert_house"),
    VILLAGE_PLAINS_HOUSE("chests/village/village_plains_house"),
    VILLAGE_TAIGA_HOUSE("chests/village/village_taiga_house"),
    VILLAGE_SNOWY_HOUSE("chests/village/village_snowy_house"),
    VILLAGE_SAVANNA_HOUSE("chests/village/village_savanna_house"),
    ABANDONED_MINESHAFT("chests/abandoned_mineshaft"),
    NETHER_BRIDGE("chests/nether_bridge"),
    STRONGHOLD_LIBRARY("chests/stronghold_library"),
    STRONGHOLD_CROSSING("chests/stronghold_crossing"),
    STRONGHOLD_CORRIDOR("chests/stronghold_corridor"),
    DESERT_PYRAMID("chests/desert_pyramid"),
    JUNGLE_TEMPLE("chests/jungle_temple"),
    JUNGLE_TEMPLE_DISPENSER("chests/jungle_temple_dispenser"),
    IGLOO_CHEST("chests/igloo_chest"),
    WOODLAND_MANSION("chests/woodland_mansion"),
    UNDERWATER_RUIN_SMALL("chests/underwater_ruin_small"),
    UNDERWATER_RUIN_BIG("chests/underwater_ruin_big"),
    BURIED_TREASURE("chests/buried_treasure"),
    SHIPWRECK_MAP("chests/shipwreck_map"),
    SHIPWRECK_SUPPLY("chests/shipwreck_supply"),
    SHIPWRECK_TREASURE("chests/shipwreck_treasure"),
    PILLAGER_OUTPOST("chests/pillager_outpost");
	
	@SuppressWarnings("unused")
	private String key;
	TerraLootTable(String key){
		this.key = key;
	}
}
