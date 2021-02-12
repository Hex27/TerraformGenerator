package org.terraform.structure.mineshaft;

import org.bukkit.Material;

import java.util.Random;

public class BadlandsMineshaftPathPopulator extends MineshaftPathPopulator {
    public BadlandsMineshaftPathPopulator(Random rand) {
        super(rand);
    }

    @Override
    public Material[] getPathMaterial() {
        return new Material[] {
                Material.DARK_OAK_PLANKS,
                Material.DARK_OAK_SLAB,
                Material.DARK_OAK_PLANKS,
                Material.DARK_OAK_SLAB,
                Material.GRAVEL
        };
    }

    @Override
    public Material getFenceMaterial() {
        return Material.DARK_OAK_FENCE;
    }

    @Override
    public Material getSupportMaterial() {
        return Material.DARK_OAK_LOG;
    }
}
