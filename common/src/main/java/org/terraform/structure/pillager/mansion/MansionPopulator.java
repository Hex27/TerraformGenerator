package org.terraform.structure.pillager.mansion;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MansionPopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(717281012, chunkX, chunkZ);
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(
                tw.getHashedRand(chunkX, chunkZ, 99572),
                (int) (TConfig.c.STRUCTURES_MANSION_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }

        // Enforce minimum distance
        if (Math.pow(chunkX * 16, 2) + Math.pow(chunkZ * 16, 2)
            < Math.pow(TConfig.c.STRUCTURES_MANSION_MINDISTANCE, 2))
        {
            return false;
        }

        // Mansions must spawn. Dark forests are rare enough. Ignore ground height.
        if (biome == (BiomeBank.DARK_FOREST)) {
            return rollSpawnRatio(tw, chunkX, chunkZ);
        }
        return false;
    }

    @Override
    public void populate(TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        if (!isEnabled()) {
            return;
        }

        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());


        // If it is below sea level, DON'T SPAWN IT.
        int[] coords = mc.getCenterBiomeSectionBlockCoords(); // getCoordsFromMegaChunk(tw, mc);
        int y = GenUtils.getHighestGround(data, coords[0], coords[1]);
        if (y < TerraformGenerator.seaLevel) {
            y = TerraformGenerator.seaLevel;
        }


        MansionJigsawBuilder builder = new MansionJigsawBuilder(
                TConfig.c.STRUCTURES_MANSION_SIZE,
                TConfig.c.STRUCTURES_MANSION_SIZE,
                data,
                coords[0],
                y,
                coords[1]
        );
        builder.generate(new Random());
        builder.build(new Random());

    }

    @Override
    public int getChunkBufferDistance() {
        return TConfig.c.STRUCTURES_MANSION_CHUNK_EXCLUSION_ZONE;
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled()
               && BiomeBank.isBiomeEnabled(BiomeBank.DARK_FOREST)
               && TConfig.c.STRUCTURES_MANSION_ENABLED;
    }
}
