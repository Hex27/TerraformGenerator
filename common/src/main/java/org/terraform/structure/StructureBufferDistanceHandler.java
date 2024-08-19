package org.terraform.structure;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;

public class StructureBufferDistanceHandler {

    /**
     * Called from decorators to determine whether or not they
     * can place large trees and obstructive decorations, or if
     * they must make way for structures.
     */
    public static boolean canDecorateChunk(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {

        // Within radius
        if (Math.pow(chunkX * 16, 2) + Math.pow(chunkZ * 16, 2) < HeightMap.spawnFlatRadiusSquared) {
            return false;
        }

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        BiomeBank biome = mc.getCenterBiomeSection(tw).getBiomeBank();
        for (StructurePopulator structPop : StructureRegistry.getLargeStructureForMegaChunk(tw, mc)) {
            if (structPop == null) {
                continue;
            }

            if (!(structPop instanceof SingleMegaChunkStructurePopulator spop)) {
                continue;
            }
            int chunkBufferRadius = spop.getChunkBufferDistance();
            if (chunkBufferRadius <= 0) {
                continue;
            }
            // No need to account for strongholds, which have a different way of
            // checking spawn locations.

            // Grab the center chunk, where the structure will spawn
            int[] chunkCoords = mc.getCenterBiomeSectionChunkCoords();
            if (TConfigOption.areStructuresEnabled() && spop.canSpawn(tw, chunkCoords[0], chunkCoords[1], biome)) {
                // If the structure will spawn, calculate distance to it.
                int dist = (int) (Math.pow(chunkCoords[0] - chunkX, 2) + Math.pow(chunkCoords[1] - chunkZ, 2));
                if (Math.sqrt(dist) <= chunkBufferRadius) {
                    return false;
                }
            }
        }

        return true;
    }

}
