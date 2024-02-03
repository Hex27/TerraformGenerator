package org.terraform.biome.cavepopulators;

import java.util.Random;

import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;

public enum CaveClusterRegistry {
	LUSH(
			9527213,
			TConfigOption.BIOME_CAVE_LUSHCLUSTER_SEPARATION.getInt(), 
			TConfigOption.BIOME_CAVE_LUSHCLUSTER_MAXPERTUB.getFloat()
			),
	DRIPSTONE(
			5902907,
			TConfigOption.BIOME_CAVE_DRIPSTONECLUSTER_SEPARATION.getInt(), 
			TConfigOption.BIOME_CAVE_DRIPSTONECLUSTER_MAXPERTUB.getFloat()
			),
	CRYSTALLINE(
			4427781,
			TConfigOption.BIOME_CAVE_CRYSTALLINECLUSTER_SEPARATION.getInt(), 
			TConfigOption.BIOME_CAVE_CRYSTALLINECLUSTER_MAXPERTUB.getFloat()
			),
    FLUID(
            79183628,
            40,
            0.2f
    ),
	;
	
	int hashSeed;
	int separation;
	float pertub;
	CaveClusterRegistry(int hashSeed, int separation, float pertub){
		this.hashSeed = hashSeed;
		this.separation = separation;
		this.pertub = pertub;
	}
	
	public AbstractCaveClusterPopulator getPopulator(Random random) {
        return switch(this) {
            case LUSH -> new LushClusterCavePopulator(
                    GenUtils.randInt(random,
                            TConfigOption.BIOME_CAVE_LUSHCLUSTER_MINSIZE.getInt(),
                            TConfigOption.BIOME_CAVE_LUSHCLUSTER_MAXSIZE.getInt()),
                    false);
            case DRIPSTONE -> new DripstoneClusterCavePopulator(
                    GenUtils.randInt(random,
                            TConfigOption.BIOME_CAVE_DRIPSTONECLUSTER_MINSIZE.getInt(),
                            TConfigOption.BIOME_CAVE_DRIPSTONECLUSTER_MAXSIZE.getInt()));
            case CRYSTALLINE -> new CrystallineClusterCavePopulator(
                    GenUtils.randInt(random,
                            TConfigOption.BIOME_CAVE_CRYSTALLINECLUSTER_MINSIZE.getInt(),
                            TConfigOption.BIOME_CAVE_CRYSTALLINECLUSTER_MAXSIZE.getInt()));
            case FLUID -> new CaveFluidClusterPopulator(
                    GenUtils.randInt(random,
                            TConfigOption.BIOME_CAVE_CRYSTALLINECLUSTER_MINSIZE.getInt(),
                            TConfigOption.BIOME_CAVE_CRYSTALLINECLUSTER_MAXSIZE.getInt()));
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
