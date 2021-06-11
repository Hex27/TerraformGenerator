package org.terraform.structure.village.plains;

import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.structure.village.plains.temple.PlainsVillageTempleJigsawBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Random;

public class PlainsVillageTemplePopulator extends RoomPopulatorAbstract {

	private PlainsVillagePopulator plainsVillagePopulator;
    public PlainsVillageTemplePopulator(PlainsVillagePopulator plainsVillagePopulator, Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {

        int height = GenUtils.getHighestGroundOrSeaLevel(data, room.getX(), room.getZ());
        
        //1 is added to height because temples need a small bit of elevation to look better
        PlainsVillageTempleJigsawBuilder builder = new PlainsVillageTempleJigsawBuilder(
        		plainsVillagePopulator,
        		room.getWidthX() - 3, room.getWidthZ() - 3, data, room.getX(), height+1, room.getZ()
        );
        if (room instanceof DirectionalCubeRoom)
            builder.forceEntranceDirection(((DirectionalCubeRoom) room).getDirection());

        builder.generate(this.rand);
        builder.build(this.rand);

        Wall entrance = builder.getEntranceBlock().getRear().getGround();
        int maxDepth = 12;

        boolean placedLamp = false;
        //Connect front to the nearest path.
        while (entrance.getType() != OneOneSevenBlockHandler.DIRT_PATH() && maxDepth > 0) {
            if (BlockUtils.isDirtLike(entrance.getType()))
                entrance.setType(OneOneSevenBlockHandler.DIRT_PATH());

            if (!placedLamp && GenUtils.chance(this.rand, 3, 5)) {
                SimpleBlock target;
                if (this.rand.nextBoolean())
                    target = entrance.getLeft(2).getGround().getRelative(0, 1, 0).get();
                else
                    target = entrance.getRight(2).getGround().getRelative(0, 1, 0).get();
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
    public boolean canPopulate(CubeRoom room) {
        return room.getWidthX() >= 15;
    }
}
