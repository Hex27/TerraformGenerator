package org.terraform.populators;

import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Collection;
import java.util.Random;

public class AnimalPopulator {
    private final EntityType animalType;
    private final int chance;
    private final int minNum;
    private final int maxNum;
    private BiomeBank[] whitelistedBiomes;
    private BiomeBank[] blacklistedBiomes;

    public AnimalPopulator(EntityType animalType, int minNum, int maxNum, int chance, boolean useWhitelist, BiomeBank... biomes) {
        this.animalType = animalType;
        this.chance = chance;
        if (useWhitelist) {
            this.whitelistedBiomes = biomes;
        } else this.blacklistedBiomes = biomes;

        this.minNum = minNum;
        this.maxNum = maxNum;
    }

    public boolean canSpawn(Collection<BiomeBank> b, Random rand) {
        //TerraformGeneratorPlugin.logger.info("Can-spawn for - " + animalType.toString());
        if (GenUtils.chance(rand, 100 - chance, 100))
            return false;
        //TerraformGeneratorPlugin.logger.info("Pass chance");
        if (whitelistedBiomes != null) {
            for (BiomeBank entr : whitelistedBiomes) {
                if (b.contains(entr)) return true;
            }
            return false;
        }
        if (blacklistedBiomes != null) {
            for (BiomeBank entr : blacklistedBiomes) {
                if (b.contains(entr)) return false;
            }
            return true;
        }
        //TerraformGeneratorPlugin.logger.info("Failed.");
        return false;
    }

    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {

        for (int i = 0; i < GenUtils.randInt(random, minNum, maxNum); i++) {
            int x = (data.getChunkX() << 4) + GenUtils.randInt(random, 5, 7);
            int z = (data.getChunkZ() << 4) + GenUtils.randInt(random, 5, 7);
            int height = HeightMap.getBlockHeight(world, x, z) + 2;//GenUtils.getHighestGround(data, x, z)+1;
            //TerraformGeneratorPlugin.logger.info("Spawned " + animalType.toString() + " at " + x + "," + height + "," + z);
            data.addEntity(x, height, z, animalType);
        }
        //TerraformGeneratorPlugin.logger.debug("animal populator - finished.");
    }

    /**
     * @return the animalType
     */
    public EntityType getAnimalType() {
        return animalType;
    }
}
