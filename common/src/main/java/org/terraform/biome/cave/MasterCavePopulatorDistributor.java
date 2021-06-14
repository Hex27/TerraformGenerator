package org.terraform.biome.cave;

import java.util.Random;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;
import org.terraform.utils.version.Version;

/**
 * This class will distribute ALL cave post-population to the right populators,
 * as well as handle placements for special small clusters like dripzone, lush and
 * deep zones.
 *
 */
public class MasterCavePopulatorDistributor{
	
	double lushClusterChance = TConfigOption.BIOME_CAVE_LUSHCLUSTER_FREQUENCY.getDouble();
	double dripstoneClusterChance = TConfigOption.BIOME_CAVE_DRIPSTONECLUSTER_FREQUENCY.getDouble();
	
	public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                
            	BiomeBank bank = tw.getBiomeBank(x, z);
                for (int[] pair : GenUtils.getCaveCeilFloors(data, x, z)) {
                	
                	if(pair[0] - pair[1] <= 3) //Ceiling too short.
                		continue;
                	
                    SimpleBlock ceil = new SimpleBlock(data,x,pair[0],z); //non-solid
                    SimpleBlock floor = new SimpleBlock(data,x,pair[1],z); //solid
                    
                    //Don't populate inside amethysts
                    if(Version.isAtLeast(17) 
                    		&& (floor.getType() == OneOneSevenBlockHandler.AMETHYST_BLOCK
                    		|| floor.getType() == OneOneSevenBlockHandler.AMETHYST_CLUSTER)) {
                    	continue;
                    }
                    
                    AbstractCavePopulator pop;
                    if(floor.getY() < 15)
                    	/**
                    	 * Deep cave floors will use the deep cave populator.
                    	 * This has to happen, as most surfaces
                    	 * too low down will be lava. Hard to decorate.
                    	 */
                    	pop = new DeepCavePopulator();
                    else 
                    {
                    	/**
                    	 * Cluster Populators won't just decorate one block, they
                    	 * will populate the surrounding surfaces in a fuzzy 
                    	 * radius.
                    	 */
                    	
                    	if(GenUtils.chance(random, (int) (lushClusterChance*100001.0), 100001)) {
                            TerraformGeneratorPlugin.logger.info("Spawning lush cluster at " + floor);
                    		pop = new LushClusterCavePopulator();
                    	}
                    	else if(GenUtils.chance(random, (int) (dripstoneClusterChance*100000.0), 100000)) {
                            TerraformGeneratorPlugin.logger.info("Spawning dripstone cluster at " + floor);
                    		pop = new DripstoneClusterCavePopulator();
                    	}
                    	else
                    		//If both clusters don't spawn, then revert to the
                    		//basic biome-based cave populator
                    		pop = bank.getCavePop();
                    }
                    
                    if(pop != null)
                    	pop.populate(tw, random, ceil, floor);
                }
            }
        }
	}
}
