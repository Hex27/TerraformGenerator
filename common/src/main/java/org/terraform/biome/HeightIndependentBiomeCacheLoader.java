package org.terraform.biome;

import com.google.common.cache.CacheLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.data.TWSimpleLocation;
import org.terraform.data.TerraformWorld;

public class HeightIndependentBiomeCacheLoader extends CacheLoader<TWSimpleLocation, BiomeBank> {

    @Override
    public @Nullable BiomeBank load(@NotNull TWSimpleLocation loc) {

        // This optimisation doesn't work here. Many aesthetic options rely on
        // the fact that this is block-accurate. Calculating once per 4x4 blocks
        // creates obvious ugly 4x4 artifacts
        // x = (x >> 2) << 2; z = (z >> 2) << 2;

        BiomeSection mostDominant = BiomeSection.getMostDominantSection(loc.tw(), loc.x(), loc.z());
        return mostDominant.getBiomeBank();
    }

}
