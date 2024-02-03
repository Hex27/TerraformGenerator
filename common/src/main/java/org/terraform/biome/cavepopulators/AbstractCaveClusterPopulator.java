package org.terraform.biome.cavepopulators;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.Version;

public abstract class AbstractCaveClusterPopulator extends AbstractCavePopulator {

	private final float radius;
    //Starts null, but will be populated by the time oneUnit is called.
    protected SimpleBlock center;
    protected SimpleBlock lowestYCenter;
	public AbstractCaveClusterPopulator(float radius) {
		this.radius = radius;
	}
	
	protected abstract void oneUnit(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor, boolean isBoundary);
	@Override
    public void populate(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor) {

		ArrayList<SimpleBlock[]> ceilFloorPairs = new ArrayList<>();
        ArrayList<Boolean> boundaries = new ArrayList<>();
        
        FastNoise circleNoise = NoiseCacheHandler.getNoise(
                tw,
                NoiseCacheEntry.BIOME_CAVECLUSTER_CIRCLENOISE,
                world -> {
                    FastNoise n = new FastNoise((int) (world.getSeed() * 11));
                    n.SetNoiseType(FastNoise.NoiseType.Simplex);
                    n.SetFrequency(0.09f);

                    return n;
                });
        
        center = new SimpleBlock(ceil.getPopData(), ceil.getX(), (ceil.getY() + floor.getY())/2, ceil.getZ());
        int lowest = center.getY();
        for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
                SimpleBlock rel = center.getRelative(Math.round(x), 0, Math.round(z));
                
                //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                double equationResult = Math.pow(x, 2) / Math.pow(radius, 2)
                        + Math.pow(z, 2) / Math.pow(radius, 2);
                double noiseVal = circleNoise.GetNoise(rel.getX(), rel.getZ());
                if (equationResult <= 1 + 0.7*noiseVal) {
                	Wall candidateFloorWall = new Wall(rel).findStonelikeFloor(60);
                	Wall candidateCeilWall = new Wall(rel).findStonelikeCeiling(60);
                	if(candidateFloorWall != null && candidateCeilWall != null) {
                		
                		
                		SimpleBlock candidateCeil = candidateCeilWall.get();
                		SimpleBlock candidateFloor = candidateFloorWall.get();

                        //If this is wet, don't touch it.
                        //Don't populate inside amethysts
                        if(BlockUtils.amethysts.contains(floor.getType())
                                || BlockUtils.fluids.contains(floor.getUp().getType())
                                || BlockUtils.amethysts.contains(ceil.getDown().getType())) {
                            continue;
                        }
                		
                		//Ensure that this is not already dripstone or moss
                		if((candidateFloor.getType() == Material.MOSS_BLOCK
                        		|| candidateFloor.getType() == Material.DRIPSTONE_BLOCK)) {
                        	continue;
                        }
                		
	                	if(!candidateFloor.getUp().getType().isSolid()) {
	                		if(!candidateCeil.getDown().getType().isSolid()) {
	                			if(candidateCeil.getY() - 1 > candidateFloor.getY() + 1) {
	                        		ceilFloorPairs.add(new SimpleBlock[] {
	                        				candidateCeil,
	                        				candidateFloor
	                        		});
                                    boundaries.add(equationResult > 0.7 + 0.7*noiseVal);
                                    lowest = Math.min(lowest,candidateFloor.getY());
	                        	}
	                    	}
	                	}
                		
                	}
                }
            }
        }
        lowestYCenter = center.getAtY(lowest);
        for(int i = 0; i < ceilFloorPairs.size(); i++) {
            SimpleBlock[] candidates = ceilFloorPairs.get(i);

            //Late fluid checks
            if(BlockUtils.fluids.contains(candidates[1].getAtY(lowest+1).getType()))
                continue;
        	oneUnit(tw, random, candidates[0], candidates[1], boundaries.get(i));
        }
    }
}
