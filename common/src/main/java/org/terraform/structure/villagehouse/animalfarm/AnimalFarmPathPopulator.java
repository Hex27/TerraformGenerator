package org.terraform.structure.villagehouse.animalfarm;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class AnimalFarmPathPopulator extends PathPopulatorAbstract {
    private final Random rand;
    private final RoomLayoutGenerator gen;

    public AnimalFarmPathPopulator(RoomLayoutGenerator gen, Random rand) {
        this.gen = gen;
        this.rand = rand;
    }

    @Override
    public void populate(@NotNull PathPopulatorData ppd) {
        Wall w = new Wall(ppd.base, ppd.dir);
        for (CubeRoom room : gen.getRooms()) {
            if (room.isPointInside(new int[] {w.get().getX(), w.get().getZ()})) {
                return;
            }
        }
        if (GenUtils.chance(rand, 1, 50)) {
            w.getLeft().getGround().getUp().setType(Material.CAMPFIRE);
        }
        if (GenUtils.chance(rand, 2, 10)) {
            w.getGround()
             .setType(GenUtils.randChoice(Material.COBBLESTONE, Material.COARSE_DIRT, Material.MOSSY_COBBLESTONE));
        }
        if (GenUtils.chance(rand, 2, 10)) {
            w.getLeft()
             .getGround()
             .setType(GenUtils.randChoice(Material.COBBLESTONE, Material.COARSE_DIRT, Material.MOSSY_COBBLESTONE));
        }
        if (GenUtils.chance(rand, 2, 10)) {
            w.getRight()
             .getGround()
             .setType(GenUtils.randChoice(Material.COBBLESTONE, Material.COARSE_DIRT, Material.MOSSY_COBBLESTONE));
        }
    }
}
