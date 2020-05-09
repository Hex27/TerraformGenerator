package org.terraform.coregen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.carving.CaveWormCreator;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.populators.AnimalPopulator;
import org.terraform.populators.OrePopulator;
import org.terraform.populators.RiverWormCreator;

public class TerraformPopulator{
    private static Set<SimpleChunkLocation> chunks = new HashSet<SimpleChunkLocation>();
    
    private RiverWormCreator rwc;
    
	private ArrayList<OrePopulator> orePops = new ArrayList<OrePopulator>(){{
		
		//Ores
		add(new OrePopulator(Material.COAL_ORE, TConfigOption.ORE_COAL_CHANCE.getInt(), TConfigOption.ORE_COAL_VEINSIZE.getInt(), TConfigOption.ORE_COAL_MAXVEINNUMBER.getInt(), TConfigOption.ORE_COAL_MINSPAWNHEIGHT.getInt(),TConfigOption.ORE_COAL_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_COAL_MAXSPAWNHEIGHT.getInt()));
		add(new OrePopulator(Material.IRON_ORE, TConfigOption.ORE_IRON_CHANCE.getInt(), TConfigOption.ORE_IRON_VEINSIZE.getInt(), TConfigOption.ORE_IRON_MAXVEINNUMBER.getInt(), TConfigOption.ORE_IRON_MINSPAWNHEIGHT.getInt(),TConfigOption.ORE_IRON_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_IRON_MAXSPAWNHEIGHT.getInt()));
		add(new OrePopulator(Material.GOLD_ORE, TConfigOption.ORE_GOLD_CHANCE.getInt(), TConfigOption.ORE_GOLD_VEINSIZE.getInt(), TConfigOption.ORE_GOLD_MAXVEINNUMBER.getInt(), TConfigOption.ORE_GOLD_MINSPAWNHEIGHT.getInt(),TConfigOption.ORE_GOLD_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_GOLD_MAXSPAWNHEIGHT.getInt()));
		add(new OrePopulator(Material.DIAMOND_ORE, TConfigOption.ORE_DIAMOND_CHANCE.getInt(), TConfigOption.ORE_DIAMOND_VEINSIZE.getInt(), TConfigOption.ORE_DIAMOND_MAXVEINNUMBER.getInt(), TConfigOption.ORE_DIAMOND_MINSPAWNHEIGHT.getInt(),TConfigOption.ORE_DIAMOND_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_DIAMOND_MAXSPAWNHEIGHT.getInt()));
		add(new OrePopulator(Material.LAPIS_ORE, TConfigOption.ORE_LAPIS_CHANCE.getInt(), TConfigOption.ORE_LAPIS_VEINSIZE.getInt(), TConfigOption.ORE_LAPIS_MAXVEINNUMBER.getInt(), TConfigOption.ORE_LAPIS_MINSPAWNHEIGHT.getInt(),TConfigOption.ORE_LAPIS_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_LAPIS_MAXSPAWNHEIGHT.getInt()));
		add(new OrePopulator(Material.REDSTONE_ORE, TConfigOption.ORE_REDSTONE_CHANCE.getInt(), TConfigOption.ORE_REDSTONE_VEINSIZE.getInt(), TConfigOption.ORE_REDSTONE_MAXVEINNUMBER.getInt(), TConfigOption.ORE_REDSTONE_MINSPAWNHEIGHT.getInt(),TConfigOption.ORE_REDSTONE_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_REDSTONE_MAXSPAWNHEIGHT.getInt()));
		
		//Non-ores
		add(new OrePopulator(Material.GRAVEL, TConfigOption.ORE_GRAVEL_CHANCE.getInt(), TConfigOption.ORE_GRAVEL_VEINSIZE.getInt(), TConfigOption.ORE_GRAVEL_MAXVEINNUMBER.getInt(), TConfigOption.ORE_GRAVEL_MINSPAWNHEIGHT.getInt(),TConfigOption.ORE_GRAVEL_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_GRAVEL_MAXSPAWNHEIGHT.getInt()));
		add(new OrePopulator(Material.ANDESITE, TConfigOption.ORE_ANDESITE_CHANCE.getInt(), TConfigOption.ORE_ANDESITE_VEINSIZE.getInt(), TConfigOption.ORE_ANDESITE_MAXVEINNUMBER.getInt(), TConfigOption.ORE_ANDESITE_MINSPAWNHEIGHT.getInt(),TConfigOption.ORE_ANDESITE_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_ANDESITE_MAXSPAWNHEIGHT.getInt()));
		add(new OrePopulator(Material.DIORITE, TConfigOption.ORE_DIORITE_CHANCE.getInt(), TConfigOption.ORE_DIORITE_VEINSIZE.getInt(), TConfigOption.ORE_DIORITE_MAXVEINNUMBER.getInt(), TConfigOption.ORE_DIORITE_MINSPAWNHEIGHT.getInt(),TConfigOption.ORE_DIORITE_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_DIORITE_MAXSPAWNHEIGHT.getInt()));
		add(new OrePopulator(Material.GRANITE, TConfigOption.ORE_GRANITE_CHANCE.getInt(), TConfigOption.ORE_GRANITE_VEINSIZE.getInt(), TConfigOption.ORE_GRANITE_MAXVEINNUMBER.getInt(), TConfigOption.ORE_GRANITE_MINSPAWNHEIGHT.getInt(),TConfigOption.ORE_GRANITE_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_GRANITE_MAXSPAWNHEIGHT.getInt()));
	}};
	
