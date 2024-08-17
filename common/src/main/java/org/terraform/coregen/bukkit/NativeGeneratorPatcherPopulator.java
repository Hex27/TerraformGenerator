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
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class NativeGeneratorPatcherPopulator extends BlockPopulator implements Listener{
	
	private static boolean flushIsQueued = false;
    //SimpleChunkLocation to a collection of location:blockdata entries marked for repair.
    public static final @NotNull Map<SimpleChunkLocation, Collection<Object[]>> cache = new ConcurrentHashMap<>();
    //public static Map<Material, Integer> cacheContents = new ConcurrentHashMap<>();
    //private final TerraformWorld tw;

    public NativeGeneratorPatcherPopulator() {
        //this.tw = tw;
    	Bukkit.getPluginManager().registerEvents(this, TerraformGeneratorPlugin.get());
    }
    
    public static void pushChange(String world, int x, int y, int z, BlockData data) {
    	
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
    	
        SimpleChunkLocation scl = new SimpleChunkLocation(world, x, y, z);
        if (!cache.containsKey(scl))
            cache.put(scl, new ArrayList<>());
//        cacheContents.put(data.getMaterial(), cacheContents.getOrDefault(data.getMaterial(),0)+1);
        cache.get(scl).add(new Object[]{
                new int[]{x, y, z},
                data
        });
    }
    
    public static void flushChanges() {
    	if(cache.isEmpty())
    		return;
    	TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Flushing repairs (" + cache.size() + " chunks), pushed by cache size");
//        for(Material mat:cacheContents.keySet())
//        {
//            TerraformGeneratorPlugin.logger.info(mat + " : " + cacheContents.get(mat));
//        }
//        cacheContents.clear();
        ArrayList<SimpleChunkLocation> locs = new ArrayList<>(cache.keySet());
    	for(SimpleChunkLocation scl:locs) {
    		World w = Bukkit.getWorld(scl.getWorld());
    		if(w == null) continue;
    		if(w.isChunkLoaded(scl.getX(), scl.getZ())) {
    			//TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher]   - Flushing changes to loaded chunk...");
				Collection<Object[]> changes = cache.remove(scl);
    	        if (changes != null) {
    	        	for (Object[] entry : changes) {
    	                int[] loc = (int[]) entry[0];
    	                BlockData data = (BlockData) entry[1];
    	                w.getBlockAt(loc[0], loc[1], loc[2])
    	                        .setBlockData(data, false);
    	            }
    	        }
    		} else {
    			//Let the event handler do it
    			//TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher]   - Loading a chunk to flush changes...");
    	        w.loadChunk(scl.getX(), scl.getZ());
    		}
    	}
    }
    
    @Override
    public void populate(@NotNull World world, Random random, @NotNull Chunk chunk) {
        SimpleChunkLocation scl = new SimpleChunkLocation(chunk);
        Collection<Object[]> changes = cache.remove(scl);
        if (changes != null) {
        	//TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Flushing repairs (" + cache.size() + " chunks), pushed by BlockPopulator");
            for (Object[] entry : changes) {
                int[] loc = (int[]) entry[0];
                BlockData data = (BlockData) entry[1];
                world.getBlockAt(loc[0], loc[1], loc[2])
                        .setBlockData(data, false);
            }
        }
    }

//    @EventHandler
//    public void onChunkUnload(ChunkUnloadEvent event) {
//        SimpleChunkLocation scl = new SimpleChunkLocation(event.getChunk());
//        Collection<Object[]> changes = cache.remove(scl);
//        if (changes != null) {
//        	//TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Flushing repairs for 1 chunk (" + scl.getX() + "," + scl.getZ() + "), pushed by chunkunloadevent");
//            for (Object[] entry : changes) {
//                int[] loc = (int[]) entry[0];
//                BlockData data = (BlockData) entry[1];
//                event.getChunk().getWorld().getBlockAt(loc[0], loc[1], loc[2])
//                        .setBlockData(data, false);
//            }
//        }
//    }

    @EventHandler
    public void onChunkLoad(@NotNull ChunkLoadEvent event) {
        SimpleChunkLocation scl = new SimpleChunkLocation(event.getChunk());
        Collection<Object[]> changes = cache.remove(scl);
        if (changes != null) {
        	//TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Flushing repairs for 1 chunk (" + scl.getX() + "," + scl.getZ() + "), pushed by chunkloadevent");
            for (Object[] entry : changes) {
                int[] loc = (int[]) entry[0];
                BlockData data = (BlockData) entry[1];
                event.getChunk().getWorld().getBlockAt(loc[0], loc[1], loc[2])
                        .setBlockData(data, false);
            }
        }
    }
    
    @EventHandler
    public void onWorldUnload(@NotNull WorldUnloadEvent event) {
    	TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Flushing repairs for " + event.getWorld().getName() + " (" + cache.size() + " chunks in cache), triggered by world unload");
        
    	int processed = 0;
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
	        
	        processed++;
	        if(processed % 20 == 0)
	        	TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Processed " + processed + "/" + cache.size() + " chunks");
    	}
    }

}
