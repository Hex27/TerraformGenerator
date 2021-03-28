package org.terraform.biome;

import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;

public class BiomeBlender {
    private final int mountainHeight = TConfigOption.BIOME_MOUNTAIN_HEIGHT.getInt();
    private final TerraformWorld tw;
    double biomeThreshold = 0.25;
    boolean blendBiomeGrid;
    int riverThreshold = 5;
    boolean blendWater;
    int mountainThreshold = 5;
    boolean blendMountains;
    boolean blendBeachesToo = true;

    public BiomeBlender(TerraformWorld tw, boolean blendBiomeGrid, boolean blendWater, boolean blendMountains) {
        this.tw = tw;

        this.blendBiomeGrid = blendBiomeGrid;
        this.blendWater = blendWater;
        this.blendMountains = blendMountains;
    }

    public BiomeBlender(TerraformWorld tw) {
        this(tw, true, true, true);
    }

    /**
     * A value between 1 and 0 that gets closer to 0
     * when moving closer to the biome edge, mountain or water.
     * Checking each of those elements can be individually
     * disabled or enabled. Threshold values defined in the object
     * can control how quickly blending happens. Blending is linear.
     */
    public double getEdgeFactor(BiomeBank currentBiome, int x, int z) {
        return getEdgeFactor(currentBiome, x, z, blendBeachesToo ? HeightMap.RIVER.getHeight(tw, x, z) : 0);
    }

    /**
     * @param riverDepth Current river depth, has to have also negative values
     * @see BiomeBlender#getEdgeFactor(BiomeBank, int, int)
     */
    public double getEdgeFactor(BiomeBank currentBiome, int x, int z, double riverDepth) {
        double factor = 1;

        if (blendWater) {
            // Linear blending when closer to water

            double riverFactor = blendBeachesToo ? riverDepth / (-riverThreshold) :
                    (HeightMap.getPreciseHeight(tw, x, z) - TerraformGenerator.seaLevel) / riverThreshold;

            if (riverFactor < factor) factor = Math.max(0, riverFactor);
        }

        if (blendMountains) {
            // Linear blending when closer to mountains
            double mountainFactor = ((mountainHeight - 5) - HeightMap.getPreciseHeight(tw, x, z)) / (double) mountainThreshold;
            if (mountainFactor < factor) factor = Math.max(0, mountainFactor);
        }

        if (blendBiomeGrid) {
            // Same here when closer to biome edge
            double gridFactor = getGridEdgeFactor(tw,x,z);//getGridEdgeFactor(BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z),
            		//currentBiome,
                    //BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z).getTemperature(), BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z).getMoisture());
            if (gridFactor < factor) factor = gridFactor;
        }

        return factor;
    }

    /*
        Get edge factor only based on land, ignore rivers
     */
    @Deprecated /*NEEDS UPDATE WITH NEW SYSTEM. TEMPERATURE AND MOISTURE DON'T VARY BY BLOCK COORDS ANYMORE.*/
    public double getGridEdgeFactor(TerraformWorld tw, int x, int z) {
    	BiomeSection section = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);
    	
    	int sectionWidth = BiomeSection.sectionWidth;
    	
    	SimpleLocation lowerBound = section.getLowerBounds();
    	SimpleLocation upperBound = section.getUpperBounds();
    	
    	double lowestDiff = (double) sectionWidth;
    	
    	if(Math.abs(lowerBound.getX() - x) < lowestDiff) 
    		lowestDiff = Math.abs(lowerBound.getX() - x);
    	if(Math.abs(upperBound.getX() - x) < lowestDiff) 
    		lowestDiff = Math.abs(upperBound.getX() - x);
    	if(Math.abs(lowerBound.getZ() - z) < lowestDiff) 
    		lowestDiff = Math.abs(lowerBound.getZ() - z);
    	if(Math.abs(upperBound.getZ() - z) < lowestDiff) 
    		lowestDiff = Math.abs(upperBound.getZ() - z);
    	
    	double factor = lowestDiff/((double) sectionWidth);
    	