	private ArrayList<AnimalPopulator> animalPops = new ArrayList<AnimalPopulator>(){{
		add(new AnimalPopulator(EntityType.PIG, TConfigOption.ANIMALS_PIG_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_PIG_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_PIG_CHANCE.getInt(), false, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_MOUNTAINS));
		add(new AnimalPopulator(EntityType.COW, TConfigOption.ANIMALS_COW_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_COW_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_COW_CHANCE.getInt(), false, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN,BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_MOUNTAINS));
		add(new AnimalPopulator(EntityType.SHEEP, TConfigOption.ANIMALS_SHEEP_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_SHEEP_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_SHEEP_CHANCE.getInt(), false, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN,BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_MOUNTAINS));
		add(new AnimalPopulator(EntityType.CHICKEN, TConfigOption.ANIMALS_CHICKEN_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_CHICKEN_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_CHICKEN_CHANCE.getInt(), false, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN,BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_MOUNTAINS));
		add(new AnimalPopulator(EntityType.HORSE, TConfigOption.ANIMALS_HORSE_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_HORSE_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_HORSE_CHANCE.getInt(), true, BiomeBank.PLAINS, BiomeBank.SAVANNA));
		add(new AnimalPopulator(EntityType.DONKEY, TConfigOption.ANIMALS_DONKEY_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_DONKEY_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_DONKEY_CHANCE.getInt(), true, BiomeBank.PLAINS, BiomeBank.SAVANNA));
		add(new AnimalPopulator(EntityType.RABBIT, TConfigOption.ANIMALS_RABBIT_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_RABBIT_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_RABBIT_CHANCE.getInt(), true, BiomeBank.DESERT, BiomeBank.FOREST, BiomeBank.TAIGA, BiomeBank.SNOWY_TAIGA, BiomeBank.ROCKY_BEACH, BiomeBank.SNOWY_WASTELAND));
		add(new AnimalPopulator(EntityType.POLAR_BEAR, TConfigOption.ANIMALS_POLAR_BEAR_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_POLAR_BEAR_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_POLAR_BEAR_CHANCE.getInt(), true, BiomeBank.ICE_SPIKES, BiomeBank.FROZEN_OCEAN, BiomeBank.SNOWY_TAIGA, BiomeBank.ROCKY_BEACH, BiomeBank.SNOWY_WASTELAND));
		//add(new AnimalPopulator(EntityType.PANDA, 2, 3, 100, true, BiomeBank.ICE_SPIKES, BiomeBank.FROZEN_OCEAN, BiomeBank.SNOWY_TAIGA, BiomeBank.ROCKY_BEACH, BiomeBank.SNOWY_WASTELAND));
	}};

	
	private CaveWormCreator cavePop;
	
	public TerraformPopulator(TerraformWorld tw){
		this.rwc = new RiverWormCreator(tw);
	}
	
	public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		
		//ores
		for(OrePopulator ore:orePops){
			//TerraformGeneratorPlugin.logger.info("Generating ores...");
			ore.populate(tw, random, data);
		}
		
		//Biome specific populators
		ArrayList<BiomeBank> banks = new ArrayList<>();
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int height = new HeightMap().getHeight(tw, x, z);//GenUtils.getTrueHighestBlock(data, x, z);
				for(BiomeBank bank:BiomeBank.values()){
					BiomeBank currentBiome = tw.getBiomeBank(x, height, z);//BiomeBank.calculateBiome(tw,tw.getTemperature(x, z), height);
					
					if(bank == currentBiome){
						if(!banks.contains(bank))
							banks.add(bank);
						break;
					}
				}
			}
		}	
		
		for(BiomeBank bank:banks){
			//TerraformGeneratorPlugin.logger.info("Populating for biome: " + bank.toString());
			bank.getHandler().populate(tw, random, data);
		}
		
		ArrayList<EntityType> spawned = new ArrayList<>();
		//TerraformGeneratorPlugin.logger.debug("animal-populator eval for " + data.getChunkX() + "," + data.getChunkZ());
		for(AnimalPopulator pop:animalPops){
			if(pop.canSpawn(banks,random) && 
					!spawned.contains(pop.getAnimalType())){
				//TerraformGeneratorPlugin.logger.debug("animal populator proc");
				pop.populate(tw, random, data);
				spawned.add(pop.getAnimalType());
			}
		}
		spawned.clear();
	}
}
