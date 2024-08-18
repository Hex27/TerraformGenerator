package org.terraform.structure.trialchamber;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.VanillaStructurePopulator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class TrialChamberPopulator extends VanillaStructurePopulator {
    public TrialChamberPopulator() {
        super("trial_chambers");
    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
        return rollSpawnRatio(tw,chunkX,chunkZ);
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
    }


    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(670191632, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_TRIALCHAMBER_ENABLED.getBoolean();
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 19650),
                (int) (TConfigOption.STRUCTURES_TRIALCHAMBER_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }
}
