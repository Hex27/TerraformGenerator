package org.terraform.structure.villagehouse;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.villagehouse.animalfarm.AnimalFarmPopulator;
import org.terraform.structure.villagehouse.farmhouse.FarmhousePopulator;
import org.terraform.structure.villagehouse.mountainhouse.MountainhousePopulator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class VillageHousePopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(2291282, chunkX, chunkZ);
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(
                tw.getHashedRand(chunkX, chunkZ, 12422),
                (int) (TConfigOption.STRUCTURES_VILLAGEHOUSE_SPAWNRATIO.getDouble() * 10000),
                10000
        );
    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, @NotNull BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[] coords = mc.getCenterBiomeSectionBlockCoords(); // getCoordsFromMegaChunk(tw, mc);
        if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {

            if (!biome.isDry()) {
                return false;
            }

            // If it is below sea level, DON'T SPAWN IT.
            if (HeightMap.getBlockHeight(tw, coords[0], coords[1]) > TerraformGenerator.seaLevel) {
                if (biome == (BiomeBank.DESERT) || biome == (BiomeBank.BADLANDS) || biome == (BiomeBank.ICE_SPIKES)) {
                    return TConfigOption.STRUCTURES_ANIMALFARM_ENABLED.getBoolean() && rollSpawnRatio(tw,
                            chunkX,
                            chunkZ);
                }
                else if (biome == (BiomeBank.SNOWY_TAIGA)
                         || biome == (BiomeBank.SNOWY_WASTELAND)
                         || biome == (BiomeBank.JUNGLE))
                {

                    return TConfigOption.STRUCTURES_FARMHOUSE_ENABLED.getBoolean() && rollSpawnRatio(tw,
                            chunkX,
                            chunkZ);
                }
                else if (biome == (BiomeBank.ROCKY_MOUNTAINS)) {

                    return TConfigOption.STRUCTURES_MOUNTAINHOUSE_ENABLED.getBoolean() && rollSpawnRatio(tw,
                            chunkX,
                            chunkZ);
                }
            }
        }
        return false;
    }

    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        if (!isEnabled()) {
            return;
        }

        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());

        // On ground, spawn dry village houses
        // int[] coords = mc.getCenterBiomeSectionBlockCoords(); // getCoordsFromMegaChunk(tw, mc);
        BiomeBank biome = mc.getCenterBiomeSection(tw).getBiomeBank();
        // if (GenUtils.getHighestGround(data, coords[0], coords[1]) > TerraformGenerator.seaLevel) {
        if (biome == (BiomeBank.DESERT) || biome == (BiomeBank.BADLANDS) || biome == (BiomeBank.ICE_SPIKES)) {
            if (!TConfigOption.STRUCTURES_ANIMALFARM_ENABLED.getBoolean()) {
                return;
            }

            new AnimalFarmPopulator().populate(tw, data);
        }
        else if (biome == (BiomeBank.SNOWY_TAIGA)
                 || biome == (BiomeBank.SNOWY_WASTELAND)
                 || biome == (BiomeBank.JUNGLE))
        {

            if (!TConfigOption.STRUCTURES_FARMHOUSE_ENABLED.getBoolean()) {
                return;
            }

            new FarmhousePopulator().populate(tw, data);
        }
        else if (biome == (BiomeBank.ROCKY_MOUNTAINS)) {

            if (!TConfigOption.STRUCTURES_MOUNTAINHOUSE_ENABLED.getBoolean()) {
                return;
            }

            new MountainhousePopulator().populate(tw, data);
        }
        // }
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.areStructuresEnabled() && (BiomeBank.isBiomeEnabled(BiomeBank.DESERT)
                                                        || BiomeBank.isBiomeEnabled(BiomeBank.BADLANDS)
                                                        || BiomeBank.isBiomeEnabled(BiomeBank.ICE_SPIKES)
                                                        || BiomeBank.isBiomeEnabled(BiomeBank.SNOWY_TAIGA)
                                                        || BiomeBank.isBiomeEnabled(BiomeBank.SNOWY_WASTELAND)
                                                        || BiomeBank.isBiomeEnabled(BiomeBank.JUNGLE)
                                                        || BiomeBank.isBiomeEnabled(BiomeBank.ROCKY_MOUNTAINS)) && (
                       TConfigOption.STRUCTURES_ANIMALFARM_ENABLED.getBoolean()
                       || TConfigOption.STRUCTURES_FARMHOUSE_ENABLED.getBoolean()
                       || TConfigOption.STRUCTURES_MOUNTAINHOUSE_ENABLED.getBoolean());
    }
}
