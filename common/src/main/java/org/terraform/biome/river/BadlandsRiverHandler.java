package org.terraform.biome.river;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public class BadlandsRiverHandler extends RiverHandler {
    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        super.populateSmallItems(world, random,rawX, surfaceY, rawZ, data);
        OasisRiver.generateOasisRiver(world, random, data, rawX, rawZ, BiomeBank.BADLANDS);
    }
}
