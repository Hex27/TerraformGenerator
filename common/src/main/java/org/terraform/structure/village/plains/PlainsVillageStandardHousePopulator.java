package org.terraform.structure.village.plains;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.village.plains.house.PlainsVillageHouseJigsawBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class PlainsVillageStandardHousePopulator extends PlainsVillageAbstractRoomPopulator {

    private final PlainsVillagePopulator plainsVillagePopulator;

    public PlainsVillageStandardHousePopulator(PlainsVillagePopulator plainsVillagePopulator,
                                               Random rand,
                                               boolean forceSpawn,
                                               boolean unique)
    {
        super(rand, forceSpawn, unique);
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);
        int height = super.calculateRoomY(data, room);
        // GenUtils.getHighestGroundOrSeaLevel(data, room.getX(), room.getZ());

        // Debug squares

        PlainsVillageHouseJigsawBuilder builder = new PlainsVillageHouseJigsawBuilder(
                plainsVillagePopulator,
                room.getWidthX() - 3,
                room.getWidthZ() - 3,
                data,
                room.getX(),
                height,
                room.getZ()
        );
        if (room instanceof DirectionalCubeRoom) {
            builder.forceEntranceDirection(((DirectionalCubeRoom) room).getDirection());
        }

        builder.generate(this.rand);
        builder.build(this.rand);

        Wall entrance = builder.getEntranceBlock().getRear().getGround();
        int maxDepth = 6;

        boolean placedLamp = false;
        // Connect front to the nearest path.
        while (entrance.getType() != Material.DIRT_PATH && maxDepth > 0) {
            if (BlockUtils.isDirtLike(entrance.getType())) {
                entrance.setType(Material.DIRT_PATH);
            }

            if (!placedLamp && GenUtils.chance(this.rand, 3, 5)) {
                SimpleBlock target;
                if (this.rand.nextBoolean()) {
                    target = entrance.getLeft(2).getGround().getUp().get();
                }
                else {
                    target = entrance.getRight(2).getGround().getUp().get();
                }
                if (PlainsVillagePathPopulator.canPlaceLamp(target)) {
                    placedLamp = true;
                    PlainsVillagePathPopulator.placeLamp(rand, target);
                }

            }

            entrance = entrance.getFront().getGround();
            maxDepth--;
        }
    }


    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() >= 15;
    }
}
