package org.terraform.structure;

import java.util.ArrayList;

import org.terraform.biome.BiomeBank;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;

/**
 * This represents structures that can spawn multiple times within a mega chunk,
 * including things like small dungeons and shipwrecks.
 */
public abstract class MultiMegaChunkStructurePopulator extends StructurePopulator {
	
	public abstract boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, ArrayList<BiomeBank> biomes);
	
	public abstract int[][] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc);

    public abstract int[] getNearestFeature(TerraformWorld world, int rawX, int rawZ);
}