    	//Considered far from edge if it is 25% within the biomesection
    	if(factor >= biomeThreshold) {
    		factor = 1;
    	}
    	
    	return factor;
    	
    	
//        if (BiomeBank.selectBiome(section, temp, moist) != currentBiome) return 0;
//
//        double tempDecimals = Math.abs(temp - (int) temp);
//        double moistDecimals = Math.abs(moist - (int) moist);
//
//        // These tell if current point is near biome edge in biome grid
//        boolean tempIncrease = tempDecimals < 0.5 && tempDecimals > 0.5 - biomeThreshold;
//        boolean tempDecrease = tempDecimals > 0.5 && tempDecimals < 0.5 + biomeThreshold;
//        boolean moistIncrease = moistDecimals < 0.5 && moistDecimals > 0.5 - biomeThreshold;
//        boolean moistDecrease = moistDecimals > 0.5 && moistDecimals < 0.5 + biomeThreshold;
//
//        // Calculate biome that will be changed to
//        double nextTemp = temp;
//        if (tempIncrease) nextTemp = Math.min(10, temp + 1);
//        else if (tempDecrease) nextTemp = Math.max(0, temp - 1);
//
//        double nextMoist = moist;
//        if (moistIncrease) nextMoist = Math.min(10, moist + 1);
//        else if (moistDecrease) nextMoist = Math.max(0, moist - 1);
//
//        //Rounds removed from here, not sure why they were being used.
//        BiomeBank nextTempBiome = BiomeBank.selectBiome(section,(nextTemp), (moist));
//        BiomeBank nextMoistBiome = BiomeBank.selectBiome(section, (temp), (nextMoist));
//        BiomeBank nextCornerBiome = BiomeBank.selectBiome(section, (nextTemp), (nextMoist));
//
//        // Calculate how near to the edge the point is
//        double tempFactor = Math.abs((0.5 - tempDecimals) / biomeThreshold);
//        double moistFactor = Math.abs((0.5 - moistDecimals) / biomeThreshold);
//
//        double factor = 1;
//
//        boolean cornerSituation = tempFactor < 1 && moistFactor < 1 && (
//                nextCornerBiome != currentBiome && nextTempBiome == currentBiome && nextMoistBiome == currentBiome
//        );
//        boolean tempSituation = tempFactor < 1 && nextTempBiome != currentBiome;
//        boolean moistSituation = moistFactor < 1 && nextMoistBiome != currentBiome;
//
//        // If in L shaped corner in BiomeGrid
//        if (cornerSituation) factor = Math.max(tempFactor, moistFactor);
//        else if (tempSituation) factor = tempFactor;
//        else if (moistSituation) factor = moistFactor;
//
//        return factor;
    }

    /**
     * @param biomeThreshold Value between > 0 and 1, defines how quickly
     *                       output value approaches 0 when near biome edge.
     *                       Default of 0.25, which means blending will start
     *                       1/4 "biome grid units" from biome edge.
     */
    public BiomeBlender setBiomeThreshold(double biomeThreshold) {
        this.biomeThreshold = biomeThreshold;
        return this;
    }

    /**
     * @param riverThreshold Default value of 5, which means
     *                       linear blending happens when river
     *                       depth is more than -5 (0 > dep > -5).
     */
    public BiomeBlender setRiverThreshold(int riverThreshold) {
        this.riverThreshold = riverThreshold;
        return this;
    }

    /**
     * @param mountainThreshold Default value of 5, which means
     *                          linear blending happens when closer
     *                          than 5 blocks from mountain height threshold.
     */
    public BiomeBlender setMountainThreshold(int mountainThreshold) {
        this.mountainThreshold = mountainThreshold;
        return this;
    }

    /**
     * @param blendBeachesToo If false, blending will happen *riverThreshold*
     *                        away from sea level instead of beach level. In other words,
     *                        controls if blending happens based on sea level or river depth.
     */
    public BiomeBlender setBlendBeaches(boolean blendBeachesToo) {
        this.blendBeachesToo = blendBeachesToo;
        return this;
    }
}
