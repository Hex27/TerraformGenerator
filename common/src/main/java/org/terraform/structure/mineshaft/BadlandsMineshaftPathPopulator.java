package org.terraform.structure.mineshaft;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BadlandsMineshaftPathPopulator extends MineshaftPathPopulator {
    public BadlandsMineshaftPathPopulator(Random rand) {
        super(rand);
    }

    @Override
    public Material @NotNull [] getPathMaterial() {
        return new Material[] {
                Material.DARK_OAK_PLANKS,
                Material.DARK_OAK_SLAB,
                Material.DARK_OAK_PLANKS,
                Material.DARK_OAK_SLAB,
                Material.GRAVEL
        };
    }

    @Override
    public @NotNull Material getFenceMaterial() {
        return Material.DARK_OAK_FENCE;
    }

    @Override
    public @NotNull Material getSupportMaterial() {
        return Material.DARK_OAK_LOG;
    }
}
