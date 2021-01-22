package org.terraform.populators;

import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class OrePopulator {

    private final Material type;
    private final int baseChance;
    private final int maxOreSize;
    private final int maxNumberOfVeins;
    private final int maxRange;
    private final int rareMaxRange;
    private int minRange = 5;

    public OrePopulator(Material type, int baseChance, int maxOreSize,
                        int maxNumberOfVeins, int maxRange, int rareMaxRange) {
        this.type = type;
        this.baseChance = baseChance;
        this.maxOreSize = maxOreSize;
        this.maxNumberOfVeins = maxNumberOfVeins;
        this.maxRange = maxRange;
        this.rareMaxRange = rareMaxRange;
    }

    public OrePopulator(Material type, int baseChance, int maxOreSize,
                        int maxNumberOfVeins, int minRange, int maxRange, int rareMaxRange) {
        this.type = type;
        this.baseChance = baseChance;
        this.maxOreSize = maxOreSize;
        this.maxNumberOfVeins = maxNumberOfVeins;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.rareMaxRange = rareMaxRange;
    }

    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        //TickTimer timer = new TickTimer("Ore-generation");
        int x, y, z;
        for(int i = 0; i < this.maxNumberOfVeins; i++) {
            // Number of veins

            // The chance of spawning a vein
            if(GenUtils.chance(random, this.baseChance, 100)) {
                x = GenUtils.randInt(random, 0, 15) + data.getChunkX() * 16;
                z = GenUtils.randInt(random, 0, 15) + data.getChunkZ() * 16;

                int range = maxRange;
                if(GenUtils.chance(random, 1, 50)) range = rareMaxRange;

                y = GenUtils.randInt(random, minRange, range);  // Get randomized coordinates

                for(int s = 0; s < maxOreSize; s++) {
                    Material type = data.getType(x, y, z);
                    if(type != Material.STONE) break;

                    data.setType(x, y, z, this.type);

                    switch(random.nextInt(5)) {  // The direction chooser
                        case 0:
                            x++;
                            break;
                        case 1:
                            y++;
                            break;
                        case 2:
                            z++;
                            break;
                        case 3:
                            x--;
                            break;
                        case 4:
                            y--;
                            break;
                        case 5:
                            z--;
                            break;
                    }
                }
            }
        }
        //timer.finish();
    }


}
