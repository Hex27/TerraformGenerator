package org.terraform.main;

import java.util.List;

import org.bukkit.ChatColor;
import org.drycell.config.DCConfig;

public enum TConfigOption{

	HEIGHT_MAP_MOUNTAIN_FREQUENCY("heightmap.mountain-frequency",0.002f),
	HEIGHT_MAP_OCEANIC_FREQUENCY("heightmap.oceanic-frequency",0.001f),
	HEIGHT_MAP_SEA_LEVEL("heightmap.sea-level",62),
	HEIGHT_MAP_DEEP_SEA_LEVEL("heightmap.deep-sea-level",35),
	
	BIOME_TEMPERATURE_FREQUENCY("biome.temperature-frequency",0.001f),
	BIOME_MOISTURE_FREQUENCY("biome.moisture-frequency",0.001f),
	BIOME_DITHER("biome.dithering",0.1d),
	
	//MISC_SMOOTH_DESIGN("misc.smooth-design",false),
	MISC_SAPLING_CUSTOM_TREES_ENABLED("misc.custom-small-trees-from-saplings.enabled",false),
	MISC_SAPLING_CUSTOM_TREES_BIGTREES("misc.custom-small-trees-from-saplings.big-jungle-tree",true),
	
	DEVSTUFF_DEBUG_MODE("dev-stuff.debug-mode",false),
	DEVSTUFF_ATTEMPT_FIXING_PREMATURE("dev-stuff.attempt-fixing-premature-generations",true),
	
	//-=[CAVES]=-
	//CAVES_ALLOW_FLOODED_CAVES("caves.allow-flooded-caves",true),
	//CAVES_ALLOW_FLOODED_RAVINES("caves.allow-flooded-ravines",true),
	
	//-=[STRUCTURES]=-
	STRUCTURES_MEGACHUNK_BITSHIFTS("structures.technical.megachunk.bitshifts",6),
	STRUCTURES_STRONGHOLD_ENABLED("structures.stronghold.enabled",true),
	STRUCTURES_MONUMENT_ENABLED("structures.monument.enabled",true),
	STRUCTURES_MONUMENT_SPAWNRATIO("structures.monument.spawn-ratio",1.0),
	STRUCTURES_FARMHOUSE_ENABLED("structures.farmhouse.enabled",true),
	STRUCTURES_ANIMALFARM_ENABLED("structures.animalfarm.enabled",true),
	STRUCTURES_SWAMPHUT_ENABLED("structures.swamphut.enabled",true),
	STRUCTURES_SWAMPHUT_CHANCE_OUT_OF_TEN_THOUSAND("structures.swamphut.chance-out-of-10000",10),
	STRUCTURES_UNDERGROUNDDUNGEON_ENABLED("structures.underground-dungeon.enabled",true),
	STRUCTURES_DROWNEDDUNGEON_ENABLED("structures.drowned-dungeon.enabled",true),
	STRUCTURES_SHIPWRECK_ENABLED("structures.shipwreck.enabled",true),
	STRUCTURES_MINESHAFT_ENABLED("structures.mineshaft.enabled",true),
	STRUCTURES_MINESHAFT_CHANCE("structures.mineshaft.chance",75),
	STRUCTURES_LARGECAVE_ENABLED("structures.largecave.enabled",true),
	STRUCTURES_LARGECAVE_CHANCE("structures.largecave.chance",75),
	
