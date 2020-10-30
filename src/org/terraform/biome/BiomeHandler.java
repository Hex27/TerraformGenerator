package org.terraform.biome;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.Random;

public abstract class BiomeHandler {
    public abstract boolean isOcean();

    public abstract Biome getBiome();

    //public abstract int getHeight(int x, int z, Random rand);

    public abstract Material[] getSurfaceCrust(Random rand);

    public abstract void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data);
}
