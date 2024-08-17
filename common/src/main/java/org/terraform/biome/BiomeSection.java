package org.terraform.biome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;

import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

public class BiomeSection {
	private int x, z;
	// A BiomeSection is 128 blocks wide (Default of bitshift 7).
	public static final int bitshifts = TConfigOption.BIOME_SECTION_BITSHIFTS.getInt();
	private TerraformWorld tw;
	public static final int sectionWidth = (int) (1 << bitshifts);
	public static final int minSize = sectionWidth;
	public static final int dominanceThreshold = (int)(0.35 * sectionWidth);
	private float temperature;
	private float moisture;
	private int radius;
	private BiomeBank biome;
	private FastNoise shapeNoise;
	
	/**
	 * Block x and z
	 * 
	 * @param x
	 * @param z
	 */
	protected BiomeSection(TerraformWorld tw, int x, int z) {
		this.x = x >> bitshifts;
		this.z = z >> bitshifts;
		this.tw = tw;
	}

	protected BiomeSection(TerraformWorld tw, int x, int z, boolean useSectionCoords) {
		this.x = x;
		this.z = z;
		this.tw = tw;
	}
	
	protected void doCalculations() {
		this.radius = GenUtils.randInt(getSectionRandom(), minSize / 2, 5 * minSize / 4);
		this.shapeNoise = new FastNoise(Objects.hash(tw.getSeed(), x, z));
		shapeNoise.SetNoiseType(NoiseType.SimplexFractal);
		shapeNoise.SetFractalOctaves(3);
		shapeNoise.SetFrequency(0.01f);
		this.biome = this.parseBiomeBank();
	}

	/**
	 * @return the width * width closest biome sections to this block point.
	 */
	public static Collection<BiomeSection> getSurroundingSections(TerraformWorld tw, int width, int blockX, int blockZ) {
	    BiomeSection homeSection = BiomeBank.getBiomeSectionFromBlockCoords(tw, blockX, blockZ);
	    Collection<BiomeSection> sections = new ArrayList<>();

		SimpleLocation center = homeSection.getCenter();
		int startX, startZ;
		if (width % 2 == 1) {
		    startX = startZ = -width / 2;
        } else {
		    startX = blockX >= center.getX() ? -width / 2 - 1 : -width / 2 ;
		    startZ = blockZ >= center.getZ() ? -width / 2 - 1 : -width / 2 ;
        }

	    for (int rx = startX; rx < startX + width; rx++) {
	        for (int rz = startZ; rz < startZ + width; rz++) {
                sections.add(homeSection.getRelative(rx, rz));
	        }
        }

	    if (sections.size() != width * width)
            TerraformGeneratorPlugin.logger.error("Section size was not " + (width * width) + ".");

	    return sections;
    }

	/**
	 * 
	 * @param blockX
	 * @param blockZ
	 * @return the four closest biome sections to this block point
	 */
	public static Collection<BiomeSection> getSurroundingSections(TerraformWorld tw, int blockX, int blockZ) {
		Collection<BiomeSection> sections = new ArrayList<>();

		BiomeSection homeBiome = BiomeBank.getBiomeSectionFromBlockCoords(tw, blockX, blockZ);
		sections.add(homeBiome);

		SimpleLocation center = homeBiome.getCenter();
		if (blockX >= center.getX()) {
			if (blockZ >= center.getZ()) {
				sections.add(homeBiome.getRelative(1, 0));
				sections.add(homeBiome.getRelative(1, 1));
				sections.add(homeBiome.getRelative(0, 1));
			} else {
				sections.add(homeBiome.getRelative(1, 0));
				sections.add(homeBiome.getRelative(1, -1));
				sections.add(homeBiome.getRelative(0, -1));
			}
		} else {
			if (blockZ >= center.getZ()) {
				sections.add(homeBiome.getRelative(-1, 0));
				sections.add(homeBiome.getRelative(-1, 1));
				sections.add(homeBiome.getRelative(0, 1));
			} else {
				sections.add(homeBiome.getRelative(-1, 0));
				sections.add(homeBiome.getRelative(-1, -1));
				sections.add(homeBiome.getRelative(0, -1));
			}

		}
		if (sections.size() != 4) {
			TerraformGeneratorPlugin.logger.error("Section size was not 4.");
		}
		return sections;
	}
	
