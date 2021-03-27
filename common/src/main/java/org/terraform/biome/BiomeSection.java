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
	// A BiomeSection is 128 blocks wide.
	private static final int bitshifts = 7;
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
	
	private BiomeBank parseBiomeBank() {
		temperature = 3.0f*2.5f*tw.getTemperatureOctave().GetNoise(this.x, this.z);
    	moisture = 3.0f*2.5f*tw.getMoistureOctave().GetNoise(this.x, this.z);
    	
    	if(temperature > 2.5f) temperature = 2.5f;
    	if(temperature < -2.5f) temperature = -2.5f;
    	if(moisture > 2.5f) moisture = 2.5f;
    	if(moisture < -2.5f) moisture = -2.5f;
    	
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
