package org.terraform.structure;

import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataStructurePregen;
import org.terraform.data.MegaChunk;
import org.terraform.data.MegaChunkKey;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.concurrent.ConcurrentHashMap;

public class StructurePregenRunnable extends Thread {

    private final TerraformWorld tw;
    private final MegaChunk mc;
    private final PopulatorDataStructurePregen data;
    private final StructurePopulator spop;
    private final ConcurrentHashMap<ChunkCache, ChunkCache> readOnlyCache;
    private final TerraformGenerator generator;
    public volatile boolean hasFinished = false;

    public StructurePregenRunnable(TerraformWorld tw, MegaChunk mc, PopulatorDataStructurePregen data, StructurePopulator spop, ConcurrentHashMap<ChunkCache, ChunkCache> readOnlyCache, TerraformGenerator generator) {
        this.tw = tw;
        this.mc = mc;
        this.data = data;
        this.spop = spop;
        this.readOnlyCache = readOnlyCache;
        this.generator = generator;
    }

    @Override
    public void run() {
        if(spop != null)
        {
            /*
            Begin by ensuring that the boundaries are generated and ready for use
             */
            MegaChunkKey mck = new MegaChunkKey(tw,mc);
            TerraformGeneratorPlugin.logger.info("MC[" + mck.hashCode() + "," + mc.getX() + "," + mc.getZ() + "] Preparing to generate " + spop.getClass().getName() + " at chunk: " + data.getChunkX() + "," + data.getChunkZ());
            int boundary = spop.getPregenBoundaryRadius();
            for(int nx = -boundary; nx <= boundary; nx++)
                for(int nz = -boundary; nz <= boundary; nz++)
                {
                    final int targetX = nx+data.getChunkX();
                    final int targetZ = nz+data.getChunkZ();
                    ChunkCache query = new ChunkCache(tw,targetX<<4,0,targetZ<<4);
                    readOnlyCache.computeIfAbsent(query, (key)->{
                        key.initInternalCache();
                        generator.buildFilledCache(tw ,targetX, targetZ, key);
                        return key;
                    });
                }

            TerraformGeneratorPlugin.logger.info("MC[" + mck.hashCode() + "," +mc.getX() + "," + mc.getZ() + "] Finished boundary, generating " + spop.getClass().getName() + " at chunk: " + data.getChunkX() + "," + data.getChunkZ());
            spop.populate(tw, data);
        }else
            TerraformGeneratorPlugin.logger.info("Skipping structuregen (spop null) for megachunk " + mc.getX() + "," + mc.getZ());

        //DONE. Tell Pregenerator to flush cache data away as it's not needed anymore
        hasFinished = true;
        StructurePregenerator.markFinished(tw,mc);
    }

//    @Override
//    public void cancel(){
//        //Wacky af spinlock
//        //Would be hilarious if someone changed the bukkit
//        //scheduler and breaks this randomly.
//        //would be great to debug
//        while(!hasFinished) Thread.onSpinWait();
//    }
}