	public Random getSectionRandom() {
		return new Random(Objects.hash(tw.getSeed(), x, z));
	}

	public Random getSectionRandom(int multiplier) {
		return new Random(multiplier*Objects.hash(tw.getSeed(), x, z));
	}

	public BiomeSection getRelative(int x, int z) {
        return BiomeBank.getBiomeSectionFromSectionCoords(this.tw, this.x + x, this.z + z, true);
	}

	public BiomeBank getBiomeBank() {
		return biome;
	}
	
	private BiomeBank parseBiomeBank() {
		temperature = 3.0f*2.5f*tw.getTemperatureOctave().GetNoise(this.x, this.z);
    	moisture = 3.0f*2.5f*tw.getMoistureOctave().GetNoise(this.x, this.z);
    	
    	//if(temperature > 2.5f) temperature = 2.5f;
    	//if(temperature < -2.5f) temperature = -2.5f;
    	//if(moisture > 2.5f) moisture = 2.5f;
    	//if(moisture < -2.5f) moisture = -2.5f;
    	
		return BiomeBank.selectBiome(this, temperature, moisture);//BiomeGrid.calculateBiome(BiomeType.FLAT, temperature, moisture);
	}

	/**
	 * Will be used to calculate which biome section has dominance in a certain
	 * block
	 * 
	 * @return
	 */
	public float getDominance(SimpleLocation target) {
		return getDominanceBasedOnRadius(target.getX(), target.getZ());
	}

	public float getDominanceBasedOnRadius(int blockX, int blockZ) {
		SimpleLocation center = this.getCenter();

		int xOffset = center.getX() - blockX;
		int zOffset = center.getZ() - blockZ;

		double equationResult = Math.pow(xOffset, 2) / Math.pow(radius, 2) 
				+ Math.pow(zOffset, 2) / Math.pow(radius, 2)
				+ 0.7 * shapeNoise.GetNoise(xOffset, zOffset);

		// if(1 -1*(equationResult) < 0)
		// TerraformGeneratorPlugin.logger.info("Radius Dominance: (" + blockX + "," +
		// blockZ + ") "+ xOffset + "," + zOffset + ": " + equationResult);
		return (float) (1 - 1 * (equationResult));

	}

	public SimpleLocation getCenter() {
		int x = ((this.x << bitshifts)) + sectionWidth / 2;
		int z = ((this.z << bitshifts)) + sectionWidth / 2;
		// TerraformGeneratorPlugin.logger.info("Center " + toString() + ": " + x + ","
		// + z);
		return new SimpleLocation(x, 0, z);
	}
	
	
	/**
	 * 
	 * @return Block coords of lowest coord pair in the section's square
	 */
	public SimpleLocation getLowerBounds() {
		int x = ((this.x << bitshifts));
		int z = ((this.z << bitshifts));
		return new SimpleLocation(x, 0, z);
	}

	/**
	 * 
	 * @return Block coords of highest coord pair in the section's square
	 */
	public SimpleLocation getUpperBounds() {
		int x = ((this.x << bitshifts)) + sectionWidth;
		int z = ((this.z << bitshifts)) + sectionWidth;
		return new SimpleLocation(x, 0, z);
	}
	
	public static BiomeSection getMostDominantSection(TerraformWorld tw, int x, int z) {

        double dither = TConfigOption.BIOME_DITHER.getDouble();
    	Random locationBasedRandom  = new Random(Objects.hash(tw.getSeed(),x,z));
    	SimpleLocation target  = new SimpleLocation(x,0,z);
    	BiomeSection homeSection = BiomeBank.getBiomeSectionFromBlockCoords(tw, x,z);
    	
    	//Don't calculate if distance is very close to center
    	if(target.distance(homeSection.getCenter()) <= dominanceThreshold) {
    		return homeSection;
    	}
    	
    	Collection<BiomeSection> sections = BiomeSection.getSurroundingSections(tw, x, z);
    	BiomeSection mostDominant = homeSection;
    	
    	for(BiomeSection sect:sections) {
    		float dom = (float) (sect.getDominance(target)+GenUtils.randDouble(locationBasedRandom,-dither,dither));
    		
    		if(dom > mostDominant.getDominance(target)+GenUtils.randDouble(locationBasedRandom,-dither,dither))
    			mostDominant = sect;
    	}
    	
    	return mostDominant;
	}

