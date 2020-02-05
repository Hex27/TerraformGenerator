package org.terraform.coregen;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.terraform.main.TerraformGeneratorPlugin;

public class PopulatorDataPostGen extends PopulatorDataAbstract {
	private World w;
	private Chunk c;
	public PopulatorDataPostGen(Chunk c){
		this.w = c.getWorld();
		this.c = c;
	}

	@Override
	public Material getType(int x, int y, int z) {
		return w.getBlockAt(x,y,z).getType();
	}

	@Override
	public BlockData getBlockData(int x, int y, int z) {
		return w.getBlockAt(x,y,z).getBlockData();
	}

	@Override
	public void setType(int x, int y, int z, Material type) {
		//if(type == Material.DIRT) Bukkit.getLogger().info("Set " + x + "," + y + "," + z + " to dirt.");
		boolean isFragile = type.toString().contains("DOOR");
		w.getBlockAt(x, y, z).setType(type,!isFragile);
	}

	@Override
	public void setBlockData(int x, int y, int z, BlockData data) {
		boolean isFragile = data.getMaterial().toString().contains("DOOR");
		w.getBlockAt(x,y,z).setBlockData(data,!isFragile);
	}

	@Override
	public Biome getBiome(int rawX, int rawY, int rawZ) {
		return w.getBlockAt(rawX,rawY,rawZ).getBiome();
	}

	@Override
	public int getChunkX() {
		return c.getX();
	}

	@Override
	public int getChunkZ() {
		return c.getZ();
	}

	@Override
	public void addEntity(int rawX, int rawY, int rawZ, EntityType type) {
		Entity e = c.getWorld().spawnEntity(new Location(c.getWorld(),rawX,rawY,rawZ), type);
		e.setPersistent(true);
	}

	@Override
	public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {

		Block b = w.getBlockAt(rawX, rawY, rawZ);
		b.setType(Material.SPAWNER,false);
		CreatureSpawner spawner = (CreatureSpawner) b.getState(); 
		spawner.setSpawnedType(type);
		spawner.update();
		//TerraformGeneratorPlugin.injector.getICAData(c).setSpawner(rawX, rawY, rawZ, type);
	}

	@Override
	public void lootTableChest(int x, int y, int z, TerraLootTable table) {
		if(!w.getBlockAt(x,y,z).getType().toString().contains("CHEST")){
			TerraformGeneratorPlugin.logger.error("Attempted to set loot table to a non chest @ " + x +"," + y + "," + z);
		}
		TerraformGeneratorPlugin.injector.getICAData(w.getBlockAt(x,y,z).getChunk()).lootTableChest(x, y, z, table);
	}

}
