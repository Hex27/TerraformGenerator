package org.terraform.biome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;

import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class BiomeSection {
	private int x, z;
	// A BiomeSection is 256 blocks wide.
	private static final int bitshifts = 8;
	private TerraformWorld tw;
	public static final int sectionWidth = (int) (1 << bitshifts);
	private static final int minSize = sectionWidth;
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
		this.biome = this.parseBiomeBank();
		this.radius = GenUtils.randInt(getSectionRandom(), minSize / 2, 5 * minSize / 4);
		this.shapeNoise = new FastNoise(Objects.hash(tw.getSeed(), x, z));
		shapeNoise.SetNoiseType(NoiseType.SimplexFractal);
		shapeNoise.SetFractalOctaves(3);
		shapeNoise.SetFrequency(0.01f);
	}

	/**
	 * 
	 * @param blockX
	 * @param blockZ
	 * @return the four closest biome sections to this block point
	 */
	public static Collection<BiomeSection> getSurroundingSections(TerraformWorld tw, int blockX, int blockZ) {
		Collection<BiomeSection> sections = new ArrayList<>();

		BiomeSection homeBiome = BiomeBank.getBiomeSection(tw, blockX, blockZ);
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

	public BiomeSection getRelative(int x, int z) {
		BiomeSection mc = BiomeBank.getBiomeSection(this.tw, this.x + x, this.z + z, true);
		return mc;
	}

	public BiomeBank getBiomeBank() {
		return biome;
	}
	
	private double warpSine(double tempUnwarpedSine, int period, int seed) {
		//double warp = GenUtils.randInt(tw.getRand(seed),-7, 1);
		//if(warp == 0) warp = 1;
		//if(warp < 0) {
		//	warp = (15.0-2.0*warp)/10.0;
		//}
		double warp = 0.1;
		
		double warpedValue;
		if(tempUnwarpedSine == 0 && warp == 0) { //Prevent math error
			warpedValue = 0;
		}else {
			warpedValue = Math.pow(Math.abs(tempUnwarpedSine),warp);
		}
		if(tempUnwarpedSine < 0) {
			warpedValue = -warpedValue; //Preserve sign
		}
		return warpedValue;
	}
	
	private BiomeBank parseBiomeBank() {
		int period = 8;
		// Get starting offset with world seed
    	int xTempOffset = GenUtils.randInt(tw.getRand(3), 1,period/2);
    	int zTempOffset = GenUtils.randInt(tw.getRand(92), -period/2,period/2);
    	int xMoistOffset = xTempOffset + GenUtils.randInt(tw.getRand(11), 1,period/2);
    	int zMoistOffset = zTempOffset + GenUtils.randInt(tw.getRand(117), 1,period/2);
		
    	int effectiveTempX = x + xTempOffset;
    	int effectiveTempZ = z + zTempOffset;
    	int effectiveMoistX = x + xMoistOffset;
    	int effectiveMoistZ = z + zMoistOffset;
		//Temperature Calculation
		/**
		 * This works by having an original sine curve, then warping the value
		 * by bringing it to the power of a random value every period.
		 * This makes every segment vary differently.
		 */
		double unwarpedTSineX = Math.sin((2.0*Math.PI/((double)period))*((double)effectiveTempX));
		double unwarpedTSineZ = Math.sin((2.0*Math.PI/((double)period))*((double)effectiveTempZ));
		double unwarpedMSineX = Math.sin((2.0*Math.PI/((double)period))*((double)effectiveMoistX));
		double unwarpedMSineZ = Math.sin((2.0*Math.PI/((double)period))*((double)effectiveMoistZ));
		
		//A simple hash that reflects the number of sine periods in x and z
		int tempSineSegmentHash = (effectiveTempX/period)+11*(effectiveTempZ/period);
		int moistSineSegmentHash = (effectiveMoistX/period)+11*(effectiveMoistZ/period);
		
		//Warp the values accordingly
		double temperatureX = warpSine(unwarpedTSineX, period, 71+tempSineSegmentHash);
		double temperatureZ = warpSine(unwarpedTSineZ, period, 71+tempSineSegmentHash);
		double moistureX = warpSine(unwarpedMSineX, period, 111+moistSineSegmentHash);
		double moistureZ = warpSine(unwarpedMSineZ, period, 111+moistSineSegmentHash);
		
		//Cache
		this.temperature = (float) (2.5*(temperatureX*temperatureZ));
		this.moisture = (float) (2.5*(moistureX*moistureZ));
		
		//temperature = (2f)*2.5f*tw.getTemperatureOctave().GetNoise(this.x, this.z);
    	//moisture = (2f)*2.5f*tw.getMoistureOctave().GetNoise(this.x, this.z);
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

	public float getTemperature() {
		return temperature;
	}

	public float getMoisture() {
		return moisture;
	}

	public TerraformWorld getTw() {
		return tw;
	}
}
