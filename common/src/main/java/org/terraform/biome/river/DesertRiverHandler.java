package org.terraform.biome.river;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public class DesertRiverHandler extends RiverHandler {
    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        super.populateSmallItems(world, random, data);

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                OasisRiver.generateOasisRiver(world, random, data, x, z, BiomeBank.DESERT);
            }
        }
    }
}
