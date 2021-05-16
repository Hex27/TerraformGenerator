package org.terraform.populators;

import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.Objects;
import java.util.Random;

public class OrePopulator {

    private final Material type;
    private final int baseChance; //Chance to spawn per attempt to spawn
    private final int maxOreSize; //Maximum size of one vein
    private final int minOreSize;
    private final int maxNumberOfVeins; //Maximum number of veins per chunk
    private final int maxRange; //max y height where ore can be commonly found/
    private final int rareMaxRange; //max y height where ore can be rarely found
    private int minRange = 5; //min spawn height

    public OrePopulator(Material type, int baseChance, int maxOreSize,
                        int maxNumberOfVeins, int maxRange, int rareMaxRange) {
        this.type = type;
        this.baseChance = baseChance;
        this.maxOreSize = maxOreSize;
        this.minOreSize = maxOreSize/2;
        this.maxNumberOfVeins = maxNumberOfVeins;
        this.maxRange = maxRange;
        this.rareMaxRange = rareMaxRange;
    }

    public OrePopulator(Material type, int baseChance, int maxOreSize,
                        int maxNumberOfVeins, int minRange, int maxRange, int rareMaxRange) {
        this.type = type;
        this.baseChance = baseChance;
        this.maxOreSize = maxOreSize;
        this.minOreSize = maxOreSize/2;
        this.maxNumberOfVeins = maxNumberOfVeins;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.rareMaxRange = rareMaxRange;
    }
    
    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
    	//Attempt maxNumberOfVeins number of times
    	for (int i = 0; i < this.maxNumberOfVeins; i++) {
    		
    		//Roll chance to spawn one ore
    		if (GenUtils.chance(random, this.baseChance, 100)) {
    			//RNG determined X Y and Z within chunk
    			int x = GenUtils.randInt(random, 0, 15) + data.getChunkX() * 16;
            	int z = GenUtils.randInt(random, 0, 15) + data.getChunkZ() * 16;
            	int groundHeight = GenUtils.getHighestGround(data, x, z);
            	int range = maxRange;
            	
            	//Rare chance for ores to spawn above max range
            	if (GenUtils.chance(random, 1, 30)) range = rareMaxRange;
            	
            	//Range cannot be above ground
            	if(range > groundHeight) range = groundHeight;
            	
            	//Spawn failed.
            	if(minRange > range) continue;
            	
            	int y = GenUtils.randInt(random, minRange, range);
            	
            	//Generate ore with rough sphere size.
            	placeOre(
            			Objects.hash(world.getSeed(),x,y,z), 
            			new SimpleBlock(data, x, y, z));
            	
    		}
    	}
    }

    public void placeOre(int seed, SimpleBlock block) {
    	double size = GenUtils.randDouble(new Random(seed), minOreSize, maxOreSize);
    	//Size is the volume of the sphere, so radius is:
    	double radius = Math.pow(((3.0/4.0)*size*(1.0/Math.PI)), 1.0/3.0);
    	
        if (radius <= 0 && radius <= 0 && radius <= 0) return;
        if (radius <= 0.5 && radius <= 0.5 && radius <= 0.5) {
            //block.setReplaceType(ReplaceType.ALL);
            block.setType(GenUtils.randMaterial(new Random(seed), type));
            return;
        }
        
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (double x = -radius; x <= radius; x++) {
            for (double y = -radius; y <= radius; y++) {
                for (double z = -radius; z <= radius; z++) {
                    SimpleBlock rel = block.getRelative((int)Math.round(x), (int)Math.round(y), (int)Math.round(z));
                    double equationResult = Math.pow(x, 2) / Math.pow(radius,2)
                            + Math.pow(y, 2) / Math.pow(radius, 2)
                            + Math.pow(z, 2) / Math.pow(radius, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        if (rel.getType() == Material.STONE) {
                            rel.setType(type);
                        }
                    }
                }
            }
        }
    }
}
