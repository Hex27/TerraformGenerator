package org.terraform.structure.catacombs;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;

import java.util.Map.Entry;
import java.util.Random;

public class CatacombsSkeletonDungeonPopulator extends CatacombsStandardPopulator {

    public CatacombsSkeletonDungeonPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }


    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);
        SimpleBlock center = room.getCenterSimpleBlock(data).getUp();

        data.setSpawner(center.getX(), center.getY(), center.getZ(), EntityType.SKELETON);

        int chests = 0;

        // Chests
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey();
            if (chests >= 2) {
                break;
            }
            for (int i = 0; i < entry.getValue(); i++) {
                if (TConfig.areDecorationsEnabled() && GenUtils.chance(rand, 1, 40)) {
                    new ChestBuilder(Material.CHEST).setFacing(w.getDirection())
                                                    .setLootTable(TerraLootTable.SIMPLE_DUNGEON)
                                                    .apply(w);
                    chests++;
                }
                if (chests >= 2) {
                    break;
                }
                w = w.getLeft();
            }
        }

        super.spawnHangingChains(data, room);
    }

    @Override
    protected boolean lightCandles() {
        return false;
    }

}
