package org.terraform.biome.mountainous;

import java.util.Random;

import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.BiomeSection;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.HeightMap;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

public abstract class AbstractMountainHandler extends BiomeHandler {

	protected double getPeakMultiplier(Random sectionRandom) {
		return GenUtils.randDouble(sectionRandom, 1.4, 1.7);
	}
	
	/**
	 * Mountain height calculation works by identifying an offset from the BiomeSection
	 * midpoint, then multiplying current height to peak at that offset.
	 * <br><br>
	 * This may lead to weird areas where blurring takes over, as the peak of the
	 * mountain is too close to the side, or a part of the mountain is too high
	 */
	@Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
    	
        double height = HeightMap.CORE.getHeight(tw, x, z);//HeightMap.MOUNTAINOUS.getHeight(tw, x, z); //Added here
        
        //Let mountains cut into adjacent sections.
        double maxMountainRadius = ((double) BiomeSection.sectionWidth);
        //Double attrition height
        height += HeightMap.ATTRITION.getHeight(tw, x, z);
        
        BiomeSection sect = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);
        if(sect.getBiomeBank().getType() != BiomeType.MOUNTAINOUS) {
        	sect = BiomeSection.getMostDominantSection(tw, x, z);
        }
        
        Random sectionRand = sect.getSectionRandom();
        double maxPeak = getPeakMultiplier(sectionRand);
        
        //Let's just not offset the peak. This seems to give a better result.
        SimpleLocation mountainPeak = sect.getCenter();
        		//.getRelative(
        		//GenUtils.randInt(sectionRand,-BiomeSection.sectionWidth/3,BiomeSection.sectionWidth/3),
        		//0,
        		//GenUtils.randInt(sectionRand,-BiomeSection.sectionWidth/3,BiomeSection.sectionWidth/3)
        		//);
        
        //SimpleLocation sectionCenter = sect.getCenter();
        
        double distFromPeak = (1.42*maxMountainRadius)-Math.sqrt(
        		Math.pow(x-mountainPeak.getX(), 2)+Math.pow(z-mountainPeak.getZ(), 2)
        		);

        //double distFromCenter = (maxSectRadius)-Math.sqrt(
        //		Math.pow(x-sectionCenter.getX(), 2)+Math.pow(z-sectionCenter.getZ(), 2)
        //		);
        //if(distFromCenter < 0) distFromCenter = 0;
        
        double heightMultiplier = maxPeak*(distFromPeak/maxMountainRadius);
        
        if(heightMultiplier < 1) heightMultiplier = 1;
        
       // heightMultiplier = heightMultiplier*(distFromCenter/maxSectRadius/2);
        
        height = height*heightMultiplier;
        
        //If the height is too high, just force it to smooth out
        if (height > 200) height = 200 + (height - 200) * 0.5;
        if (height > 230) height = 230 + (height - 230) * 0.3;
        if (height > 240) height = 240 + (height - 240) * 0.1;
        if (height > 250) height = 250 + (height - 250) * 0.05;
        
        return height;
    }
	
	
}
