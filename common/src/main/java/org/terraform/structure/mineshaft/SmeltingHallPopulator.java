package org.terraform.structure.mineshaft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Furnace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.GenUtils;

import java.util.Map.Entry;
import java.util.Random;

public class SmeltingHallPopulator extends RoomPopulatorAbstract {

    public SmeltingHallPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        int[] lowerCorner = room.getLowerCorner(3);
        int[] upperCorner = room.getUpperCorner(3);

        // Flooring - Have a stone brick platform.
        int y = room.getY();
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                SimpleBlock b = new SimpleBlock(data, x, y, z);
                if (b.getType() == Material.CAVE_AIR
                    || b.getType() == Material.OAK_PLANKS
                    || b.getType() == Material.OAK_SLAB
                    || b.getType() == Material.GRAVEL)
                {
                    b.setType(GenUtils.randChoice(
                            Material.STONE_BRICKS,
                            Material.CRACKED_STONE_BRICKS,
                            Material.MOSSY_STONE_BRICKS,
                            Material.MOSSY_COBBLESTONE,
                            Material.COBBLESTONE,
                            Material.CAVE_AIR
                    ));
                    // Small chance to set a lantern
                    if (TConfig.areDecorationsEnabled() && GenUtils.chance(rand, 1, 150)) {
                        b.getUp().setType(Material.COBBLESTONE);
                        b.getUp(2).setType(Material.LANTERN);
                    }
                }
            }
        }

        // Support hooks or pillars
        for (int[] corner : room.getAllCorners(3)) {
            int x = corner[0];
            int z = corner[1];
            Wall w = new Wall(new SimpleBlock(data, x, room.getY() + 1, z), BlockFace.NORTH);
            if (w.findCeiling(50) != null) {
                if (TConfig.areDecorationsEnabled()) {
                    w.LPillar(50, rand, Material.IRON_BARS);
                }
            }
            else {
                w.getDown().downUntilSolid(rand, Material.OAK_LOG);
            }
        }

        // Furnaces & Chests
        for (Entry<Wall, Integer> walls : room.getFourWalls(data, 4).entrySet()) {
            int type = rand.nextInt(3);
            if (type == 0) {
                continue;
            }
            Wall w = walls.getKey();
            int l = walls.getValue();
            for (int i = 0; i < l; i++) {
                // Non-rail areas
                if (TConfig.areDecorationsEnabled() && w.getType() == Material.CAVE_AIR) {
                    if (type == 1) { // Furnaces
                        Furnace furnace = (Furnace) Bukkit.createBlockData(Material.FURNACE);
                        furnace.setFacing(w.getDirection());
                        for (int ny = 0; ny < room.getHeight() / 3; ny++) {
                            w.getRelative(0, ny, 0).setBlockData(furnace);
                        }
                    }
                    else if (GenUtils.chance(rand, 1, 5)) { // Chests
                        Chest chest = (Chest) Bukkit.createBlockData(Material.CHEST);
                        chest.setFacing(w.getDirection());
                        w.setBlockData(chest);
                        data.lootTableChest(w.getX(), w.getY(), w.getZ(), TerraLootTable.ABANDONED_MINESHAFT);
                    }
                }
                w = w.getLeft();
            }
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}
