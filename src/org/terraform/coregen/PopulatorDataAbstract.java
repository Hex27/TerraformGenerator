package org.terraform.coregen;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.IBlockData;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.terraform.data.SimpleBlock;

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
}
