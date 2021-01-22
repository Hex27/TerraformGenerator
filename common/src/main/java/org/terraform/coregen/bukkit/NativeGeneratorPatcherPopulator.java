package org.terraform.coregen.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.BlockPopulator;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

public class NativeGeneratorPatcherPopulator extends BlockPopulator implements Listener{

    //SimpleChunkLocation to a collection of location:blockdata entries marked for repair.
    public static HashMap<SimpleChunkLocation, Collection<Object[]>> cache = new HashMap<SimpleChunkLocation, Collection<Object[]>>();
    //private final TerraformWorld tw;

    public NativeGeneratorPatcherPopulator() {
        //this.tw = tw;
    	Bukkit.getPluginManager().registerEvents(this, TerraformGeneratorPlugin.get());
    }
    
    public static void pushChange(String world, int x, int y, int z, BlockData data) {
    	
        SimpleChunkLocation scl = new SimpleChunkLocation(world, x, y, z);
        if (!cache.containsKey(scl))
            cache.put(scl, new ArrayList<Object[]>());

        cache.get(scl).add(new Object[]{
                new int[]{x, y, z},
                data
        });
    }
    
    public static void flushChanges() {
    	TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Flushing remaining repairs.");
        ArrayList<SimpleChunkLocation> locs = new ArrayList<>();
    	for(SimpleChunkLocation scl:cache.keySet()) {
    		locs.add(scl);
    	}
    	for(SimpleChunkLocation scl:locs) {
    		World w = Bukkit.getWorld(scl.getWorld());
    		if(w == null) continue;
    		if(w.isChunkLoaded(scl.getX(), scl.getZ())) {
    			Collection<Object[]> changes = cache.remove(scl);
    	        if (changes != null) {
    	        	//TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Detected anomalous generation by NMS on " + scl + ". Running repairs on " + changes.size() + " blocks");
    	            for (Object[] entry : changes) {
    	                int[] loc = (int[]) entry[0];
    	                BlockData data = (BlockData) entry[1];
    	                w.getBlockAt(loc[0], loc[1], loc[2])
    	                        .setBlockData(data, false);
    	            }
    	        }
    		}else {
    			//Let the event handler do it
    			w.loadChunk(scl.getX(), scl.getZ());
    		}
    	}
    }
    
    @Override
    public void populate(World world, Random random, Chunk chunk) {
        SimpleChunkLocation scl = new SimpleChunkLocation(chunk);
        Collection<Object[]> changes = cache.remove(scl);
        if (changes != null) {
        	//TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Detected anomalous generation by NMS on " + scl + ". Running repairs on " + changes.size() + " blocks");
            for (Object[] entry : changes) {
                int[] loc = (int[]) entry[0];
                BlockData data = (BlockData) entry[1];
                world.getBlockAt(loc[0], loc[1], loc[2])
                        .setBlockData(data, false);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        SimpleChunkLocation scl = new SimpleChunkLocation(event.getChunk());
        Collection<Object[]> changes = cache.remove(scl);
        if (changes != null) {
        	//TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Detected anomalous generation by NMS on " + scl + ". Running repairs on " + changes.size() + " blocks");
            for (Object[] entry : changes) {
                int[] loc = (int[]) entry[0];
                BlockData data = (BlockData) entry[1];
                event.getChunk().getWorld().getBlockAt(loc[0], loc[1], loc[2])
                        .setBlockData(data, false);
            }
        }
    }
    
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
    	TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Flushing repairs for " + event.getWorld().getName());
        for(SimpleChunkLocation scl:cache.keySet()) {
        	if(!scl.getWorld().equals(event.getWorld().getName()))
        		continue;
        	Collection<Object[]> changes = cache.get(scl);
	        if (changes != null) {
	        	for (Object[] entry : changes) {
	                int[] loc = (int[]) entry[0];
	                BlockData data = (BlockData) entry[1];
	                event.getWorld().getBlockAt(loc[0], loc[1], loc[2])
	                        .setBlockData(data, false);
	            }
	        }
    	}
    }

}
