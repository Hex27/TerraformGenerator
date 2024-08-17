package org.terraform.structure.village.plains;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.village.plains.forge.PlainsVillageForgeJigsawBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class PlainsVillageForgePopulator extends PlainsVillageAbstractRoomPopulator {
	
	private final PlainsVillagePopulator plainsVillagePopulator;
    public PlainsVillageForgePopulator(PlainsVillagePopulator plainsVillagePopulator, Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
    	super.populate(data, room);
        int height = super.calculateRoomY(data, room);
        //GenUtils.getHighestGroundOrSeaLevel(data, room.getX(), room.getZ());
        
        //1 is added to height because temples need a small bit of elevation to look better
        PlainsVillageForgeJigsawBuilder builder = new PlainsVillageForgeJigsawBuilder(
        		plainsVillagePopulator,
                room.getWidthX() - 3, room.getWidthZ() - 3, data, room.getX(), height+1, room.getZ()
        );
        if (room instanceof DirectionalCubeRoom)
            builder.forceEntranceDirection(((DirectionalCubeRoom) room).getDirection());

        builder.generate(this.rand);
        builder.build(this.rand);

        Wall entrance = builder.getEntranceBlock().getGround();
        int maxDepth = 12;

        boolean placedLamp = false;
        //Connect front to the nearest path.
        while (entrance.getType() != Material.DIRT_PATH && maxDepth > 0) {
            if (BlockUtils.isDirtLike(entrance.getType()))
                entrance.setType(Material.DIRT_PATH);

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
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() >= 15;
    }
}
