package org.terraform.biome.river;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public class BadlandsRiverHandler extends RiverHandler {
    @Override
    public void populateSmallItems(@NotNull TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        super.populateSmallItems(world, random, rawX, surfaceY, rawZ, data);
        OasisRiver.generateOasisRiver(world, random, data, rawX, rawZ, BiomeBank.BADLANDS);
    }
}
