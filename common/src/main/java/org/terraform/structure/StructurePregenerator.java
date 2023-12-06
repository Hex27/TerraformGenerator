package org.terraform.structure;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataSpigotAPI;
import org.terraform.coregen.populatordata.PopulatorDataStructurePregen;
import org.terraform.data.MegaChunk;
import org.terraform.data.MegaChunkKey;
import org.terraform.data.TerraformWorld;
import org.terraform.event.TerraformStructureSpawnEvent;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.stronghold.StrongholdPopulator;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will handle the java threads that queue structures to be
 * generated.
 * <br>
 * It does this by starting a new thread per structure requested, then
 * JOINING the thread in TerraformPopulator if needed.
 * <br>
 * Each thread will have its own special populatordata that handles
 * generating one structure
 */
public class StructurePregenerator {

    //Used by TerraformGenerator to give information to the structurepregenerator.
    private static final ConcurrentHashMap<ChunkCache, ChunkCache> READ_ONLY_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<MegaChunkKey, PopulatorDataStructurePregen> PREGENERATION_TASKS = new ConcurrentHashMap<>();

    //Mutated by StructurePregenRunnable
    public static final HashSet<MegaChunkKey> completed = new HashSet<>();

    public static void pushChunkCache(TerraformGenerator generator, ChunkCache cache){
        MegaChunk mc = new MegaChunk(cache.chunkX,cache.chunkZ);
        MegaChunkKey key = new MegaChunkKey(cache.tw,mc);

        if(completed.contains(key)) return; //Ignore completed structures

        //Always write from TerraformGenerator
        READ_ONLY_CACHE.put(cache,cache);

        //Check if the megachunk is registered under Pregeneration Tasks
        if(PREGENERATION_TASKS.containsKey(key)) return; //don't touch stuff that's already there

        PREGENERATION_TASKS.computeIfAbsent(key, (mck)->{

            BiomeBank biome = mc.getCenterBiomeSection(mck.getTw()).getBiomeBank();
            int[] chunkCoords = mc.getCenterBiomeSectionChunkCoords();

            for(SingleMegaChunkStructurePopulator spop : StructureRegistry.getLargeStructureForMegaChunk(mck.getTw(), mc)) {
                if(spop == null) continue;
                if(!spop.isEnabled()) continue;
                if(spop instanceof StrongholdPopulator) continue;
                if(spop.canSpawn(mck.getTw(), chunkCoords[0],chunkCoords[1], biome)) {
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            int[] blockCoords = mc.getCenterBiomeSectionBlockCoords();
                            Bukkit.getPluginManager().callEvent(new TerraformStructureSpawnEvent(blockCoords[0], blockCoords[1], spop.getClass().getName()));
                        }
                    }.runTask(TerraformGeneratorPlugin.get());
                    return new PopulatorDataStructurePregen(mck, chunkCoords[0],chunkCoords[1], spop, READ_ONLY_CACHE, generator);
                }
            }
            return null;
        });
    }


    public static void flushChanges(PopulatorDataSpigotAPI data) {
        //Flush out changes for this chunk
        MegaChunkKey mck = new MegaChunkKey(data.getTerraformWorld(),new MegaChunk(data.getChunkX(),data.getChunkZ()));
        PopulatorDataStructurePregen pregen = PREGENERATION_TASKS.get(mck);
        if(pregen == null){
            markFinished(mck);
            return;
        }
        //If it is not ready, spinlock
        pregen.spinlock();
        pregen.flush(data);
    }

    public static void markFinished(MegaChunkKey mck)
    {
        PREGENERATION_TASKS.remove(mck);
    }
    public static void markFinished(TerraformWorld tw, MegaChunk mc)
    {
        //That's a lotta mega chunks
        if(completed.size() > 30) completed.clear();

        StructurePregenerator.completed.add(new MegaChunkKey(tw,mc));
        List<int[]> scls = mc.getChunkCoordinates();

        //Possibly fucking costly
        scls.forEach((a)->READ_ONLY_CACHE.remove(new ChunkCache(tw,a[0] << 4,0,a[1]<<4)));
    }

}
