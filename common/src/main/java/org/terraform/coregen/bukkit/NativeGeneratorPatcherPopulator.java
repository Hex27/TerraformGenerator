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
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;

import java.util.*;

public class NativeGeneratorPatcherPopulator extends BlockPopulator implements Listener {

    // SimpleChunkLocation to a collection of location:blockdata entries marked for repair.
    private static final @NotNull Map<SimpleChunkLocation, @NotNull Collection<FlushCacheEntry>> cache = new HashMap<>();

    public NativeGeneratorPatcherPopulator() {
        // this.tw = tw;
        Bukkit.getPluginManager().registerEvents(this, TerraformGeneratorPlugin.get());
    }

    public static void pushChange(String world, int x, int y, int z, BlockData data) {
        synchronized (cache){
            if (cache.size() > TConfig.c.DEVSTUFF_FLUSH_PATCHER_CACHE_FREQUENCY) {
                flushChanges();
            }

            SimpleChunkLocation scl = new SimpleChunkLocation(world, x, y, z);
            Collection<FlushCacheEntry> cached = cache.getOrDefault(scl,new ArrayList<>());
            //        cacheContents.put(data.getMaterial(), cacheContents.getOrDefault(data.getMaterial(),0)+1);
            cached.add(new FlushCacheEntry(x,y,z,data));
            cache.put(scl, cached);
        }
    }

    public static void syncAndFlush(){
        synchronized (cache){
            flushChanges();
        }
    }

    //Should only be called in a wrapped synchronized block
    private static void flushChanges() {
        if (cache.isEmpty()) {
            return;
        }
        TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Flushing repairs ("
                                             + cache.size()
                                             + " chunks), pushed by cache size");
        ArrayList<SimpleChunkLocation> locs = new ArrayList<>(cache.keySet());
        for (SimpleChunkLocation scl : locs) {
            World w = Bukkit.getWorld(scl.getWorld());
            if (w == null) {
                continue;
            }
            Collection<FlushCacheEntry> changes = cache.remove(scl);
            if (changes != null) {
                TerraformGeneratorPlugin.taskScheduler.execAsyncRegion(w,
                        scl.getX(), scl.getZ(),
                        ()->{
                            for (FlushCacheEntry entry : changes) {
                                w.getBlockAt(entry.x,entry.y,entry.z).setBlockData(entry.data, false);
                            }
                        });
            }
        }
    }

    //This method uses the deprecated bukkit populate because it comes after the
    // normal API's populate
    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk chunk) {

        SimpleChunkLocation scl = new SimpleChunkLocation(chunk);
        Collection<FlushCacheEntry> changes;
        synchronized (cache){
            changes = cache.remove(scl);
        }
        if (changes != null) {
            // TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Flushing repairs (" + cache.size() + " chunks), pushed by BlockPopulator");
            for (FlushCacheEntry entry : changes) {
                world.getBlockAt(entry.x,entry.y,entry.z).setBlockData(entry.data, false);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(@NotNull ChunkLoadEvent event) {
        SimpleChunkLocation scl = new SimpleChunkLocation(event.getChunk());
        Collection<FlushCacheEntry> changes;
        synchronized (cache){
            changes = cache.remove(scl);
        }
        if (changes != null) {
            // TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Flushing repairs (" + cache.size() + " chunks), pushed by BlockPopulator");
            for (FlushCacheEntry entry : changes) {
                event.getChunk().getWorld().getBlockAt(entry.x,entry.y,entry.z).setBlockData(entry.data, false);
            }
        }
    }

    @EventHandler
    public void onWorldUnload(@NotNull WorldUnloadEvent event) {
        TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Flushing repairs for "
                                             + event.getWorld()
                                                    .getName()
                                             + " ("
                                             + cache.size()
                                             + " chunks in cache), triggered by world unload");
        synchronized (cache){

            int processed = 0;
            for (SimpleChunkLocation scl : cache.keySet()) {
                if (!scl.getWorld().equals(event.getWorld().getName())) {
                    continue;
                }
                Collection<FlushCacheEntry> changes = cache.get(scl);
                if (changes != null) {
                    for (FlushCacheEntry entry : changes) {
                        event.getWorld().getBlockAt(entry.x,entry.y,entry.z).setBlockData(entry.data, false);
                    }
                }

                processed++;
                if (processed % 20 == 0) {
                    TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Processed "
                                                         + processed
                                                         + "/"
                                                         + cache.size()
                                                         + " chunks");
                }
            }
        }
    }

    private record FlushCacheEntry(int x, int y, int z, BlockData data){
    }

}
