package org.terraform.structure.pillager.mansion.secondfloor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSize;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public class MansionTowerStairwayPopulator extends MansionRoomPopulator {

    private final int towerHeight;

    public MansionTowerStairwayPopulator(CubeRoom room,
                                         HashMap<BlockFace, MansionInternalWallState> internalWalls,
                                         int towerHeight)
    {
        super(room, internalWalls);
        this.towerHeight = towerHeight;
    }

    protected static int getNextIndex(int bfIndex) {
        bfIndex++;
        if (bfIndex >= 8) {
            bfIndex = 0;
        }
        return bfIndex;
    }

    @Override
    public void decorateRoom(@NotNull PopulatorDataAbstract data, @NotNull Random random) {
        int bfIndex = 0;
        Wall b = new Wall(this.getRoom().getCenterSimpleBlock(data));
        int height = 8;
        if (towerHeight == 2) {
            height = 15;
        }
        // Slabs curving upwards
        for (int i = 1; i < height; i++) {
            b.getRelative(0, i, 0).setType(Material.STONE_BRICKS);

            BlockFace face = BlockUtils.xzPlaneBlockFaces[bfIndex];
            Slab bottom = (Slab) Bukkit.createBlockData(Material.STONE_BRICK_SLAB);
            bottom.setType(Type.BOTTOM);
            b.getRelative(face).getRelative(0, i, 0).setBlockData(bottom);
            b.getRelative(face).getRelative(0, i + 1, 0).Pillar(3, Material.AIR);
            bfIndex = getNextIndex(bfIndex);

            face = BlockUtils.xzPlaneBlockFaces[bfIndex];
            Slab top = (Slab) Bukkit.createBlockData(Material.STONE_BRICK_SLAB);
            top.setType(Type.TOP);
            b.getRelative(face).getRelative(0, i, 0).setBlockData(top);
            b.getRelative(face).getRelative(0, i + 1, 0).Pillar(3, Material.AIR);
            bfIndex = getNextIndex(bfIndex);
        }

        // 7 blocks above, place some chests and decorations at the stairs
        b = b.getUp(8);

        for (Entry<Wall, Integer> entry : this.getRoom().getFourWalls(data, 2).entrySet()) {
            Wall w = entry.getKey().getAtY(b.getY());
            for (int i = 0; i < entry.getValue(); i++) {
                int pileHeight = GenUtils.randInt(random, 1, 2);
                w.Pillar(pileHeight,
                        random,
                        Material.CRAFTING_TABLE,
                        Material.FLETCHING_TABLE,
                        Material.CARTOGRAPHY_TABLE,
                        Material.ANVIL,
                        Material.NOTE_BLOCK,
                        Material.SMITHING_TABLE
                );
                if (GenUtils.chance(random, 1, 5)) {
                    w.setBlockData(BlockUtils.getRandomBarrel());
                    data.lootTableChest(w.getX(), w.getY(), w.getZ(), TerraLootTable.WOODLAND_MANSION);
                }
                if (GenUtils.chance(random, 1, 5)) {
                    w.getRelative(0, pileHeight, 0).setType(Material.LANTERN);
                }

                w = w.getLeft();
            }
        }
    }

    @Override
    public @NotNull MansionRoomSize getSize() {
        return new MansionRoomSize(1, 1);
    }

}