	//-=[ANIMALS]=-
		//BEES
	ANIMALS_BEE_HIVEFREQUENCY("animals.bee.hive-frequency",0.02),
		//PIG
	ANIMALS_PIG_MINHERDSIZE("animals.pig.min-herd-size",3),
	ANIMALS_PIG_MAXHERDSIZE("animals.pig.max-herd-size",4),
	ANIMALS_PIG_CHANCE("animals.pig.chance",2),
		//COW
	ANIMALS_COW_MINHERDSIZE("animals.cow.min-herd-size",4),
	ANIMALS_COW_MAXHERDSIZE("animals.cow.max-herd-size",12),
	ANIMALS_COW_CHANCE("animals.cow.chance",2),
		//SHEEP
	ANIMALS_SHEEP_MINHERDSIZE("animals.sheep.min-herd-size",2),
	ANIMALS_SHEEP_MAXHERDSIZE("animals.sheep.max-herd-size",8),
	ANIMALS_SHEEP_CHANCE("animals.sheep.chance",2),
		//CHICKEN
	ANIMALS_CHICKEN_MINHERDSIZE("animals.chicken.min-herd-size",1),
	ANIMALS_CHICKEN_MAXHERDSIZE("animals.chicken.max-herd-size",3),
	ANIMALS_CHICKEN_CHANCE("animals.chicken.chance",2),
		//HORSE
	ANIMALS_HORSE_MINHERDSIZE("animals.horse.min-herd-size",2),
	ANIMALS_HORSE_MAXHERDSIZE("animals.horse.max-herd-size",6),
	ANIMALS_HORSE_CHANCE("animals.horse.chance",1),
		//DONKEY
	ANIMALS_DONKEY_MINHERDSIZE("animals.donkey.min-herd-size",2),
	ANIMALS_DONKEY_MAXHERDSIZE("animals.donkey.max-herd-size",6),
	ANIMALS_DONKEY_CHANCE("animals.donkey.chance",1),
		//RABBIT
	ANIMALS_RABBIT_MINHERDSIZE("animals.rabbit.min-herd-size",2),
	ANIMALS_RABBIT_MAXHERDSIZE("animals.rabbit.max-herd-size",3),
	ANIMALS_RABBIT_CHANCE("animals.rabbit.chance",1),
		//POLAR_BEAR
	ANIMALS_POLAR_BEAR_MINHERDSIZE("animals.polarbear.min-herd-size",2),
	ANIMALS_POLAR_BEAR_MAXHERDSIZE("animals.polarbear.max-herd-size",3),
	ANIMALS_POLAR_BEAR_CHANCE("animals.polarbear.chance",1),
	
	//-=[ORES]=-
		//COAL
	ORE_COAL_CHANCE("ore.coal.chance-per-chunk",70),
	ORE_COAL_VEINSIZE("ore.coal.max-vein-size",30),
	ORE_COAL_MAXVEINNUMBER("ore.coal.max-vein-count",50),
	ORE_COAL_COMMONSPAWNHEIGHT("ore.coal.common-spawn-height",128),
	ORE_COAL_MAXSPAWNHEIGHT("ore.coal.max-spawn-height",131),
	ORE_COAL_MINSPAWNHEIGHT("ore.coal.min-spawn-height",5),
	
		//IRON
	ORE_IRON_CHANCE("ore.iron.chance-per-chunk",50),
	ORE_IRON_VEINSIZE("ore.iron.max-vein-size",10),
	ORE_IRON_MAXVEINNUMBER("ore.iron.max-vein-count",30),
	ORE_IRON_COMMONSPAWNHEIGHT("ore.iron.common-spawn-height",64),
	ORE_IRON_MAXSPAWNHEIGHT("ore.iron.max-spawn-height",67),
	ORE_IRON_MINSPAWNHEIGHT("ore.iron.min-spawn-height",5),

		//GOLD
	ORE_GOLD_CHANCE("ore.gold.chance-per-chunk",40),
	ORE_GOLD_VEINSIZE("ore.gold.max-vein-size",10),
	ORE_GOLD_MAXVEINNUMBER("ore.gold.max-vein-count",15),
	ORE_GOLD_COMMONSPAWNHEIGHT("ore.gold.common-spawn-height",29),
	ORE_GOLD_MAXSPAWNHEIGHT("ore.gold.max-spawn-height",33),
	ORE_GOLD_MINSPAWNHEIGHT("ore.gold.min-spawn-height",5),
	
		//DIAMOND
	ORE_DIAMOND_CHANCE("ore.diamond.chance-per-chunk",40),
	ORE_DIAMOND_VEINSIZE("ore.diamond.max-vein-size",7),
	ORE_DIAMOND_MAXVEINNUMBER("ore.diamond.max-vein-count",5),
	ORE_DIAMOND_COMMONSPAWNHEIGHT("ore.diamond.common-spawn-height",12),
	ORE_DIAMOND_MAXSPAWNHEIGHT("ore.diamond.max-spawn-height",15),
	ORE_DIAMOND_MINSPAWNHEIGHT("ore.diamond.min-spawn-height",5),

		//LAPIS
	ORE_LAPIS_CHANCE("ore.lapis.chance-per-chunk",40),
	ORE_LAPIS_VEINSIZE("ore.lapis.max-vein-size",7),
	ORE_LAPIS_MAXVEINNUMBER("ore.lapis.max-vein-count",15),
	ORE_LAPIS_COMMONSPAWNHEIGHT("ore.lapis.common-spawn-height",23),
	ORE_LAPIS_MAXSPAWNHEIGHT("ore.lapis.max-spawn-height",33),
	ORE_LAPIS_MINSPAWNHEIGHT("ore.lapis.min-spawn-height",14),
	
