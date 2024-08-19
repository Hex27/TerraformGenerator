package org.terraform.coregen.bukkit;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.populators.AnimalPopulator;
import org.terraform.utils.version.OneTwentyFiveBlockHandler;
import org.terraform.utils.version.Version;

import java.util.Random;

public class TerraformAnimalPopulator extends BlockPopulator {

    private final TerraformWorld tw;

    private static final AnimalPopulator[] ANIMAL_POPULATORS = {
    		null, // Slot for goat
            null, // Slot for armadillo
    		
            new AnimalPopulator(EntityType.PIG, TConfigOption.ANIMALS_PIG_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_PIG_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_PIG_CHANCE.getInt(), false, BiomeBank.BLACK_OCEAN, BiomeBank.MUSHROOM_ISLANDS, BiomeBank.MUSHROOM_BEACH,BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_CANYON),
            
            new AnimalPopulator(EntityType.COW, TConfigOption.ANIMALS_COW_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_COW_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_COW_CHANCE.getInt(), false, BiomeBank.BLACK_OCEAN, BiomeBank.MUSHROOM_ISLANDS, BiomeBank.MUSHROOM_BEACH, BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_CANYON),
            
            new AnimalPopulator(EntityType.SHEEP, TConfigOption.ANIMALS_SHEEP_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_SHEEP_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_SHEEP_CHANCE.getInt(), false, BiomeBank.BLACK_OCEAN, BiomeBank.MUSHROOM_ISLANDS, BiomeBank.MUSHROOM_BEACH, BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_CANYON),
            
            new AnimalPopulator(EntityType.CHICKEN, TConfigOption.ANIMALS_CHICKEN_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_CHICKEN_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_CHICKEN_CHANCE.getInt(), false, BiomeBank.BLACK_OCEAN, BiomeBank.MUSHROOM_ISLANDS, BiomeBank.MUSHROOM_BEACH, BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_CANYON),
            
            new AnimalPopulator(EntityType.HORSE, TConfigOption.ANIMALS_HORSE_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_HORSE_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_HORSE_CHANCE.getInt(), true, BiomeBank.PLAINS, BiomeBank.SAVANNA),
            
            new AnimalPopulator(EntityType.DONKEY, TConfigOption.ANIMALS_DONKEY_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_DONKEY_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_DONKEY_CHANCE.getInt(), true, BiomeBank.PLAINS, BiomeBank.SAVANNA),
            
            new AnimalPopulator(EntityType.RABBIT, TConfigOption.ANIMALS_RABBIT_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_RABBIT_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_RABBIT_CHANCE.getInt(), true, BiomeBank.DESERT, BiomeBank.FOREST, BiomeBank.TAIGA, BiomeBank.SNOWY_TAIGA, BiomeBank.ROCKY_BEACH,
                    BiomeBank.SNOWY_WASTELAND),
            
            new AnimalPopulator(EntityType.POLAR_BEAR, TConfigOption.ANIMALS_POLAR_BEAR_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_POLAR_BEAR_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_POLAR_BEAR_CHANCE.getInt(), true, BiomeBank.ICE_SPIKES, BiomeBank.SNOWY_TAIGA, BiomeBank.ICY_BEACH, BiomeBank.SNOWY_WASTELAND),
            
            new AnimalPopulator(EntityType.PANDA, TConfigOption.ANIMALS_PANDA_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_PANDA_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_PANDA_CHANCE.getInt(), true, BiomeBank.BAMBOO_FOREST),

            new AnimalPopulator(EntityType.FOX, TConfigOption.ANIMALS_FOX_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_FOX_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_FOX_CHANCE.getInt(), true, BiomeBank.TAIGA, BiomeBank.SNOWY_TAIGA),

            new AnimalPopulator(EntityType.LLAMA, TConfigOption.ANIMALS_LLAMA_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_LLAMA_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_LLAMA_CHANCE.getInt(), true, BiomeBank.SAVANNA, BiomeBank.ROCKY_MOUNTAINS),

            new AnimalPopulator(EntityType.PARROT, TConfigOption.ANIMALS_PARROT_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_PARROT_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_PARROT_CHANCE.getInt(), true, BiomeBank.JUNGLE),

            new AnimalPopulator(EntityType.OCELOT, TConfigOption.ANIMALS_OCELOT_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_OCELOT_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_OCELOT_CHANCE.getInt(), true, BiomeBank.JUNGLE, BiomeBank.BAMBOO_FOREST),

            new AnimalPopulator(EntityType.WOLF, TConfigOption.ANIMALS_WOLF_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_WOLF_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_WOLF_CHANCE.getInt(), true, BiomeBank.FOREST, BiomeBank.TAIGA, BiomeBank.SNOWY_TAIGA, BiomeBank.DARK_FOREST),

            new AnimalPopulator(EntityType.TURTLE, TConfigOption.ANIMALS_TURTLE_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_TURTLE_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_TURTLE_CHANCE.getInt(), true, BiomeBank.SANDY_BEACH),

            new AnimalPopulator(EntityType.DOLPHIN, TConfigOption.ANIMALS_DOLPHIN_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_DOLPHIN_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_DOLPHIN_CHANCE.getInt(), true, BiomeBank.OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN)
            .setAquatic(true),
    
            new AnimalPopulator(EntityType.COD, TConfigOption.ANIMALS_COD_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_COD_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_COD_CHANCE.getInt(), true, BiomeBank.OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.DEEP_COLD_OCEAN)
            .setAquatic(true),
            
            new AnimalPopulator(EntityType.SQUID, TConfigOption.ANIMALS_SQUID_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_SQUID_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_SQUID_CHANCE.getInt(), true, 
                    BiomeBank.FROZEN_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN,
                    BiomeBank.COLD_OCEAN, BiomeBank.DEEP_COLD_OCEAN,
                    BiomeBank.BLACK_OCEAN,BiomeBank.DEEP_BLACK_OCEAN,
                    BiomeBank.OCEAN, BiomeBank.DEEP_OCEAN, 
                    BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.WARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN,
                    BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.JUNGLE_RIVER)
            .setAquatic(true),
            
            new AnimalPopulator(EntityType.SALMON, TConfigOption.ANIMALS_SALMON_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_SALMON_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_SALMON_CHANCE.getInt(), true, 
                    BiomeBank.COLD_OCEAN, BiomeBank.DEEP_COLD_OCEAN, 
                    BiomeBank.FROZEN_OCEAN, BiomeBank.DEEP_COLD_OCEAN, 
                    BiomeBank.RIVER, BiomeBank.FROZEN_RIVER)
            .setAquatic(true),
            
            new AnimalPopulator(EntityType.PUFFERFISH, TConfigOption.ANIMALS_PUFFERFISH_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_PUFFERFISH_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_PUFFERFISH_CHANCE.getInt(), true, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN)
            .setAquatic(true),
            
            new AnimalPopulator(EntityType.TROPICAL_FISH, TConfigOption.ANIMALS_TROPICALFISH_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_TROPICALFISH_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_TROPICALFISH_CHANCE.getInt(), true, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN)
            .setAquatic(true),

            new AnimalPopulator(EntityType.MUSHROOM_COW, TConfigOption.ANIMALS_MOOSHROOM_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_MOOSHROOM_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_MOOSHROOM_CHANCE.getInt(), true, BiomeBank.MUSHROOM_BEACH, BiomeBank.MUSHROOM_ISLANDS),
    };
    
