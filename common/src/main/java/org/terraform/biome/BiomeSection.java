package org.terraform.biome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;

import org.terraform.data.SimpleLocation;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class BiomeSection{
    private int x, z;
    private static final int bitshifts = 7;
    public static final int sectionWidth = (int) (1 << bitshifts);
    private static final int minSize = sectionWidth;
    private final int radius;
    //A BiomeSection is 64 blocks wide.
    
    /**
     * Block x and z
     * @param x
     * @param z
     */
    public BiomeSection(int x, int z) {
        this.x = x >> bitshifts;
        this.z = z >> bitshifts;
        this.radius = GenUtils.randInt(getSectionRandom(), minSize/2, 5*minSize/4);
    }
    
    public BiomeSection(int x, int z, boolean useSectionCoords) {
        this.x = x;
        this.z = z;
        this.radius = GenUtils.randInt(getSectionRandom(), minSize/2, 5*minSize/4);
    }
    
    
    /**
     * 
     * @param blockX
     * @param blockZ
     * @return the four closest biome sections to this point
     */
    public static Collection<BiomeSection> getSurroundingSections(int blockX, int blockZ){
    	Collection<BiomeSection> sections = new ArrayList<>();
    	
    	BiomeSection homeBiome = new BiomeSection(blockX, blockZ);
    	sections.add(homeBiome);
    	
    	SimpleLocation center = homeBiome.getCenter();
    	if(blockX >= center.getX()) {
    		if(blockZ >= center.getZ()) {
    			sections.add(homeBiome.getRelative(1, 0));
    			sections.add(homeBiome.getRelative(1, 1));
    			sections.add(homeBiome.getRelative(0, 1));
    		}else {
    			sections.add(homeBiome.getRelative(1, 0));
    			sections.add(homeBiome.getRelative(1, -1));
    			sections.add(homeBiome.getRelative(0, -1));
    		}
    	}else {
    		if(blockZ >= center.getZ()) {
    			sections.add(homeBiome.getRelative(-1, 0));
    			sections.add(homeBiome.getRelative(-1, 1));
    			sections.add(homeBiome.getRelative(0, 1));
    		}else {
    			sections.add(homeBiome.getRelative(-1, 0));
    			sections.add(homeBiome.getRelative(-1, -1));
    			sections.add(homeBiome.getRelative(0, -1));
    		}
    	
    	}
    	if(sections.size() != 4){
    		TerraformGeneratorPlugin.logger.error("Section size was not 4.");
    	}
    	return sections;
    }
    
   
    public Random getSectionRandom() {
    	return new Random(Objects.hash(127,x,z));
    }

    public BiomeSection getRelative(int x, int z) {
        BiomeSection mc = new BiomeSection(this.x+x, this.z+z, true);
        return mc;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BiomeSection) {
            BiomeSection BiomeSection = (BiomeSection) obj;
            return this.x == BiomeSection.x && this.z == BiomeSection.z;
        }
        return false;
    }
    
    public BiomeBank parseBiomeBank() {
    	FastNoise noise = new FastNoise(12);
    	noise.SetNoiseType(NoiseType.Simplex);
    	noise.SetFrequency(0.06f);
    	
    	float temperature = noise.GetNoise(x,z)*2.77f;

    	noise = new FastNoise(24);
    	noise.SetNoiseType(NoiseType.Simplex);
    	noise.SetFrequency(0.06f);
    	
    	float moisture =  noise.GetNoise(x,z)*2.77f;
    	
    	return BiomeGrid.calculateBiome(BiomeType.FLAT, temperature, moisture);
    }
    
    /**
     * Will be used to calculate which biome section has dominance in a 
     * certain block
     * @return
     */
    public float getDominance(SimpleLocation target, boolean debugMode) {
    	
//    	if(new BiomeSection(target.getX(),target.getZ()).toString().equals(this.toString())) {
//    		return 100;
//    	}
    	if(debugMode)
    		TerraformGeneratorPlugin.logger.info(x+","+z + "," + (Math.abs(x) % 2 == 1) + "," +  (Math.abs(z) % 2 == 1));
//    	if(Math.abs(x) % 2 == 1 || Math.abs(z) % 2 == 1) {
//    		return 0;
//    	}
    	
    	return getDominanceBasedOnRadius(target.getX(),target.getZ());
    }
    
    public float getDominanceBasedOnRadius(int blockX, int blockZ) {        
        FastNoise noise = new FastNoise(Objects.hash(127,x,z));
        noise.SetNoiseType(NoiseType.SimplexFractal);
        noise.SetFractalOctaves(5);
        noise.SetFrequency(0.01f);
        SimpleLocation center = this.getCenter();
        
        int xOffset = center.getX() - blockX;
        int zOffset = center.getZ() - blockZ;
        
        double equationResult = Math.pow(xOffset, 2) / Math.pow(radius, 2)
                + Math.pow(zOffset, 2) / Math.pow(radius, 2)
                + 0.7 * noise.GetNoise(xOffset,zOffset);
        
        //if(1 -1*(equationResult) < 0)
        //	TerraformGeneratorPlugin.logger.info("Radius Dominance: (" + blockX + "," + blockZ + ") "+ xOffset + "," + zOffset + ": " + equationResult);
        return (float) (1 -1*(equationResult));
        
    }
    
    public SimpleLocation getCenter() {
    	int x = ((this.x << bitshifts)) + sectionWidth/2;
    	int z = ((this.z << bitshifts)) + sectionWidth/2;
    	//TerraformGeneratorPlugin.logger.info("Center " + toString() + ": " + x + "," + z);
    	return new SimpleLocation(x,0,z);
    }
    
    @Override
    public int hashCode() {
        int prime = 13;
        int result = 5;

        result = prime * result + x;
        result = prime * result + z;

        return result;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
    
    @Override
    public String toString() {
    	return "(" + x + "," + z +")";
    }
}

