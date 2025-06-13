package org.terraform.biome.mountainous;

import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.*;
import org.terraform.coregen.HeightMap;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public abstract class AbstractMountainHandler extends BiomeHandler {

    protected double getPeakMultiplier(@NotNull BiomeSection section, @NotNull Random sectionRandom)
    {
        boolean surroundedByMountains = true;

        // Check direct faces, not diagonals
        for (BlockFace face: BlockUtils.directBlockFaces) {
            switch(section.getRelative(face.getModX(), face.getModZ()).getBiomeBank().getType()){
                case MOUNTAINOUS, HIGH_MOUNTAINOUS -> { }
                default -> surroundedByMountains = false;
            }
        }

        double lowerBound, upperBound;
        if(surroundedByMountains){
            lowerBound = TConfig.c.BIOME_MOUNTAINOUS_BESIDE_MOUNT_PEAK_MIN;
            upperBound = TConfig.c.BIOME_MOUNTAINOUS_BESIDE_MOUNT_PEAK_MAX;
        }else{
            lowerBound = TConfig.c.BIOME_MOUNTAINOUS_BESIDE_NORMAL_PEAK_MIN;
            upperBound = TConfig.c.BIOME_MOUNTAINOUS_BESIDE_NORMAL_PEAK_MAX;
        }

        return GenUtils.randDouble(sectionRandom, lowerBound, upperBound);
    }

    /**
     * Mountain height calculation works by taking the BiomeSection
     * center, then multiplying current height to peak at that location.
     */
    @Override
    public double calculateHeight(@NotNull TerraformWorld tw, int x, int z) {

        double height = HeightMap.CORE.getHeight(tw, x, z);

        // Let mountains cut into adjacent sections.
        double maxMountainRadius = BiomeSection.sectionWidth;
        // Double attrition height
        height += HeightMap.ATTRITION.getHeight(tw, x, z);

        BiomeSection sect = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);
        if (sect.getBiomeBank().getType() != BiomeType.MOUNTAINOUS) {
            sect = BiomeSection.getMostDominantSection(tw, x, z);
        }

        Random sectionRand = sect.getSectionRandom();
        double maxPeak = getPeakMultiplier(sect, sectionRand);

        // Let's just not offset the peak. This seems to give a better result.
        SimpleLocation mountainPeak = sect.getCenter();

        double distFromPeak = (1.42 * maxMountainRadius)
                              - Math.sqrt(Math.pow(x - mountainPeak.getX(), 2)
                             + Math.pow(z - mountainPeak.getZ(),2));

        double heightMultiplier = maxPeak * (distFromPeak / maxMountainRadius);
        //Now, we need to "connect" the heights of adjacent mountains/oceans.
        // Check the biomesection closest to this one, and MIN the heightMultiplier
        // depending on what biome it is
        switch(sect.getRelative(sect.getSubSection(x,z)).getBiomeBank().getType()){
            case OCEANIC, DEEP_OCEANIC ->
                heightMultiplier = Math.min(heightMultiplier, TConfig.c.BIOME_MOUNTAINOUS_CONNECTOR_OCEAN_MULT);
            case MOUNTAINOUS, HIGH_MOUNTAINOUS ->
                heightMultiplier = Math.max(heightMultiplier, TConfig.c.BIOME_MOUNTAINOUS_CONNECTOR_MOUNT_MULT);
        }

        height = height * heightMultiplier;

        height = limitSigmoid(height, TerraformGeneratorPlugin.injector.getMaxY() - 20);

        return height;
    }

    //Tapers down the ending height with the Sigmoid function.
    //Guarantees a number below the input limit
    //\frac{1}{1+e^{\left(x-285-6\right)}}
    private double limitSigmoid(double inputY, double limit){
        if(inputY <= limit) return inputY;

        return limit+(inputY-limit)*Math.pow(1.0+Math.exp((inputY-limit-6)),-1);
    }
}