    public TerraformAnimalPopulator(TerraformWorld tw) {
        this.tw = tw;
        if(Version.isAtLeast(17)) {
        	ANIMAL_POPULATORS[0] = new AnimalPopulator(EntityType.valueOf("GOAT"), TConfigOption.ANIMALS_GOAT_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_GOAT_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_GOAT_CHANCE.getInt(), true, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS);
        }
        if(Version.isAtLeast(20.5)) {
            ANIMAL_POPULATORS[1] = new AnimalPopulator(OneTwentyFiveBlockHandler.ARMADILLO, TConfigOption.ANIMALS_ARMADILLO_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_ARMADILLO_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_ARMADILLO_CHANCE.getInt(), true, BiomeBank.SAVANNA, BiomeBank.SHATTERED_SAVANNA, BiomeBank.BADLANDS, BiomeBank.BADLANDS_CANYON);
        }
    }

    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk chunk) {
       
    	PopulatorDataPostGen data = new PopulatorDataPostGen(chunk);

    	for (AnimalPopulator pop : ANIMAL_POPULATORS) {
        	if(pop == null) continue;
            if (pop.canSpawn(tw.getHashedRand(chunk.getX(), pop.hashCode(), chunk.getZ()))) {
                pop.populate(tw, tw.getHashedRand(chunk.getX(), 111+pop.hashCode(), chunk.getZ()), data);
            }
        }
    }
}
