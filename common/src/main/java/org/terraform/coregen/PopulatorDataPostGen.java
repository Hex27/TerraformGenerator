package org.terraform.coregen;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

public class PopulatorDataPostGen extends PopulatorDataAbstract {
    private final World w;
    private final Chunk c;

    public PopulatorDataPostGen(Chunk c) {
        this.w = c.getWorld();
        this.c = c;
    }

    /**
     * @return the w
     */
    public World getWorld() {
        return w;
    }


    /**
     * @return the c
     */
    public Chunk getChunk() {
        return c;
    }


    @Override
    public Material getType(int x, int y, int z) {
        return w.getBlockAt(x, y, z).getType();
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        return w.getBlockAt(x, y, z).getBlockData();
    }

    @Override
    public void setType(int x, int y, int z, Material type) {
        boolean isFragile = type.toString().contains("DOOR") ||
                type.toString().contains("CARPET") ||
                type == Material.FARMLAND ||
                type == Material.WATER;
        //TerraformGeneratorPlugin.injector.getICAData(w.getBlockAt(x,y,z).getChunk())
        //.setType(x, y, z, type);
        Block b = w.getBlockAt(x, y, z);
        b.setType(type, !isFragile);
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData data) {
        boolean isFragile = data.getMaterial().toString().contains("DOOR") ||
        		data.getMaterial().toString().contains("CARPET") ||
                data.getMaterial() == Material.FARMLAND ||
                data.getMaterial() == Material.WATER;
        //TerraformGeneratorPlugin.injector.getICAData(w.getBlockAt(x,y,z).getChunk())
        //.setBlockData(x, y, z, data);
        Block b = w.getBlockAt(x, y, z);
        b.setBlockData(data.clone(), !isFragile);
    }

    /**
     * Blockstates are mutable, so just edit them. There is no method to directly set them.
     * @param x
     * @param y
     * @param z
     * @param state
     * @return
     */
    public BlockState getBlockState(int x, int y, int z) {
        Block b = w.getBlockAt(x, y, z);
        return b.getState();
    }

    public void noPhysicsUpdateForce(int x, int y, int z, BlockData data) {
        Block b = w.getBlockAt(x, y, z);
        b.setBlockData(data.clone(), false);
    }

    @Override
    public Biome getBiome(int rawX, int rawZ) {
        return w.getBlockAt(rawX, TerraformGenerator.seaLevel, rawZ).getBiome();
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
    public void addEntity(int x, int y, int z, EntityType type) {
    	//Always offset by 0.5 to prevent them spawning in corners.
    	//Y is offset by a small bit to prevent falling through weird spawning areas
        Entity e = c.getWorld().spawnEntity(new Location(c.getWorld(),x+0.5,y+0.3,z+0.5), type);
        e.setPersistent(true);
    }
    
    private static int spawnerRetries = 0;
    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
        Block b = w.getBlockAt(rawX, rawY, rawZ);
        b.setType(Material.SPAWNER, false);
        try {
            CreatureSpawner spawner = (CreatureSpawner) b.getState();
            spawner.setSpawnedType(type);
            spawner.update();
        }
        catch(IllegalStateException e)
        {
        	spawnerRetries++;
        	if(spawnerRetries > 10){ 
            	Bukkit.getLogger().info("Giving up on spawner at " + rawX + "," + rawY + "," + rawZ);
            	spawnerRetries = 0;
        		return;
        	}
        	Bukkit.getLogger().info("Failed to get state for spawner, try " + spawnerRetries);
        	setSpawner(rawX, rawY, rawZ, type);
        }
    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
//		if(!w.getBlockAt(x,y,z).getType().toString().contains("CHEST")){
//			TerraformGeneratorPlugin.logger.error("Attempted to set loot table to a non chest @ " + x +"," + y + "," + z);
//		}
        TerraformGeneratorPlugin.injector.getICAData(w.getBlockAt(x, y, z).getChunk()).lootTableChest(x, y, z, table);
//		LootTable lt = LootTables.valueOf(table.toString()).getLootTable();
//		Block chestBlock = w.getBlockAt(x,y,z);
//		if((chestBlock.getState() instanceof Lootable)){
//			Lootable state = (Lootable) chestBlock.getState();
//			state.setLootTable(lt);
//			//chestBlock.getState().update();
//		}
    }

	@Override
	public TerraformWorld getTerraformWorld() {
		return TerraformWorld.get(w);
	}
}
