package org.terraform.biome;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public abstract class BiomeHandler {
    public abstract boolean isOcean();

    public abstract Biome getBiome();

    //public abstract int getHeight(int x, int z, Random rand);

    public abstract Material[] getSurfaceCrust(Random rand);

    public abstract void populateSmallItems(TerraformWorld tw, Random random, PopulatorDataAbstract data);
    
    public abstract void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data);

    /**
     * @return The used handler for transforming
     * the terrain. If handler uses another
     * handler's transform function, return that.
     * This is to ensure that same transform
     * function is called only once per chunk.
     */
    public BiomeHandler getTransformHandler() {
        return null;
    }

    // Populate event but for the terrain.
    public void transformTerrain(TerraformWorld tw, Random random, ChunkGenerator.ChunkData chunk, int chunkX, int chunkZ) { /* Do nothing by default */ }
}
