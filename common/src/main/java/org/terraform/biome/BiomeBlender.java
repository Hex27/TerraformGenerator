package org.terraform.biome;

import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;

import java.util.Collection;

/**
 * This class is used to determine an "edge factor".
 * The edge factor refers to how close any xz coordinate is to
 * a river or another biome. For example, being on near biome border
 * or river is an edge factor of near 0. This helps for features
 * like the plateaus in Eroded Plains to avoid rising in rivers and
 * other biomes.
 */
public class BiomeBlender {
    final boolean blendBiomeGrid;
    final boolean blendWater;
    private final TerraformWorld tw;
    double gridBlendingFactor = 1;
    int riverThreshold = 5;
    boolean blendBeachesToo = true;
    int smoothBlendTowardsRivers = -1;

    public BiomeBlender(TerraformWorld tw, boolean blendBiomeGrid, boolean blendWater) {
        this.tw = tw;

        this.blendBiomeGrid = blendBiomeGrid;
        this.blendWater = blendWater;
    }

    public BiomeBlender(TerraformWorld tw) {
        this(tw, true, true);
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
            double riverFactor;
            if (smoothBlendTowardsRivers == -1) {
                riverFactor = blendBeachesToo
                              ? riverDepth / (-riverThreshold)
                              : (HeightMap.getPreciseHeight(tw, x, z) - TerraformGenerator.seaLevel) / riverThreshold;
            }
            else {
                double height = HeightMap.getPreciseHeight(tw, x, z);
                if (height > TerraformGenerator.seaLevel + smoothBlendTowardsRivers) {
                    riverFactor = 1;
                }
                else if (height <= TerraformGenerator.seaLevel) {
                    riverFactor = 0;
                }
                else {
                    // if(height > TerraformGenerator.seaLevel)
                    //	height = TerraformGenerator.seaLevel;
                    // Linearly blend
                    riverFactor = ((height - TerraformGenerator.seaLevel) / ((double) smoothBlendTowardsRivers));
                    // TerraformGeneratorPlugin.logger.info("RF" + riverFactor);
                }
            }
            if (riverFactor < factor) {
                factor = Math.max(0, riverFactor);
            }
        }

        if (blendBiomeGrid) {
            // Same here when closer to biome edge
            double gridFactor = getGridEdgeFactor(
                    currentBiome,
                    tw,
                    x,
                    z
            );// getGridEdgeFactor(BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z),
            // currentBiome,
            // BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z).getTemperature(), BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z).getMoisture());
            if (gridFactor < factor) {
                factor = gridFactor;
            }
        }

        return factor;
    }

    /*
        Get edge factor only based on land, ignore rivers
        Updated to reflect new changes to heightmap and biomes
     */
    public double getGridEdgeFactor(BiomeBank currentBiome, TerraformWorld tw, int x, int z) {
        SimpleLocation target = new SimpleLocation(x, 0, z);
        Collection<BiomeSection> sections = BiomeSection.getSurroundingSections(tw, 3, x, z);

        BiomeSection mostDominantTarget = null;
        double dominance = -100;
        for (BiomeSection section : sections) {
            if (section.getBiomeBank() == currentBiome) {
                double dom = section.getDominance(target);
                if (dom > dominance) {
                    mostDominantTarget = section;
                    dominance = dom;
                }

            }
        }
        if (mostDominantTarget == null) {
            return 0;
        }

        double factor = 1;
        for (BiomeSection section : sections) {
            if (section.getBiomeBank() == currentBiome) {
                continue;
            }

            float dom = section.getDominance(target);
            double diff = Math.max(0, dominance - dom);

            factor = Math.min(factor, diff * gridBlendingFactor);
        }

        return Math.min(factor, 1);
    }

    /**
     * @param gridBlendingFactor Section dominance difference is multiplied
     *                           by this value. Can be used to control how "steep"
     *                           the blending near biome edge is.
     */
    public @NotNull BiomeBlender setGridBlendingFactor(double gridBlendingFactor) {
        this.gridBlendingFactor = gridBlendingFactor;
        return this;
    }

    /**
     * @param riverThreshold Default value of 5, which means
     *                       linear blending happens when river
     *                       depth is more than -5 (0 > dep > -5).
     */
    public @NotNull BiomeBlender setRiverThreshold(int riverThreshold) {
        this.riverThreshold = riverThreshold;
        return this;
    }

    /**
     * Use a slower algorithm to smooth towards rivers based on heightmap's getPreciseHeight
     * <p>
     * A higher number means a longer blend.
     */
    public @NotNull BiomeBlender setSmoothBlendTowardsRivers(int smoothBlendTowardsRivers)
    {
        this.smoothBlendTowardsRivers = smoothBlendTowardsRivers;
        return this;
    }

    /**
     * @param blendBeachesToo If false, blending will happen *riverThreshold*
     *                        away from sea level instead of beach level. In other words,
     *                        controls if blending happens based on sea level or river depth.
     */
    public @NotNull BiomeBlender setBlendBeaches(boolean blendBeachesToo) {
        this.blendBeachesToo = blendBeachesToo;
        return this;
    }
}