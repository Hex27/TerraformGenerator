package org.terraform.structure;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.ArrayList;
import java.util.Random;

public abstract class StructurePopulator {
	
    public abstract boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, ArrayList<BiomeBank> biomes);

    public abstract boolean isEnabled();

    public abstract void populate(TerraformWorld world, PopulatorDataAbstract data);

    public abstract int[] getNearestFeature(TerraformWorld world, int rawX, int rawZ);

    public abstract Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ);
    
    /**
     * Refers to 6x6 chunks as a buffer distance for the structure to spawn 
     * with the default value.
     * This buffer will force biome populators to stop populating 
     * certain things for that chunk radius.
     * 
     * For underground structures, this should be "0" to denote NO buffer
     * 
     * Only works for SingleMegaChunkStructurePopulators
     * @return
     */
    public int getChunkBufferDistance() {
    	return 3;
    }
}
