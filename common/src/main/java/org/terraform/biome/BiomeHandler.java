package org.terraform.biome;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
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
    
    
    //By default, use the normal height map.
    //Omit mountain and sea calculations - they're not necessary.
    public double calculateHeight(TerraformWorld tw, int x, int z) {
    	
        double height = HeightMap.CORE.getHeight(tw, x, z);

        if (height > HeightMap.defaultSeaLevel + 4) {
            height += HeightMap.ATTRITION.getHeight(tw, x, z);
        } else {
            height += HeightMap.ATTRITION.getHeight(tw, x, z) * 0.8;
        }

        //If the height is too high, just force it to smooth out
        if (height > 200) height = 200 + (height - 200) * 0.5;
        if (height > 230) height = 230 + (height - 230) * 0.3;
        if (height > 240) height = 240 + (height - 240) * 0.1;
        if (height > 250) height = 250 + (height - 250) * 0.05;

        //River Depth
        double depth = HeightMap.RIVER.getHeight(tw, x, z);
        
        //Cut off negative values
        depth = depth < 0 ? 0 : depth;

        //Normal scenario: Shallow area
        if (height - depth >= TerraformGenerator.seaLevel - 15) {
            height -= depth;

            //Fix for underwater river carving: Don't carve deeply
        } else if (height > TerraformGenerator.seaLevel - 15 && height - depth < TerraformGenerator.seaLevel - 15) {
            height = TerraformGenerator.seaLevel - 15;
        }

        if (HeightMap.heightAmplifier != 1f && height > TerraformGenerator.seaLevel)
        	height += HeightMap.heightAmplifier * (height - TerraformGenerator.seaLevel);

        return height;
    }
}
