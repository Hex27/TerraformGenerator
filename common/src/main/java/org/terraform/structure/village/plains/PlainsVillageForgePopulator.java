package org.terraform.structure.village.plains;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.structure.village.plains.forge.PlainsVillageForgeJigsawBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class PlainsVillageForgePopulator extends RoomPopulatorAbstract {

    public PlainsVillageForgePopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {

        int height = GenUtils.getHighestGround(data, room.getX(), room.getZ());
        
        //1 is added to height because temples need a small bit of elevation to look better
        PlainsVillageForgeJigsawBuilder builder = new PlainsVillageForgeJigsawBuilder(
                room.getWidthX() - 3, room.getWidthZ() - 3, data, room.getX(), height+1, room.getZ()
        );
        if (room instanceof DirectionalCubeRoom)
            builder.forceEntranceDirection(((DirectionalCubeRoom) room).getDirection());

        builder.generate(this.rand);
        builder.build(this.rand);

        Wall entrance = builder.getEntranceBlock().getRear().getGround();
        int maxDepth = 6;

        boolean placedLamp = false;
        //Connect front to the nearest path.
        while (entrance.getType() != Material.GRASS_PATH && maxDepth > 0) {
            if (BlockUtils.isDirtLike(entrance.getType()))
                entrance.setType(Material.GRASS_PATH);

            if (!placedLamp && GenUtils.chance(this.rand, 3, 5)) {
                SimpleBlock target;
                if (this.rand.nextBoolean())
                    target = entrance.getLeft(2).getGround().getRelative(0, 1, 0).get();
                else
                    target = entrance.getRight(2).getGround().getRelative(0, 1, 0).get();
                if (canPlaceLamp(target)) {
                    placedLamp = true;
                    PlainsVillagePathPopulator.placeLamp(rand, target);
                }

            }

            entrance = entrance.getFront().getGround();
            maxDepth--;
        }
    }

    private boolean canPlaceLamp(SimpleBlock target) {

        for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
            for (int i = 0; i < 6; i++)
                if (target.getRelative(face).getRelative(0, i, 0).getType().isSolid())
                    return false;
        }

        return true;
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return room.getWidthX() >= 15;
    }
}
