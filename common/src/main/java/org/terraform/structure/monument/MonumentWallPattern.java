package org.terraform.structure.monument;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.Wall;

public enum MonumentWallPattern {
    EYE, CROSS;

    public void apply(@NotNull Wall w) {
        if (this == EYE) {
            // Eye brow
            for (int i = 0; i <= 4; i++) {
                w.getUp(2).getLeft(i).setType(Material.DARK_PRISMARINE);
                w.getUp(2).getRight(i).setType(Material.DARK_PRISMARINE);
            }

            // Eye whites
            w.getLeft(3).setType(Material.DARK_PRISMARINE);
            w.getRight(3).setType(Material.DARK_PRISMARINE);
            w.getUp().getLeft(3).setType(Material.DARK_PRISMARINE);
            w.getUp().getRight(3).setType(Material.DARK_PRISMARINE);

            w.getDown(3).getLeft(3).setType(Material.DARK_PRISMARINE);
            w.getDown(3).getRight(3).setType(Material.DARK_PRISMARINE);

            w.getDown().getLeft(2).setType(Material.DARK_PRISMARINE);
            w.getDown().getRight(2).setType(Material.DARK_PRISMARINE);

            w.getDown(2).getLeft().setType(Material.DARK_PRISMARINE);
            w.getDown(2).getRight().setType(Material.DARK_PRISMARINE);
            w.getDown(2).setType(Material.DARK_PRISMARINE);

            w.getDown(3).setType(Material.DARK_PRISMARINE);

            w.getDown(2).getLeft(3).setType(Material.DARK_PRISMARINE);
            w.getDown(2).getRight(3).setType(Material.DARK_PRISMARINE);

            // Pupil
            w.setType(Material.DARK_PRISMARINE);
            w.getUp().setType(Material.DARK_PRISMARINE);
        }
        else if (this == CROSS) {
            // Sea lanterns
            w.setType(Material.SEA_LANTERN);
            w.getUp(2).setType(Material.SEA_LANTERN);
            w.getDown(2).setType(Material.SEA_LANTERN);
            w.getLeft(2).setType(Material.SEA_LANTERN);
            w.getRight(2).setType(Material.SEA_LANTERN);

            // Others
            w.getLeft(2).getUp(2).setType(Material.DARK_PRISMARINE);
            w.getLeft(2).getDown(2).setType(Material.DARK_PRISMARINE);
            w.getRight(2).getUp(2).setType(Material.DARK_PRISMARINE);
            w.getRight(2).getDown(2).setType(Material.DARK_PRISMARINE);
        }
    }
}
