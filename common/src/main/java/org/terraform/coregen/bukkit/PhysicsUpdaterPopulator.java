package org.terraform.coregen.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.SimpleLocation;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicsUpdaterPopulator extends BlockPopulator implements Listener{
	
	private static boolean flushIsQueued = false;
    //SimpleChunkLocation to a collection of simplelocations
    public static final @NotNull Map<SimpleChunkLocation, Collection<SimpleLocation>> cache = new ConcurrentHashMap<>();
    //private final TerraformWorld tw;

    public PhysicsUpdaterPopulator() {
        //this.tw = tw;
    	Bukkit.getPluginManager().registerEvents(this, TerraformGeneratorPlugin.get());
    }
    
    public static void pushChange(String world, @NotNull SimpleLocation loc) {
    	
    	if(!flushIsQueued && cache.size() > TConfigOption.DEVSTUFF_FLUSH_PATCHER_CACHE_FREQUENCY.getInt()) {
			flushIsQueued = true;
    		new BukkitRunnable() {
	    		@Override
				public void run() {
		    		flushChanges();		
		    		flushIsQueued = false;
				}
    		}.runTask(TerraformGeneratorPlugin.get());
    	}
    	
        SimpleChunkLocation scl = new SimpleChunkLocation(world, loc.getX(), loc.getY(),loc.getZ());
        if (!cache.containsKey(scl))
            cache.put(scl, new ArrayList<>());

        cache.get(scl).add(loc);
    }
    
    public static void flushChanges() {
    	if(cache.isEmpty())
    		return;
    	TerraformGeneratorPlugin.logger.info("[PhysicsUpdaterPopulator] Flushing repairs (" + cache.size() + " chunks)");
        ArrayList<SimpleChunkLocation> locs = new ArrayList<>(cache.keySet());
    	for(SimpleChunkLocation scl:locs) {
    		World w = Bukkit.getWorld(scl.getWorld());
    		if(w == null) continue;
    		if(w.isChunkLoaded(scl.getX(), scl.getZ())) {
    			Collection<SimpleLocation> changes = cache.remove(scl);
    	        if (changes != null) {
    	        	for (SimpleLocation entry : changes) {
    	        		Block target = w.getBlockAt(entry.getX(), entry.getY(), entry.getZ());
    	        		//Set block physics by calling setBlockData
    	        		//Note that this should not be used for complex blocks.
    	        		BlockData old = target.getBlockData();
    	        		TerraformGeneratorPlugin.logger.info("[PhysicsUpdaterPopulator] " + target.getLocation());
    	                target.setType(Material.AIR);
    	        		target.setBlockData(old, true);
    	        	}
    	        }
    		} else {
    			//Let the event handler do it
    			w.loadChunk(scl.getX(), scl.getZ());
    		}
    	}
    }
    
    @Override
    public void populate(@NotNull World world, Random random, @NotNull Chunk chunk) {
        SimpleChunkLocation scl = new SimpleChunkLocation(chunk);
		Collection<SimpleLocation> changes = cache.remove(scl);
        if (changes != null) {
        	//TerraformGeneratorPlugin.logger.info("[PhysicsUpdaterPopulator] Detected anomalous generation by NMS on " + scl + ". Running repairs on " + changes.size() + " blocks");
        	for (SimpleLocation entry : changes) {
        		Block target = world.getBlockAt(entry.getX(), entry.getY(), entry.getZ());
        		//Set block physics by calling setBlockData
        		//Note that this should not be used for complex blocks.
        		BlockData old = target.getBlockData();
        		target.setType(Material.AIR);
        		target.setBlockData(old, true);
        	}
        }
    }

//    @EventHandler
//    public void onChunkLoad(ChunkLoadEvent event) {
//        SimpleChunkLocation scl = new SimpleChunkLocation(event.getChunk());
//        Collection<SimpleLocation> changes = cache.remove(scl);
//        if (changes != null) {
//        	//TerraformGeneratorPlugin.logger.info("[PhysicsUpdaterPopulator] Detected anomalous generation by NMS on " + scl + ". Running repairs on " + changes.size() + " blocks");
//        	for (SimpleLocation entry : changes) {
//        		Block target = event.getWorld().getBlockAt(entry.getX(), entry.getY(), entry.getZ());
//        		//Set block physics by calling setBlockData
//        		//Note that this should not be used for complex blocks.
//        		BlockData old = target.getBlockData();
//        		target.setType(Material.AIR);
//        		target.setBlockData(old, true);
//        	}
//        }
//    }
//    
    @EventHandler
    public void onWorldUnload(@NotNull WorldUnloadEvent event) {
    	TerraformGeneratorPlugin.logger.info("[PhysicsUpdaterPopulator] Flushing repairs for " + event.getWorld().getName() + " (" + cache.size() + " chunks in cache)");
        
    	int processed = 0;
    	for(SimpleChunkLocation scl:cache.keySet()) {
        	if(!scl.getWorld().equals(event.getWorld().getName()))
        		continue;
        	Collection<SimpleLocation> changes = cache.remove(scl);
            if (changes != null) {
            	//TerraformGeneratorPlugin.logger.info("[PhysicsUpdaterPopulator] Detected anomalous generation by NMS on " + scl + ". Running repairs on " + changes.size() + " blocks");
            	for (SimpleLocation entry : changes) {
            		Block target = event.getWorld().getBlockAt(entry.getX(), entry.getY(), entry.getZ());
            		//Set block physics by calling setBlockData
            		//Note that this should not be used for complex blocks.
	        		BlockData old = target.getBlockData();
	        		target.setType(Material.AIR);
	        		target.setBlockData(old, true);
            	}
            }
	        
	        processed++;
	        if(processed % 20 == 0)
	        	TerraformGeneratorPlugin.logger.info("[PhysicsUpdaterPopulator] Processed " + processed + " more chunks");
    	}
    }

}
