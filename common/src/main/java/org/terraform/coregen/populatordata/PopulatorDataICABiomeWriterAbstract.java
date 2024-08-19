package org.terraform.coregen.populatordata;

import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.custombiomes.CustomBiomeType;

public abstract class PopulatorDataICABiomeWriterAbstract extends PopulatorDataICAAbstract {

    public abstract void setBiome(int rawX, int rawY, int rawZ, CustomBiomeType cbt, Biome fallback);

    public void setBiome(int rawX, int rawY, int rawZ, @NotNull BiomeBank biomebank)
    {
        setBiome(rawX, rawY, rawZ, biomebank.getHandler().getCustomBiome(), biomebank.getHandler().getBiome());
    }

    public abstract void setBiome(int rawX, int rawY, int rawZ, Biome biome);

}