	/**
	 * 
	 * @param radius in biomesection coords
	 * @return surrounding biome sections at radius distance away 
	 */
    public Collection<BiomeSection> getRelativeSurroundingSections(int radius) {
        if (radius == 0) {
        	BiomeSection target = this;
        	return new ArrayList<BiomeSection>() {{
            	add(target);
            }};
        }
        //     xxxxx
        //xxx  x   x
        //xox  x o x
        //xxx  x   x
        //     xxxxx
        ArrayList<BiomeSection> candidates = new ArrayList<BiomeSection>();
        
        //Lock rX, iterate rZ
        for(int rx:new int[] {-radius,radius}) {
        	 for (int rz = -radius; rz <= radius; rz++) {
        		 candidates.add(this.getRelative(rx, rz));
             }
        }
        
        //Lock rZ, iterate rX
        for(int rz:new int[] {-radius,radius}) {
       	 for (int rx = 1-radius; rx <= radius-1; rx++) {
       		 candidates.add(this.getRelative(rx, rz));
            }
       }

        return candidates;
    }
    
    /**
     * 
     * @param rawX
     * @param rawZ
     * @return the subsection within this biome section that the coordinates belong in.
     * Works even if the coords are outside the biome section.
     */
    public BiomeSubSection getSubSection(int rawX, int rawZ) {
    	//if(new BiomeSection(tw, rawX, rawZ).equals(this)) {
    		SimpleLocation sectionCenter = this.getCenter();
    		int relXFromCenter = rawX - sectionCenter.getX();
    		int relZFromCenter = rawZ - sectionCenter.getZ();
    		
    		if(relXFromCenter > 0) {
    			if(relXFromCenter >= Math.abs(relZFromCenter))
    				return BiomeSubSection.POSITIVE_X;
    		}
    		
    		if(relXFromCenter <= 0) {
    			if(Math.abs(relXFromCenter) >= Math.abs(relZFromCenter))
    				return BiomeSubSection.NEGATIVE_X;
    		}

    		if(relZFromCenter > 0) {
    			if(relZFromCenter >= Math.abs(relXFromCenter))
    				return BiomeSubSection.POSITIVE_Z;
    		}
    		
    		if(relZFromCenter <= 0) {
    			if(Math.abs(relZFromCenter) >= Math.abs(relXFromCenter))
    				return BiomeSubSection.NEGATIVE_Z;
    		}
    		
    		return BiomeSubSection.NONE;
    		
    	//}
    	
    	//TerraformGeneratorPlugin.logger.info("Mismatch in biomesection! " + new BiomeSection(tw, rawX, rawZ).toString() + " vs " + this.toString());
    	//return BiomeSubSection.NONE;
    }

	@Override
	public int hashCode() {
		int prime = 13;
		int result = 5;

		result = prime * result + x;
		result = prime * result + z;
		result = prime * result + tw.getName().hashCode();

		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BiomeSection) {
			BiomeSection BiomeSection = (BiomeSection) obj;
			return this.tw.getName().equals(BiomeSection.tw.getName()) 
					&& this.x == BiomeSection.x 
					&& this.z == BiomeSection.z;
		}
		return false;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	@Override
	public String toString() {
		return "(" + x + "," + z + ")";
	}
	
	public BiomeClimate getClimate() {
		return BiomeClimate.selectClimate(temperature, moisture);
	}

	public float getTemperature() {
		return temperature;
	}

	public float getMoisture() {
		return moisture;
	}

	public TerraformWorld getTw() {
		return tw;
	}

	public double getOceanLevel() {
    	return tw.getOceanicNoise().GetNoise(x,z)*50.0;
	}

	public double getMountainLevel() {
    	return tw.getMountainousNoise().GetNoise(x,z)*50.0;
	}
}
