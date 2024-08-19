package org.terraform.biome.cavepopulators;

import org.jetbrains.annotations.NotNull;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;

import java.util.Random;

public enum CaveClusterRegistry {
    LUSH(9527213,
            TConfig.c.BIOME_CAVE_LUSHCLUSTER_SEPARATION,
            (float)TConfig.c.BIOME_CAVE_LUSHCLUSTER_MAXPERTUB
    ), DRIPSTONE(5902907,
            TConfig.c.BIOME_CAVE_DRIPSTONECLUSTER_SEPARATION,
            (float)TConfig.c.BIOME_CAVE_DRIPSTONECLUSTER_MAXPERTUB
    ), CRYSTALLINE(4427781,
            TConfig.c.BIOME_CAVE_CRYSTALLINECLUSTER_SEPARATION,
            (float)TConfig.c.BIOME_CAVE_CRYSTALLINECLUSTER_MAXPERTUB
    ), FLUID(79183628, 40, 0.2f),
    ;

    final int hashSeed;
    final int separation;
    final float pertub;

    CaveClusterRegistry(int hashSeed, int separation, float pertub) {
        this.hashSeed = hashSeed;
        this.separation = separation;
        this.pertub = pertub;
    }

    public @NotNull AbstractCaveClusterPopulator getPopulator(@NotNull Random random) {
        return switch (this) {
            case LUSH -> new LushClusterCavePopulator(GenUtils.randInt(random,
                    TConfig.c.BIOME_CAVE_LUSHCLUSTER_MINSIZE,
                    TConfig.c.BIOME_CAVE_LUSHCLUSTER_MAXSIZE
            ), false);
            case DRIPSTONE -> new DripstoneClusterCavePopulator(GenUtils.randInt(random,
                    TConfig.c.BIOME_CAVE_DRIPSTONECLUSTER_MINSIZE,
                    TConfig.c.BIOME_CAVE_DRIPSTONECLUSTER_MAXSIZE
            ));
            case CRYSTALLINE -> new CrystallineClusterCavePopulator(GenUtils.randInt(random,
                    TConfig.c.BIOME_CAVE_CRYSTALLINECLUSTER_MINSIZE,
                    TConfig.c.BIOME_CAVE_CRYSTALLINECLUSTER_MAXSIZE
            ));
            case FLUID -> new CaveFluidClusterPopulator(GenUtils.randInt(random,
                    TConfig.c.BIOME_CAVE_CRYSTALLINECLUSTER_MINSIZE,
                    TConfig.c.BIOME_CAVE_CRYSTALLINECLUSTER_MAXSIZE
            ));
        };

    }

    public int getHashSeed() {
        return hashSeed;
    }

    public int getSeparation() {
        return separation;
    }

    public float getPertub() {
        return pertub;
    }
}
