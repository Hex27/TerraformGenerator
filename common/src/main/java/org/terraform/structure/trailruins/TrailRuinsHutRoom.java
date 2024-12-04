package org.terraform.structure.trailruins;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.version.V_1_19;
import org.terraform.utils.version.V_1_20;
import org.terraform.utils.version.Version;

import java.util.Map;
import java.util.Random;

public class TrailRuinsHutRoom extends RoomPopulatorAbstract {
    public TrailRuinsHutRoom(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        Material terracottaType = GenUtils.randChoice(BlockUtils.TERRACOTTA);
        for (Map.Entry<Wall, Integer> entry : room.getFourWalls(data, 1).entrySet()) {
            Wall w = entry.getKey();
            for (int i = 1; i < entry.getValue() - 1; i++) {
                if (w.getDown().isSolid()) {
                    int h = GenUtils.randInt(rand, 1, 5);
                    w.Pillar(Math.min(2, h), V_1_19.MUD_BRICKS);
                    w.getUp(2).Pillar(Math.min(0, h - 2), terracottaType);

                    // If this is inside a jungle, allow jungle chests.
                    if (data.getBiome(w.getX(), w.getZ()) == Biome.JUNGLE
                        || data.getBiome(w.getX(), w.getZ()) == Biome.BAMBOO_JUNGLE
                        // || data.getBiome(w.getX(),w.getZ()) == Biome.SPARSE_JUNGLE
                        // this biome doesn't exist in 1.16. It's not used anyway, might as well remove the check
                    )
                    {
                        if (i > 1 && i < entry.getValue() - 2 && GenUtils.chance(rand, 1, 9)) {
                            new ChestBuilder(Material.CHEST).setFacing(w.getDirection())
                                                            .setLootTable(TerraLootTable.JUNGLE_TEMPLE)
                                                            .apply(w.getFront().getRight().getUp());
                        }
                    }
                }
                w = w.getLeft();
            }
        }

        // Suspicious gravel
        if (Version.isAtLeast(20)) {
            for (int i = 0; i < rand.nextInt(4); i++) {
                int[] coords = room.randomCoords(rand);
                data.setType(coords[0], room.getY(), coords[2], V_1_20.SUSPICIOUS_GRAVEL);
                data.lootTableChest(
                        coords[0],
                        room.getY(),
                        coords[2],
                        GenUtils.chance(rand, 1, 3)
                        ? TerraLootTable.TRAIL_RUINS_ARCHAEOLOGY_RARE
                        : TerraLootTable.TRAIL_RUINS_ARCHAEOLOGY_COMMON
                );
            }
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}
