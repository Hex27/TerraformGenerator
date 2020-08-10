package org.terraform.coregen;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;

public abstract class PopulatorDataAbstract {

	/**
	 * Refers to raw x,y,z coords, not the chunk 0-15 coords.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public abstract Material getType(int x, int y, int z);
	
	/**
	 * Refers to raw x,y,z coords, not the chunk 0-15 coords.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public abstract BlockData getBlockData(int x, int y, int z);
	
	/**
	 * Refers to raw x,y,z coords, not the chunk 0-15 coords.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public abstract void setType(int x, int y, int z, Material type);
	
	/**
	 * Refers to raw x,y,z coords, not the chunk 0-15 coords.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public abstract void setBlockData(int x, int y, int z, BlockData data);
	
	/**
	 * Refers to raw x,y,z coords, not the chunk 0-15 coords.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Biome getBiome(int rawX, int rawZ){
		return getBiome(rawX, 50, rawZ);
	}
	
	public abstract Biome getBiome(int rawX, int rawY, int rawZ);
	
	public abstract void addEntity(int rawX, int rawY, int rawZ, EntityType type);
	
	public abstract int getChunkX();
	public abstract int getChunkZ();
	
	public abstract void setSpawner(int rawX, int rawY, int rawZ, EntityType type);
	
	public abstract void lootTableChest(int x,int y, int z, TerraLootTable table);
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getClass().getCanonicalName().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
}
