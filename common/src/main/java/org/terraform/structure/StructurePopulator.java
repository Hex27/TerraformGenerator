package org.terraform.structure;

import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public abstract class StructurePopulator {
	
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean isEnabled();

    public abstract void populate(TerraformWorld world, PopulatorDataAbstract data);

    public abstract Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ);
    
    /**
     * Refers to 6x6 chunks as a buffer distance for the structure to spawn 
     * with the default value.
     * This buffer will force biome populators to stop populating 
     * certain things for that chunk radius.
     * <p>
     * For underground structures, this should be "0" to denote NO buffer
     * <p>
     * Only works for SingleMegaChunkStructurePopulators
	 */
    public int getChunkBufferDistance() {
    	return 3;
    }
}
