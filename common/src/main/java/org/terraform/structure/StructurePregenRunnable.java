package org.terraform.structure;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataStructurePregen;
import org.terraform.data.MegaChunk;
import org.terraform.data.MegaChunkKey;
import org.terraform.data.TerraformWorld;
import org.terraform.event.TerraformStructureSpawnEvent;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.concurrent.ConcurrentHashMap;

public class StructurePregenRunnable implements Runnable {

    private final TerraformWorld tw;
    private final MegaChunk mc;
    private final PopulatorDataStructurePregen data;
    private final StructurePopulator spop;
    private final ConcurrentHashMap<ChunkCache, ChunkCache> readOnlyCache;
    private final TerraformGenerator generator;
    volatile boolean hasFinished = false;

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
            PregenChunkData pcd = new PregenChunkData(); //This is a shell class that discards all data
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
                        //The null is on purpose. Please do crash if you try to use that shit
                        generator.generateNoise(tw ,targetX, targetZ, pcd, key);
                        generator.generateCaves(tw ,targetX, targetZ, pcd, key);
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

    private class PregenChunkData implements ChunkGenerator.ChunkData{
        //We only care about this one.
        //x,z in [0,15]
        @Override
        public void setBlock(int x, int y, int z, @NotNull Material material) {
        }
        @Override
        public void setBlock(int i, int i1, int i2, @NotNull BlockData blockData) {

        }

        @Override
        public int getMinHeight() {
            return 0; //idc
        }

        @Override
        public int getMaxHeight() {
            return 0; //idc
        }

        @NotNull
        @Override
        public Material getType(int i, int i1, int i2) {
            return null;
        }

        @NotNull
        @Override
        public BlockData getBlockData(int i, int i1, int i2) {
            return null;
        }

        //Ignore these.
        @NotNull
        @Override
        public Biome getBiome(int i, int i1, int i2) {
            return null;
        }
        @Override
        public void setBlock(int i, int i1, int i2, @NotNull MaterialData materialData) {

        }

        @Override
        public void setRegion(int i, int i1, int i2, int i3, int i4, int i5, @NotNull Material material) {

        }

        @Override
        public void setRegion(int i, int i1, int i2, int i3, int i4, int i5, @NotNull MaterialData materialData) {

        }

        @Override
        public void setRegion(int i, int i1, int i2, int i3, int i4, int i5, @NotNull BlockData blockData) {

        }

        @NotNull
        @Override
        public MaterialData getTypeAndData(int i, int i1, int i2) {
            return null;
        }

        @Override
        public byte getData(int i, int i1, int i2) {
            return 0;
        }
    }
}
