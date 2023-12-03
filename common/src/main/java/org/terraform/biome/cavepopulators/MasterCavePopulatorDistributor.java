package org.terraform.biome.cavepopulators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
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
	
	private static ArrayList<Class<?>> populatedBefore = new ArrayList<>();
	public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		HashMap<SimpleLocation, CaveClusterRegistry> clusters = calculateClusterLocations(random, tw, data.getChunkX(), data.getChunkZ());
		
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                
            	BiomeBank bank = tw.getBiomeBank(x, z);
            	int maxHeightForCaves = bank.getHandler().getMaxHeightForCaves(tw, x, z);
                for (int[] pair : GenUtils.getCaveCeilFloors(data, x, z)) {
                	
                	//Biome disallows caves above this height
                	if(pair[0] > maxHeightForCaves) continue;
                	
                	if(pair[0] - pair[1] <= 3) //Ceiling too short.
                		continue;
                	
                    SimpleBlock ceil = new SimpleBlock(data,x,pair[0],z); //non-solid
                    SimpleBlock floor = new SimpleBlock(data,x,pair[1],z); //solid
                    
                    //Don't populate inside amethysts
                    if(Version.isAtLeast(17) 
                    		&& (floor.getType() == OneOneSevenBlockHandler.AMETHYST_BLOCK
                    		|| floor.getType() == OneOneSevenBlockHandler.AMETHYST_CLUSTER
                    		|| ceil.getType() == OneOneSevenBlockHandler.AMETHYST_BLOCK
                    		|| ceil.getType() == OneOneSevenBlockHandler.AMETHYST_CLUSTER)) {
                    	continue;
                    }
                    
                    AbstractCavePopulator pop = null;
                    if(floor.getY() < TerraformGeneratorPlugin.injector.getMinY() + 32)
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
                    	
                    	for(SimpleLocation loc:clusters.keySet()) {
                    		if(loc.getX() == x && loc.getZ() == z) {
                    			pop = clusters.get(loc).getPopulator(random);
                    		}
                    	}
                    	
                		//If both clusters don't spawn, then revert to the
                		//basic biome-based cave populator
                    	if(pop == null)
                    		pop = bank.getCavePop();
                    }
                    
                    if(pop != null) {
                    	pop.populate(tw, random, ceil, floor);
                    	
                    	//Locating and debug print
            			if(!populatedBefore.contains(pop.getClass())) {
            				populatedBefore.add(pop.getClass());
            	            TerraformGeneratorPlugin.logger.info("Spawning " + pop.getClass().getSimpleName() + " at " + floor);
            			}
                    }
                }
            }
        }
	}
	
	private HashMap<SimpleLocation, CaveClusterRegistry> calculateClusterLocations(Random rand, TerraformWorld tw, int chunkX, int chunkZ){
		HashMap<SimpleLocation, CaveClusterRegistry> locs = new HashMap<>();
		
		for(CaveClusterRegistry type:CaveClusterRegistry.values()) {
			SimpleLocation[] positions =  GenUtils.randomObjectPositions(
	    			tw.getHashedRand(type.getHashSeed(), chunkX, chunkZ).nextInt(99999), 
	    			chunkX, 
	    			chunkZ, 
	    			type.getSeparation(), 
	    			type.getPertub());
			for(SimpleLocation pos:positions) {
				if(locs.containsKey(pos))
					//give a chance to replace the old one
					if(rand.nextBoolean()) continue; 
				
				locs.put(pos, type);
			}
			
		}
		
		return locs;
	}
}
