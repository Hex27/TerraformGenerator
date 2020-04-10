package org.terraform.coregen;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.populators.AnimalPopulator;

public class TerraformAnimalSpawner extends BlockPopulator {

	private ArrayList<AnimalPopulator> animalPops = new ArrayList<AnimalPopulator>(){{
		add(new AnimalPopulator(EntityType.PIG, TConfigOption.ANIMALS_PIG_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_PIG_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_PIG_CHANCE.getInt(), false, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.SWAMP));
		add(new AnimalPopulator(EntityType.COW, TConfigOption.ANIMALS_COW_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_COW_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_COW_CHANCE.getInt(), false, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.SWAMP));
		add(new AnimalPopulator(EntityType.SHEEP, TConfigOption.ANIMALS_SHEEP_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_SHEEP_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_SHEEP_CHANCE.getInt(), false, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.SWAMP));
		add(new AnimalPopulator(EntityType.CHICKEN, TConfigOption.ANIMALS_CHICKEN_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_CHICKEN_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_CHICKEN_CHANCE.getInt(), false, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.SWAMP));
		add(new AnimalPopulator(EntityType.HORSE, TConfigOption.ANIMALS_HORSE_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_HORSE_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_HORSE_CHANCE.getInt(), true, BiomeBank.PLAINS, BiomeBank.SAVANNA));
		add(new AnimalPopulator(EntityType.DONKEY, TConfigOption.ANIMALS_DONKEY_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_DONKEY_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_DONKEY_CHANCE.getInt(), true, BiomeBank.PLAINS, BiomeBank.SAVANNA));
		add(new AnimalPopulator(EntityType.RABBIT, TConfigOption.ANIMALS_RABBIT_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_RABBIT_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_RABBIT_CHANCE.getInt(), true, BiomeBank.DESERT, BiomeBank.FOREST, BiomeBank.TAIGA, BiomeBank.SNOWY_TAIGA, BiomeBank.ROCKY_BEACH, BiomeBank.SNOWY_WASTELAND));
		add(new AnimalPopulator(EntityType.POLAR_BEAR, TConfigOption.ANIMALS_POLAR_BEAR_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_POLAR_BEAR_MAXHERDSIZE.getInt(), TConfigOption.ANIMALS_POLAR_BEAR_CHANCE.getInt(), true, BiomeBank.ICE_SPIKES, BiomeBank.FROZEN_OCEAN, BiomeBank.SNOWY_TAIGA, BiomeBank.ROCKY_BEACH, BiomeBank.SNOWY_WASTELAND));
		//add(new AnimalPopulator(EntityType.PANDA, 2, 3, 100, true, BiomeBank.ICE_SPIKES, BiomeBank.FROZEN_OCEAN, BiomeBank.SNOWY_TAIGA, BiomeBank.ROCKY_BEACH, BiomeBank.SNOWY_WASTELAND));
	}};
	
	@Override
	public void populate(World world, Random random, Chunk chunk) {
		TerraformWorld tw = TerraformWorld.get(world);
		//Don't attempt generation pre-injection.
		if(!TerraformGeneratorPlugin.injectedWorlds.contains(world.getName())) 
			return;
		
		PopulatorDataPostGen data = new PopulatorDataPostGen(chunk);
		
		ArrayList<BiomeBank> banks = new ArrayList<>();
		
		//Fetch present biomes
		for(int x = data.getChunkX()<<4; x < data.getChunkX()<<4+16; x++){
			for(int z = data.getChunkZ()<<4; z < data.getChunkZ()<<4+16; z++){
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
