package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.OneOneEightBlockHandler;

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
            if (data.getBiome(x, z) != OneOneEightBlockHandler.ERODED_BADLANDS && !force) {
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
        return OneOneEightBlockHandler.ERODED_BADLANDS;
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
            	int y = GenUtils.getHighestGround(data, x, z);
            	if(data.getBiome(x,z) != getBiome())
            		continue;
            	if(y > 
                	10+HeightMap.CORE.getHeight(world, x, z)) {
                	oneUnit(world, random, data, x, z, false);
            	}
            	if(HeightMap.getTrueHeightGradient(data, x, z, 2) < 2) {
            		data.setType(x, y, z, Material.RED_SAND);
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
	 * Badlands Canyons will use the mountain algorithm, then forcefully
	 * smooth out at a set Y level
	 */
	@Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
		double baseHeight = HeightMap.CORE.getHeight(tw, x, z);
		FastNoise duneNoise = NoiseCacheHandler.getNoise(
		    		tw, 
		    		NoiseCacheEntry.BIOME_BADLANDS_CANYON_NOISE, 
		    		world -> {
		    	    	FastNoise n = new FastNoise((int) world.getSeed());
		    	        n.SetNoiseType(NoiseType.Simplex);
		    	        n.SetFractalOctaves(3);
		    	        n.SetFrequency(0.02f);
		    	        return n;
		    		});
		double noise = duneNoise.GetNoise(x, z);
		if(noise < 0) noise = 0;

    	
        double height = HeightMap.CORE.getHeight(tw, x, z);//HeightMap.MOUNTAINOUS.getHeight(tw, x, z); //Added here
        
        //Let mountains cut into adjacent sections.
        double maxMountainRadius = ((double) BiomeSection.sectionWidth);
        //Add dune height
        height += noise*20;
        
        BiomeSection sect = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);
        if(sect.getBiomeBank() != BiomeBank.BADLANDS_CANYON
        		|| sect.getBiomeBank() != BiomeBank.BADLANDS_CANYON_PEAK) {
        	sect = BiomeSection.getMostDominantSection(tw, x, z);
        }
        
        Random sectionRand = sect.getSectionRandom();
        double maxPeak = getPeakMultiplier(sect, sectionRand);
        
        //Let's just not offset the peak. This seems to give a better result.
        SimpleLocation mountainPeak = sect.getCenter();
        
        double angleFromPeak = new SimpleLocation(x, 0, z).twoDAngleTo(mountainPeak);
        double circleFuzz = 1.32 + Math.abs(duneNoise.GetValue((float) (10*angleFromPeak), 40519*mountainPeak.getX() + 75721*mountainPeak.getZ()));//Math.min(0.5, 0.5*duneNoise.GetValue((float) angleFromPeak, 40519*mountainPeak.getX() + 75721*mountainPeak.getZ()));
        
        
        double distFromPeak = (circleFuzz*maxMountainRadius)-Math.sqrt(
        		Math.pow(x-mountainPeak.getX(), 2)+Math.pow(z-mountainPeak.getZ(), 2)
        		);
        
        
        
        double heightMultiplier = maxPeak*(distFromPeak/maxMountainRadius);
        double minMultiplier = 1;
        if(heightMultiplier < minMultiplier) heightMultiplier = minMultiplier;
        
        height = height*heightMultiplier;
        
		//Thresholds to make separate plateau-looking bits
		if(height > 75) {
			if(height < 80)
				height = 80;
			if(height < 90)
				height = 90;
			else if(height < 105)
				height = 105;
			else if(height < 120)
				height = 120;
			else
				height = 135;
		}
		else height = baseHeight;
		
        return height;
    }
	
	@Override
	protected double getPeakMultiplier(BiomeSection section, Random sectionRandom)
	{
		return super.getPeakMultiplier(section, sectionRandom)*0.9;
	}
}
