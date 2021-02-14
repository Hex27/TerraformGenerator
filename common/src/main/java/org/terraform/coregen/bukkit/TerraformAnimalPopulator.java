package org.terraform.coregen.bukkit;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.populators.AnimalPopulator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class TerraformAnimalPopulator extends BlockPopulator {

    private final TerraformWorld tw;

    private static final AnimalPopulator[] ANIMAL_POPULATORS = {
            new AnimalPopulator(EntityType.PIG, TConfigOption.ANIMALS_PIG_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_PIG_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_PIG_CHANCE.getInt(), false, BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_MOUNTAINS),
            
            new AnimalPopulator(EntityType.COW, TConfigOption.ANIMALS_COW_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_COW_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_COW_CHANCE.getInt(), false, BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_MOUNTAINS),
            
            new AnimalPopulator(EntityType.SHEEP, TConfigOption.ANIMALS_SHEEP_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_SHEEP_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_SHEEP_CHANCE.getInt(), false, BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_MOUNTAINS),
            
            new AnimalPopulator(EntityType.CHICKEN, TConfigOption.ANIMALS_CHICKEN_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_CHICKEN_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_CHICKEN_CHANCE.getInt(), false, BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_MOUNTAINS),
            
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
                    TConfigOption.ANIMALS_DOLPHIN_CHANCE.getInt(), true, BiomeBank.OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.LUKEWARM_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN)
            .setAquatic(true),
    
            new AnimalPopulator(EntityType.COD, TConfigOption.ANIMALS_COD_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_COD_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_COD_CHANCE.getInt(), true, BiomeBank.OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.LUKEWARM_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.DEEP_COLD_OCEAN)
            .setAquatic(true),
            
            new AnimalPopulator(EntityType.SQUID, TConfigOption.ANIMALS_SQUID_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_SQUID_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_SQUID_CHANCE.getInt(), true, 
                    BiomeBank.FROZEN_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN,
                    BiomeBank.COLD_OCEAN, BiomeBank.DEEP_COLD_OCEAN,
                    BiomeBank.BLACK_OCEAN,BiomeBank.DEEP_BLACK_OCEAN,
                    BiomeBank.OCEAN, BiomeBank.DEEP_OCEAN, 
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, 
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
                    TConfigOption.ANIMALS_PUFFERFISH_CHANCE.getInt(), true, BiomeBank.LUKEWARM_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN)
            .setAquatic(true),
            
            new AnimalPopulator(EntityType.TROPICAL_FISH, TConfigOption.ANIMALS_TROPICALFISH_MINHERDSIZE.getInt(), TConfigOption.ANIMALS_TROPICALFISH_MAXHERDSIZE.getInt(),
                    TConfigOption.ANIMALS_TROPICALFISH_CHANCE.getInt(), true, BiomeBank.LUKEWARM_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN)
            .setAquatic(true),
    };
    
    public TerraformAnimalPopulator(TerraformWorld tw) {
        this.tw = tw;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
       
    	PopulatorDataPostGen data = new PopulatorDataPostGen(chunk);
        ArrayList<BiomeBank> banks = GenUtils.getBiomesInChunk(tw, chunk.getX(),chunk.getZ());

        Set<EntityType> spawned = EnumSet.noneOf(EntityType.class);
        //TerraformGeneratorPlugin.logger.debug("animal-populator eval for " + data.getChunkX() + "," + data.getChunkZ());
        for (AnimalPopulator pop : ANIMAL_POPULATORS) {
            if (pop.canSpawn(banks, random) && spawned.add(pop.getAnimalType())) {
                //TerraformGeneratorPlugin.logger.debug("animal populator proc");
                pop.populate(tw, random, data);
            }
        }
        spawned.clear();
    }
}
