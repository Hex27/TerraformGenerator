package org.terraform.structure.monument;

import org.bukkit.Material;
import org.terraform.data.Wall;

public enum MonumentWallPattern {
    EYE,
    CROSS;

    public void apply(Wall w) {
        if (this == EYE) {
            //Eye brow
            for (int i = 0; i <= 4; i++) {
                w.getRelative(0, 2, 0).getLeft(i).setType(Material.DARK_PRISMARINE);
                w.getRelative(0, 2, 0).getRight(i).setType(Material.DARK_PRISMARINE);
            }

            //Eye whites
            w.getLeft(3).setType(Material.DARK_PRISMARINE);
            w.getRight(3).setType(Material.DARK_PRISMARINE);
            w.getRelative(0, 1, 0).getLeft(3).setType(Material.DARK_PRISMARINE);
            w.getRelative(0, 1, 0).getRight(3).setType(Material.DARK_PRISMARINE);

            w.getRelative(0, -3, 0).getLeft(3).setType(Material.DARK_PRISMARINE);
            w.getRelative(0, -3, 0).getRight(3).setType(Material.DARK_PRISMARINE);

            w.getRelative(0, -1, 0).getLeft(2).setType(Material.DARK_PRISMARINE);
            w.getRelative(0, -1, 0).getRight(2).setType(Material.DARK_PRISMARINE);

            w.getRelative(0, -2, 0).getLeft().setType(Material.DARK_PRISMARINE);
            w.getRelative(0, -2, 0).getRight().setType(Material.DARK_PRISMARINE);
            w.getRelative(0, -2, 0).setType(Material.DARK_PRISMARINE);

            w.getRelative(0, -3, 0).setType(Material.DARK_PRISMARINE);

            w.getRelative(0, -2, 0).getLeft(3).setType(Material.DARK_PRISMARINE);
            w.getRelative(0, -2, 0).getRight(3).setType(Material.DARK_PRISMARINE);

            //Pupil
            w.setType(Material.DARK_PRISMARINE);
            w.getRelative(0, 1, 0).setType(Material.DARK_PRISMARINE);
        } else if (this == CROSS) {
            //Sea lanterns
            w.setType(Material.SEA_LANTERN);
            w.getRelative(0, 2, 0).setType(Material.SEA_LANTERN);
            w.getRelative(0, -2, 0).setType(Material.SEA_LANTERN);
            w.getLeft(2).setType(Material.SEA_LANTERN);
            w.getRight(2).setType(Material.SEA_LANTERN);

            //Others
            w.getLeft(2).getRelative(0, 2, 0).setType(Material.DARK_PRISMARINE);
            w.getLeft(2).getRelative(0, -2, 0).setType(Material.DARK_PRISMARINE);
            w.getRight(2).getRelative(0, 2, 0).setType(Material.DARK_PRISMARINE);
            w.getRight(2).getRelative(0, -2, 0).setType(Material.DARK_PRISMARINE);
        }
    }
}
