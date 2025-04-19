package org.terraform.populators;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class AnimalPopulator {
    private final EntityType animalType;
    private final int chance;
    private final int minNum;
    private final int maxNum;
    private BiomeBank[] whitelistedBiomes;
    private BiomeBank[] blacklistedBiomes;
    private boolean isAquatic = false;

    public AnimalPopulator(EntityType animalType,
                           int minNum,
                           int maxNum,
                           int chance,
                           boolean useWhitelist,
                           BiomeBank... biomes)
    {
        this.animalType = animalType;
        this.chance = chance;
        if (useWhitelist) {
            this.whitelistedBiomes = biomes;
        }
        else {
            this.blacklistedBiomes = biomes;
        }

        this.minNum = minNum;
        this.maxNum = maxNum;
    }

    // Changed to now only roll chances. TerraformAnimalPopulator will no longer
    // call GenUtils.getBiomesInChunk, which polls biomes for 256 blocks each.
    // Instead, each query will just call getBiome per block. This sounds more
    // intensive, but it relies on the getBiome cache system to be faster.
    public boolean canSpawn(@NotNull Random rand) {
        return TConfig.areAnimalsEnabled() && !GenUtils.chance(rand, 100 - chance, 100);
    }

    private boolean canSpawnInBiome(BiomeBank b) {
        if (!TConfig.areAnimalsEnabled()) {
            return false;
        }

        if (whitelistedBiomes != null) {
            for (BiomeBank entr : whitelistedBiomes) {
                if (entr == b) {
                    return true;
                }
            }
            return false;
        }
        if (blacklistedBiomes != null) {
            for (BiomeBank entr : blacklistedBiomes) {
                if (entr == b) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void populate(@NotNull TerraformWorld world, @NotNull Random random, @NotNull PopulatorDataAbstract data) {
        if (!TConfig.areAnimalsEnabled()) {
            return;
        }

        for (int i = 0; i < GenUtils.randInt(random, minNum, maxNum); i++) {
            int x = (data.getChunkX() << 4) + GenUtils.randInt(random, 5, 7);
            int z = (data.getChunkZ() << 4) + GenUtils.randInt(random, 5, 7);

            // To account for solid ground that spawns above the noisemap (i.e. boulders/black spikes)
            int height = GenUtils.getTransformedHeight(world,x,z)+1;

            //Fallback to highest ground if it is solid
            //This may lead to animals inside houses, but too bad i guess
            if(data.getType(x,height,z).isSolid()){
                height = GenUtils.getHighestGround(data, x, z) + 1;
            }

            if (canSpawnInBiome(world.getBiomeBank(x, z))) {
                if (!this.isAquatic && height > TerraformGenerator.seaLevel) {
                    if (!data.getType(x, height, z).isSolid()) // Don't spawn in blocks
                    {
                        data.addEntity(x, height, z, animalType);
                    }
                }
                else if (this.isAquatic && height <= TerraformGenerator.seaLevel) {
                    if (data.getType(x, height, z) == Material.WATER) // Don't spawn in anything but water
                    {
                        data.addEntity(x, height, z, animalType);
                    }
                }
            }
        }
    }

    public @NotNull AnimalPopulator setAquatic(boolean aquatic) {
        this.isAquatic = aquatic;
        return this;
    }
}
