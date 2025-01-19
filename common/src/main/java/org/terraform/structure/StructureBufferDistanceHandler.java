package org.terraform.structure;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;

public class StructureBufferDistanceHandler {

    /**
     * Called from decorators to determine whether or not they
     * can place large trees and obstructive decorations, or if
     * they must make way for structures.
     *
     * @return index 0 is Decoration buffer, index 1 is cave cluster buffer
     */
    public static boolean[] canDecorateChunk(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {

        // Within radius, no surface decorations, but there can be cave decorations
        if (Math.pow(chunkX * 16, 2) + Math.pow(chunkZ * 16, 2) < HeightMap.spawnFlatRadiusSquared) {
            return new boolean[]{false,true};
        }

        boolean[] canDecorate = new boolean[]{true,true};
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
            if (chunkBufferRadius <= 0 && spop.getCaveClusterBufferDistance() <= 0) {
                continue;
            }
            // No need to account for strongholds, which have a different way of
            // checking spawn locations.

            // Grab the center chunk, where the structure will spawn
            int[] chunkCoords = mc.getCenterBiomeSectionChunkCoords();
            if (TConfig.areStructuresEnabled() && spop.canSpawn(tw, chunkCoords[0], chunkCoords[1], biome)) {
                // If the structure will spawn, calculate distance to it.
                int dist = (int) (Math.pow(chunkCoords[0] - chunkX, 2) + Math.pow(chunkCoords[1] - chunkZ, 2));
                double rootedDist = Math.max(Math.sqrt(dist),0.002); //nonzero
                canDecorate[0] &= rootedDist > chunkBufferRadius;
                canDecorate[1] &= rootedDist > spop.getCaveClusterBufferDistance();
                //Short out if both are already false
                if(!canDecorate[0] && !canDecorate[1]) return canDecorate;
            }
        }

        return canDecorate;
    }

}
