package org.terraform.structure;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.structure.animalfarm.AnimalFarmPopulator;
import org.terraform.structure.farmhouse.FarmhousePopulator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class VillageHousePopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public Random getHashedRandom(TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(2291282, chunkX, chunkZ);
    }

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, ArrayList<BiomeBank> banks) {

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[] coords = getCoordsFromMegaChunk(tw, mc);
        if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {
            if (banks.contains(BiomeBank.LUKEWARM_OCEAN)
                    || banks.contains(BiomeBank.WARM_OCEAN)
                    || banks.contains(BiomeBank.OCEAN)
                    || banks.contains(BiomeBank.COLD_OCEAN)
                    || banks.contains(BiomeBank.FROZEN_OCEAN)
                    || banks.contains(BiomeBank.SWAMP)) {
                return false;
            } else {
                //If it is below sea level, DON'T SPAWN IT.
                if (HeightMap.getBlockHeight(tw, coords[0], coords[1]) > TerraformGenerator.seaLevel) {
                    if (banks.contains(BiomeBank.DESERT)
                            || banks.contains(BiomeBank.DESERT_MOUNTAINS)
                            || banks.contains(BiomeBank.BADLANDS)
                            || banks.contains(BiomeBank.BADLANDS_MOUNTAINS)
                            || banks.contains(BiomeBank.SNOWY_WASTELAND)
                            || banks.contains(BiomeBank.ICE_SPIKES)) {
                        return TConfigOption.STRUCTURES_ANIMALFARM_ENABLED.getBoolean();
                    } else if (banks.contains(BiomeBank.FOREST)
                            || banks.contains(BiomeBank.PLAINS)
                            || banks.contains(BiomeBank.TAIGA)
                            || banks.contains(BiomeBank.SAVANNA)
                            || banks.contains(BiomeBank.SNOWY_TAIGA)) {

                        return TConfigOption.STRUCTURES_FARMHOUSE_ENABLED.getBoolean();
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        ArrayList<BiomeBank> banks = GenUtils.getBiomesInChunk(tw, data.getChunkX(), data.getChunkZ());

        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());

        if (banks.contains(BiomeBank.LUKEWARM_OCEAN)
                || banks.contains(BiomeBank.WARM_OCEAN)
                || banks.contains(BiomeBank.OCEAN)
                || banks.contains(BiomeBank.COLD_OCEAN)
                || banks.contains(BiomeBank.FROZEN_OCEAN)
                || banks.contains(BiomeBank.SWAMP)) {
            //Ships
        } else {

            //If it is below sea level, DON'T SPAWN IT.
            int[] coords = getCoordsFromMegaChunk(tw, mc);
            if (GenUtils.getHighestGround(data, coords[0], coords[1]) > TerraformGenerator.seaLevel) {
                if (banks.contains(BiomeBank.DESERT)
                        || banks.contains(BiomeBank.DESERT_MOUNTAINS)
                        || banks.contains(BiomeBank.BADLANDS)
                        || banks.contains(BiomeBank.BADLANDS_MOUNTAINS)
                        || banks.contains(BiomeBank.SNOWY_WASTELAND)
                        || banks.contains(BiomeBank.ICE_SPIKES)) {
                    if (!TConfigOption.STRUCTURES_ANIMALFARM_ENABLED.getBoolean())
                        return;

                    new AnimalFarmPopulator().populate(tw, data);
                } else if (banks.contains(BiomeBank.FOREST)
                        || banks.contains(BiomeBank.PLAINS)
                        || banks.contains(BiomeBank.TAIGA)
                        || banks.contains(BiomeBank.SAVANNA)
                        || banks.contains(BiomeBank.SNOWY_TAIGA)) {

                    if (!TConfigOption.STRUCTURES_FARMHOUSE_ENABLED.getBoolean())
                        return;

                    new FarmhousePopulator().populate(tw, data);
                }
            }
        }
    }

    @Override
    public int[] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc) {
        return mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 2392224));
    }

    @Override
    public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
        MegaChunk mc = new MegaChunk(rawX, 0, rawZ);

        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                int[] loc = getCoordsFromMegaChunk(tw, mc.getRelative(nx, nz));
                double distSqr = Math.pow(loc[0] - rawX, 2) + Math.pow(loc[1] - rawZ, 2);
                if (distSqr < minDistanceSquared) {
                    minDistanceSquared = distSqr;
                    min = loc;
                }
            }
        }
        return min;
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_ANIMALFARM_ENABLED.getBoolean() || TConfigOption.STRUCTURES_FARMHOUSE_ENABLED.getBoolean();
    }
}
