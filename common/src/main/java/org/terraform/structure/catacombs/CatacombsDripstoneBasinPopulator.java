package org.terraform.structure.catacombs;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.*;
import org.terraform.utils.SphereBuilder.SphereType;

import java.util.Random;

public class CatacombsDripstoneBasinPopulator extends CatacombsStandardPopulator {

    public CatacombsDripstoneBasinPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }


    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);
        SimpleBlock center = room.getCenterSimpleBlock(data).getUp();

        new SphereBuilder(new Random(), center.getDown(), Material.WATER).setRadius(3f)
                                                                         .setSphereType(SphereType.LOWER_SEMISPHERE)
                                                                         .setDoLiquidContainment(true)
                                                                         .setHardReplace(true)
                                                                         .build();

        new CylinderBuilder(new Random(), center.getUp(10), Material.CAVE_AIR).setRadius(2.5f)
                                                                              .setRY(6f)
                                                                              .setHardReplace(true)
                                                                              .build();

        // Sea pickles
        for (int i = 2; i <= GenUtils.randInt(2, 5); i++) {
            int[] coords = room.randomCoords(rand, 2);
            SimpleBlock target = new SimpleBlock(data, coords[0], room.getY() + 1, coords[2]);
            target = target.findFloor(room.getHeight());
            if (target == null || !BlockUtils.isWet(target.getUp())) {
                continue;
            }

            CoralGenerator.generateSeaPickles(data, target.getX(), target.getY() + 1, target.getZ());
        }
    }

}
