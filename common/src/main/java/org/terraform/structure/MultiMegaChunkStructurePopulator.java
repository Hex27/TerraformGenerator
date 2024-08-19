package org.terraform.structure;

import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;

/**
 * This represents structures that can spawn multiple times within a mega chunk,
 * including things like small dungeons and shipwrecks.
 */
public abstract class MultiMegaChunkStructurePopulator extends StructurePopulator {

    public abstract boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ);

    public abstract int[][] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc);

    public abstract int[] getNearestFeature(TerraformWorld world, int rawX, int rawZ);
}
