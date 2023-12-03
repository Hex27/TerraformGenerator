package org.terraform.biome;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public abstract class BiomeHandler {
    public abstract boolean isOcean();

    public CustomBiomeType getCustomBiome() {
    	return CustomBiomeType.NONE;
    }
    public abstract Biome getBiome();

    //public abstract int getHeight(int x, int z, Random rand);

    public abstract Material[] getSurfaceCrust(Random rand);

    public abstract void populateSmallItems(TerraformWorld tw, Random random, PopulatorDataAbstract data);
    
    public abstract void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data);

    public int getMaxHeightForCaves(TerraformWorld tw, int x, int z) {
    	return tw.maxY;
    }
    
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

    /**
     * This is used for higher-resolution transformations with access to
     * materials AFTER height map blurring.
     * @param heightChanges Write out a 16x16 array of UPDATED height compared to
     * base height for caching.
     * It is an array of signed shorts because it will never exceed [-30k,30k].
     * This must be shorts to facilitate caching (smaller memory usage)
     * <br><br>
     * No change is signalled by Short.MIN_VALUE.
     */
    public void transformTerrain(short[][] heightChanges, TerraformWorld tw, Random random, ChunkGenerator.ChunkData chunk, int chunkX, int chunkZ) {
        //Do nothing by default.
    }

    //Beach type. This will be used instead if the height is too close to sea level.
    public BiomeBank getBeachType() {
    	return BiomeBank.SANDY_BEACH;
    }
    
    //River type. This will be used instead if the heightmap got carved into a river.
    public BiomeBank getRiverType() {
    	return BiomeBank.RIVER;
    }
    
    //By default, use the normal height map.
    //Omit mountain and sea calculations - they're not necessary.
    public double calculateHeight(TerraformWorld tw, int x, int z) {
        return HeightMap.CORE.getHeight(tw, x, z);
    }
    
    public boolean forceDefaultToBeach() {
    	return false;
    }
}
