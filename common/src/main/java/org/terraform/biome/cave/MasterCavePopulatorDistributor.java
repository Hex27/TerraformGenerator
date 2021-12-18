package org.terraform.biome.cave;

import java.util.Random;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
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
	
	//double lushClusterChance = TConfigOption.BIOME_CAVE_LUSHCLUSTER_FREQUENCY.getDouble();
	//double dripstoneClusterChance = TConfigOption.BIOME_CAVE_DRIPSTONECLUSTER_FREQUENCY.getDouble();
	int lushClusterSeparation = TConfigOption.BIOME_CAVE_LUSHCLUSTER_SEPARATION.getInt();
	int dripstoneClusterSeparation = TConfigOption.BIOME_CAVE_DRIPSTONECLUSTER_SEPARATION.getInt();
	float lushClusterPertub = TConfigOption.BIOME_CAVE_LUSHCLUSTER_MAXPERTUB.getFloat();
	float dripstoneClusterPertub = TConfigOption.BIOME_CAVE_DRIPSTONECLUSTER_MAXPERTUB.getFloat();
	
	public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		SimpleLocation[] lushLocs = GenUtils.randomObjectPositions(
    			tw.getHashedRand(9527213, data.getChunkX(), data.getChunkZ()).nextInt(99999), 
    			data.getChunkX(), 
    			data.getChunkZ(), 
    			lushClusterSeparation, 
    			lushClusterPertub);
		SimpleLocation[] dripstoneLocs = GenUtils.randomObjectPositions(
    			tw.getHashedRand(5902907, data.getChunkX(), data.getChunkZ()).nextInt(99999), 
    			data.getChunkX(), 
    			data.getChunkZ(), 
    			dripstoneClusterSeparation, 
    			dripstoneClusterPertub);
		
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
                    	
                    	for(SimpleLocation loc:lushLocs) {
                    		if(loc.getX() == x && loc.getZ() == z)
                    			pop = new LushClusterCavePopulator(
                    					GenUtils.randInt(random, 
                    							TConfigOption.BIOME_CAVE_LUSHCLUSTER_MINSIZE.getInt(), 
                    							TConfigOption.BIOME_CAVE_LUSHCLUSTER_MAXSIZE.getInt()), 
                    					false); //False to prevent Azalea Trees from spawning.
                    	}
                    	
                    	if(pop == null)
                    	for(SimpleLocation loc:dripstoneLocs) {
                    		if(loc.getX() == x && loc.getZ() == z)
                    			pop = new DripstoneClusterCavePopulator(
                    					GenUtils.randInt(random, 
                    							TConfigOption.BIOME_CAVE_DRIPSTONECLUSTER_MINSIZE.getInt(), 
                    							TConfigOption.BIOME_CAVE_DRIPSTONECLUSTER_MAXSIZE.getInt()));
                    	}
                    	
                		//If both clusters don't spawn, then revert to the
                		//basic biome-based cave populator
                    	if(pop == null)
                    		pop = bank.getCavePop();
                    }
                    
                    if(pop != null)
                    	pop.populate(tw, random, ceil, floor);
                }
            }
        }
	}
}
