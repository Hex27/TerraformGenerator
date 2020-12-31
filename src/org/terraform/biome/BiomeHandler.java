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

    public abstract void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data);

    // Populate event but for the terrain.
    public void transformTerrain(TerraformWorld tw, Random random, ChunkGenerator.ChunkData chunk, int chunkX, int chunkZ) { /* Do nothing by default */ }
}
