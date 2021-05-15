package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class BadlandsCanyonHandler extends AbstractMountainHandler {
    /**
     * Performs badlands plateau generation for one x/z coord.
     */
    public static void oneUnit(TerraformWorld world, Random random, PopulatorDataAbstract data, int x, int z, boolean force) {
        int highest = GenUtils.getTrueHighestBlock(data, x, z);
        int threshold = 65;
        if (force)
            threshold = highest - GenUtils.randInt(random, 3, 6);
        for (int y = highest; y > threshold; y--) {
            if (data.getBiome(x, z) != Biome.BADLANDS_PLATEAU && !force) {
            	if(data.getBiome(x, z) == Biome.DESERT) {
            		continue;
            	}
            }
            if (data.getType(x, y, z) != Material.RED_SANDSTONE
            		&& data.getType(x, y, z) != Material.SANDSTONE
            		&& data.getType(x, y, z) != Material.RED_SAND
            		&& data.getType(x, y, z) != Material.SAND
            		&& data.getType(x, y, z) != Material.STONE)
                continue;
            
            data.setType(x, y, z, BlockUtils.getTerracotta(y));
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.BADLANDS_PLATEAU;
    }

    //Extra red sandstone padding required: Prevents exposed vertical surfaces.
    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.RED_SAND,
        		Material.RED_SAND,
        		Material.RED_SAND,
        		Material.RED_SAND,
        		Material.RED_SAND,
        		Material.RED_SAND,
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.RED_SAND),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.RED_SAND),
                Material.RED_SANDSTONE,
                Material.RED_SANDSTONE,
                Material.RED_SANDSTONE,
                Material.RED_SANDSTONE,
                Material.RED_SANDSTONE,
                Material.RED_SANDSTONE,
                Material.RED_SANDSTONE,
                Material.RED_SANDSTONE,
                Material.RED_SANDSTONE,
                Material.RED_SANDSTONE,
                Material.RED_SANDSTONE,
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
            	if(GenUtils.getHighestGround(data, x, z) > 
                	10+HeightMap.CORE.getHeight(world, x, z)) {
                	oneUnit(world, random, data, x, z, false);
            	}
            }
        }
    }

    //@Override
    //public BiomeHandler getTransformHandler() {
    //    return BiomeBank.BADLANDS.getHandler();
    //}

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.BADLANDS_BEACH;
	}
    
	/**
	 * Badlands Canyons will have a distorted circle resting in the middle of the
	 * biome section, with sand padding at the sides.
	 */
	@Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
		BiomeSection section = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);
		
		FastNoise distortNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_BADLANDS_PLATEAU_DISTORTEDCIRCLE, (world)->{
	        FastNoise noise = new FastNoise((int) (world.getSeed()/11));
	        noise.SetNoiseType(NoiseType.Simplex);
	        noise.SetFrequency(0.09f);
	        return noise;
		});
		
		SimpleLocation center = section.getCenter();
		
		int relX = x-center.getX();
		int relZ = z-center.getZ();
		
		double plateauRadius = (double) (BiomeSection.sectionWidth/2.7f);
		double sandRadius = (double) (BiomeSection.sectionWidth/2.4f);
        double plateauEquationResult = Math.pow(relX, 2) / Math.pow(plateauRadius, 2)
                + Math.pow(relZ, 2) / Math.pow(plateauRadius, 2);
        double sandEquationResult = Math.pow(relX, 2) / Math.pow(sandRadius, 2)
                + Math.pow(relZ, 2) / Math.pow(sandRadius, 2);
        
        double noiseVal = distortNoise.GetNoise(x, z);
        
        double raisedHeight = 0;
        
        if (sandEquationResult <= 1 + 0.7 * noiseVal) {
        	if (plateauEquationResult <= 1 + 0.7 * noiseVal) {
                //Aggressively raise land into a plateau
        		raisedHeight = 40.0;
            }else {
            	//Pad sides with shallow gradients
            	
            	//min 0.
            	double diff = (1 + 0.7 * noiseVal) - sandEquationResult;
            	if(diff > 1) diff = 1.0;
            	raisedHeight += 10.0 * diff;
            }
        }
		
		double riverHeight = HeightMap.getRawRiverDepth(tw, x, z);
        double height = HeightMap.CORE.getHeight(tw, x, z) + riverHeight + raisedHeight;
        
        return height;
    }
}
