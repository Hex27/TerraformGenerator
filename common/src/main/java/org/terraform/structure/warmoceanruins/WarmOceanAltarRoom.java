package org.terraform.structure.warmoceanruins;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;

import java.util.Map;
import java.util.Random;

public class WarmOceanAltarRoom extends WarmOceanBaseRoom {
    public WarmOceanAltarRoom(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }


    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);

        for (Map.Entry<Wall, Integer> entry : room.getFourWalls(data, 3).entrySet()) {
            Wall w = entry.getKey().getGround();
            for (int i = 0; i < entry.getValue(); i++) {
                if (i % 2 == 0 && rand.nextBoolean()) {
                    int h = 2 + rand.nextInt(3);
                    w.getUp().Pillar(h, Material.CUT_SANDSTONE);
                    w.getUp(h + 1)
                     .setType(Material.POLISHED_DIORITE, Material.POLISHED_ANDESITE, Material.POLISHED_GRANITE);
                }
                else if (i % 2 == 1 && rand.nextBoolean()) {
                    int h = 2 + rand.nextInt(3);
                    w.getUp().setType(Material.CUT_SANDSTONE_SLAB, Material.SMOOTH_SANDSTONE);
                }

                if (i > 0 && i < entry.getValue() - 1 && GenUtils.chance(rand, 1, 9)) {
                    new ChestBuilder(Material.CHEST).setFacing(w.getDirection())
                                                    .setLootTable(TerraLootTable.UNDERWATER_RUIN_SMALL)
                                                    .setWaterlogged(w.getUp().getY()
                                                                    <= TConfig.c.HEIGHT_MAP_SEA_LEVEL)
                                                    .apply(w.getFront().getRight().getUp());
                }
                w = w.getLeft().getGround();
            }
        }
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() < 25;
    }
}
