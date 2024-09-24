package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.GenUtils;

import java.util.Map.Entry;
import java.util.Random;

public class SilverfishNestPopulator extends RoomPopulatorAbstract {

    public SilverfishNestPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) {
                if (w.isSolid()) { // Don't block off pathways
                    w.Pillar(room.getHeight() - 1,
                            rand,
                            Material.ANDESITE,
                            Material.ANDESITE,
                            Material.ANDESITE,
                            Material.ANDESITE_SLAB,
                            Material.STONE_BRICKS,
                            Material.STONE_BRICK_SLAB,
                            Material.CRACKED_STONE_BRICKS,
                            Material.SANDSTONE,
                            Material.STONE_BRICKS
                    );

                    // Make "lumps" on the walls
                    if (i > 1 && i < entry.getValue() - 2) {
                        w.getFront().Pillar(room.getHeight() - 1,
                                rand,
                                Material.AIR,
                                Material.AIR,
                                Material.AIR,
                                Material.AIR,
                                Material.AIR,
                                Material.STONE,
                                Material.INFESTED_STONE,
                                Material.INFESTED_STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.ANDESITE
                        );
                    }

                    // Spawn chests
                    if (GenUtils.chance(this.rand, 1, 50) && i != 0 && i != entry.getValue() - 1) {
                        Directional chest = (Directional) Bukkit.createBlockData(Material.CHEST);
                        chest.setFacing(w.getDirection());
                        w.getFront().setBlockData(chest);
                        data.lootTableChest(w.getFront().getX(),
                                w.getFront().getY(),
                                w.getFront().getZ(),
                                TerraLootTable.SIMPLE_DUNGEON
                        );
                    }
                }

                w = w.getLeft();
            }
        }

        // Ceiling of lumps
        int[] lowerCorner = room.getLowerCorner(3);
        int[] upperCorner = room.getUpperCorner(3);
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                Wall w = new Wall(new SimpleBlock(data, x, room.getY() + room.getHeight() - 1, z));
                w.downLPillar(rand, GenUtils.randInt(0, 4), Material.STONE, Material.ANDESITE, Material.INFESTED_STONE);
            }
        }

        // Place a spawner if the nest is large
        if (room.getWidthX() >= 10 && room.getWidthZ() >= 10) {
            data.setSpawner(room.getX(), room.getY() + 1, room.getZ(), EntityType.SILVERFISH);
        }
    }


    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        // 13, as that's the size for crypt rooms. Don't compete with crypt rooms.
        return room.getWidthX() < 13 && room.getWidthZ() < 13;
    }
}
