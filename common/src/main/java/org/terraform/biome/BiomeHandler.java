package org.terraform.biome;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public abstract class BiomeHandler {
    public abstract boolean isOcean();

    public @NotNull CustomBiomeType getCustomBiome() {
        return CustomBiomeType.NONE;
    }

    public abstract Biome getBiome();

    // public abstract int getHeight(int x, int z, Random rand);

    public abstract Material[] getSurfaceCrust(Random rand);

    /**
     * No, you are NOT fucking allowed to change the world height here.
     * If you fucking change it, I will come after you personally.
     * Your internet anonymity will not save you
     * <br>
     * The intended use of this method is to populate stuff that really
     * only need to check their current column. The stuff
     * being generated should NOT be solid, as it will tamper with
     * world height, or mess with structure placement. TerraformGenerator
     * will ensure that the correct biomes call this method. EXTINGUISH your
     * use of getHighestGround, don't you call it here.
     * <br>
     * This CAN access adjacent blocks, it is called inside the 3x3 chunk zone.
     * <br>This CAN be used to replace existing solid blocks
     * <br>
     * Structure exclusion zones does not stop this method, so structures
     * will have to get rid of the stuff placed by this method
     *
     * @param surfaceY cached height from TerraformGenerator
     */
    public abstract void populateSmallItems(TerraformWorld tw,
                                            Random random,
                                            int rawX,
                                            int surfaceY,
                                            int rawZ,
                                            PopulatorDataAbstract data);

    public abstract void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data);

    public int getMaxHeightForCaves(@NotNull TerraformWorld tw, int x, int z) {
        return tw.maxY;
    }

    /**
     * @return The used handler for transforming
     * the terrain. If handler uses another
     * handler's transform function, return that.
     * This is to ensure that same transform
     * function is called only once per chunk.
     */
    public @Nullable BiomeHandler getTransformHandler() {
        return null;
    }

    /**
     * This is used for higher-resolution transformations with access to
     * materials AFTER height map blurring.
     * <br>
     * This method DOES NOT need to check biome, it is already checked
     * in the generator.
     * <br>
     * Purify into just X,Z queries, as TerraformGenerator is ALREADY iterating
     * through x,z. There's no need for a nested one here.
     *
     * @param x [0-15] internal chunk coords
     * @param z [0-15] internal chunk coords
     */
    public void transformTerrain(ChunkCache cache,
                                 TerraformWorld tw,
                                 Random random,
                                 ChunkGenerator.ChunkData chunk,
                                 int x,
                                 int z,
                                 int chunkX,
                                 int chunkZ)
    {
        // Do nothing by default.
    }

    // Beach type. This will be used instead if the height is too close to sea level.
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.SANDY_BEACH;
    }

    // River type. This will be used instead if the heightmap got carved into a river.
    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.RIVER;
    }

    // By default, use the normal height map.
    // Omit mountain and sea calculations - they're not necessary.
    public double calculateHeight(TerraformWorld tw, int x, int z) {
        return HeightMap.CORE.getHeight(tw, x, z);
    }

    public boolean forceDefaultToBeach() {
        return false;
    }
}
