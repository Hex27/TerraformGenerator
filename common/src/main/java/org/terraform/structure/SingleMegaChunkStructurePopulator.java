package org.terraform.structure;

import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;

/**
 * Represents larger structures that will only spawn once per megachunk.
 * This will ALWAYS spawn structures in the center of the mega chunk, and 
 * query the biome type from the center coord's biomesection.
 * 
 * SingleMegaChunkStructures CANNOT overlap.
 */
public abstract class SingleMegaChunkStructurePopulator extends StructurePopulator {

	/**
	 * Do special checks here, including biome white/blacklisting and coordinate
	 * calculations. Also check for config spawnrates here.
	 * @param tw
	 * @param chunkX
	 * @param chunkZ
	 * @param biome
	 * @return
	 */
	public abstract boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome);

	
}
