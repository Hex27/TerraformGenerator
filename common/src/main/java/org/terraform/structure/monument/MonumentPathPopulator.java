package org.terraform.structure.monument;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.Wall;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MonumentPathPopulator extends PathPopulatorAbstract {
    final Random rand;
    final MonumentDesign design;
    private boolean light = true;

    public MonumentPathPopulator(MonumentDesign design, Random rand) {
        this.rand = rand;
        this.design = design;
    }

    @Override
    public void populate(@NotNull PathPopulatorData ppd) {
        Wall w = new Wall(ppd.base, ppd.dir);

        // Fill with water :<
        // w.getLeft().getLeft().RPillar(5, rand, Material.WATER);
        w.getLeft().RPillar(5, rand, Material.WATER);
        w.RPillar(5, rand, Material.WATER);
        w.getRight().RPillar(5, rand, Material.WATER);
        // w.getRight().getRight().RPillar(5, rand, Material.WATER);

        // Floor is Prismarine >:V
        for (int i = 0; i <= 1; i++) {
            if (w.getLeft(i).getType() != Material.SEA_LANTERN) {
                w.getLeft(i).setType(Material.PRISMARINE);
            }
            if (w.getRight(i).getType() != Material.SEA_LANTERN) {
                w.getRight(i).setType(Material.PRISMARINE);
            }
        }

        // Lantern corridor lights
        if (light) {
            w.setType(Material.SEA_LANTERN);
        }
        light = !light;

        // Pillars
        if (GenUtils.chance(rand, 1, 20)) {
            w.RPillar(5, rand, GenUtils.mergeArr(design.tileSet, new Material[] {Material.SEA_LANTERN}));
        }
        //		else if(GenUtils.chance(rand, 1, 50)){
        //			MonumentRoomPopulator.setThickPillar(rand, design, w.get().getUp(3));
        //		}

        // Thick pillars
        if (GenUtils.chance(rand, 1, 50)) {
            MonumentRoomPopulator.setThickPillar(rand, design, w.get().getDown());
        }

        // Small spires on the top
        if (GenUtils.chance(rand, 1, 50)) {
            if (w.getUp(6).isSolid() && !w.getUp(7).isSolid()) {
                design.spire(w.getUp(7), rand);
            }
        }
    }

}