		//REDSTONE
	ORE_REDSTONE_CHANCE("ore.redstone.chance-per-chunk",40),
	ORE_REDSTONE_VEINSIZE("ore.redstone.max-vein-size",10),
	ORE_REDSTONE_MAXVEINNUMBER("ore.redstone.max-vein-count",15),
	ORE_REDSTONE_COMMONSPAWNHEIGHT("ore.redstone.common-spawn-height",12),
	ORE_REDSTONE_MAXSPAWNHEIGHT("ore.redstone.max-spawn-height",15),
	ORE_REDSTONE_MINSPAWNHEIGHT("ore.redstone.min-spawn-height",5),

		//GRAVEL
	ORE_GRAVEL_CHANCE("ore.gravel.chance-per-chunk",70),
	ORE_GRAVEL_VEINSIZE("ore.gravel.max-vein-size",33),
	ORE_GRAVEL_MAXVEINNUMBER("ore.gravel.max-vein-count",8),
	ORE_GRAVEL_COMMONSPAWNHEIGHT("ore.gravel.common-spawn-height",255),
	ORE_GRAVEL_MAXSPAWNHEIGHT("ore.gravel.max-spawn-height",255),
	ORE_GRAVEL_MINSPAWNHEIGHT("ore.gravel.min-spawn-height",5),
	
		//ANDESITE
	ORE_ANDESITE_CHANCE("ore.andesite.chance-per-chunk",70),
	ORE_ANDESITE_VEINSIZE("ore.andesite.max-vein-size",33),
	ORE_ANDESITE_MAXVEINNUMBER("ore.andesite.max-vein-count",8),
	ORE_ANDESITE_COMMONSPAWNHEIGHT("ore.andesite.common-spawn-height",80),
	ORE_ANDESITE_MAXSPAWNHEIGHT("ore.andesite.max-spawn-height",80),
	ORE_ANDESITE_MINSPAWNHEIGHT("ore.andesite.min-spawn-height",5),

		//DIORITE
	ORE_DIORITE_CHANCE("ore.diorite.chance-per-chunk",70),
	ORE_DIORITE_VEINSIZE("ore.diorite.max-vein-size",33),
	ORE_DIORITE_MAXVEINNUMBER("ore.diorite.max-vein-count",8),
	ORE_DIORITE_COMMONSPAWNHEIGHT("ore.diorite.common-spawn-height",80),
	ORE_DIORITE_MAXSPAWNHEIGHT("ore.diorite.max-spawn-height",80),
	ORE_DIORITE_MINSPAWNHEIGHT("ore.diorite.min-spawn-height",5),
	
		//GRANITE
	ORE_GRANITE_CHANCE("ore.granite.chance-per-chunk",70),
	ORE_GRANITE_VEINSIZE("ore.granite.max-vein-size",33),
	ORE_GRANITE_MAXVEINNUMBER("ore.granite.max-vein-count",8),
	ORE_GRANITE_COMMONSPAWNHEIGHT("ore.granite.common-spawn-height",80),
	ORE_GRANITE_MAXSPAWNHEIGHT("ore.granite.max-spawn-height",80),
	ORE_GRANITE_MINSPAWNHEIGHT("ore.granite.min-spawn-height",5),
	
	;
	String path;
	Object value;
	
	TConfigOption(String path, Object value){
		this.path = path;
		this.value = value;
	}
	
	public String getString(){
		return ChatColor.translateAlternateColorCodes('&', (String) value);
	}
	
	
	public String parse(String... placeholders){
		String parsed = this.getString();
		
		String placeholder = "";
		
		for(int i = 0; i < placeholders.length; i++){
			if(i%2 == 0){
				placeholder = placeholders[i];
			}else{
				parsed = parsed.replaceAll(placeholder, placeholders[i]);
			}
		}
		return parsed;
	}
	
	
	public int getInt(){
		if(value instanceof Number){
			return ((Number) value).intValue();
		}
		return (int) value;
	}
	
	public boolean getBoolean(){
		return ((Boolean) value).booleanValue();
	}
	
	public double getDouble(){
		if(value instanceof Number){
			return ((Number) value).doubleValue();
		}
		return (double) value;
	}
	
	public float getFloat(){
		if(value instanceof Number){
			return ((Number) value).floatValue();
		}
		return (float) value;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getStringList(){
		return (List<String>) value;
	}
	
	public String[] getStringArray(){
		String[] arr = new String[getStringList().size()];
		int i = 0;
		for(String item:getStringList()){
			arr[i] = item;
			i++;
		}
		return arr;
	}
	
	
	public static void loadValues(DCConfig conf){
		for(TConfigOption option:TConfigOption.values()){
			conf.reg(option.path, option.value);
		}
		conf.load();
		for(TConfigOption option:TConfigOption.values()){
			option.value = conf.get(option.path);
		}
	}
	

}
