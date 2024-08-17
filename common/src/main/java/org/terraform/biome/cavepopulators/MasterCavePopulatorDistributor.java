package org.terraform.biome.cavepopulators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

/**
 * This class will distribute ALL cave post-population to the right populators,
 * as well as handle placements for special small clusters like dripzone, lush and
 * deep zones.
 *
 */
public class MasterCavePopulatorDistributor{

	private static final ArrayList<Class<?>> populatedBefore = new ArrayList<>();
	public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		HashMap<SimpleLocation, CaveClusterRegistry> clusters = calculateClusterLocations(random, tw, data.getChunkX(), data.getChunkZ());
		
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                
            	BiomeBank bank = tw.getBiomeBank(x, z);
            	int maxHeightForCaves = bank.getHandler().getMaxHeightForCaves(tw, x, z);

                //Remove clusters when they're spawned.
                CaveClusterRegistry reg = clusters.remove(new SimpleLocation(x,0,z));

                Collection<int[]> pairs = GenUtils.getCaveCeilFloors(data, x, z, 4);

                //This is the index to spawn the cluster in.
                int clusterPair = pairs.size() > 0 ? random.nextInt(pairs.size()) : 0;

                for (int[] pair : pairs) {
                	
                	//Biome disallows caves above this height
                	if(pair[0] > maxHeightForCaves) continue;

                    SimpleBlock ceil = new SimpleBlock(data,x,pair[0],z); //non-solid
                    SimpleBlock floor = new SimpleBlock(data,x,pair[1],z); //solid

                    //If this is wet, don't touch it.
                    //Don't populate inside amethysts
                    if(BlockUtils.amethysts.contains(floor.getType())
                        || BlockUtils.fluids.contains(floor.getUp().getType())
                        || BlockUtils.amethysts.contains(ceil.getDown().getType())) {
                        continue;
                    }

                    AbstractCavePopulator pop;

                    /*
                     * Deep cave floors will use the deep cave populator.
                     * This has to happen, as most surfaces
                     * too low down will be lava. Hard to decorate.
                     */
                    if(floor.getY() < TerraformGeneratorPlugin.injector.getMinY() + 32)
                    	pop = new DeepCavePopulator();
                    else 
                    {
                        /*
                         * Cluster Populators won't just decorate one block, they
                         * will populate the surrounding surfaces in a fuzzy
                         * radius.
                         */
                        //If there is no cluster to spawn, then revert to the
                        //basic biome-based cave populator
                        pop = (clusterPair == 0 && reg != null) ? reg.getPopulator(random) : bank.getCavePop();
                    }
                    clusterPair--;

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
	
	private HashMap<SimpleLocation, CaveClusterRegistry> calculateClusterLocations(Random rand, TerraformWorld tw, int chunkX, int chunkZ){
		HashMap<SimpleLocation, CaveClusterRegistry> locs = new HashMap<>();
		
		for(CaveClusterRegistry type:CaveClusterRegistry.values()) {
			SimpleLocation[] positions =  GenUtils.randomObjectPositions(
	    			tw.getHashedRand(chunkX, type.getHashSeed(), chunkZ).nextInt(9999999),
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
