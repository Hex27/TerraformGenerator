package org.terraform.structure.stronghold;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SilverfishDenPopulator extends RoomPopulatorAbstract {

    public SilverfishDenPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        // Spawn a random sphere of silverfish eggs
        SimpleBlock base = new SimpleBlock(data, room.getX(), room.getY() + room.getHeight() / 2 - 2, room.getZ());
        BlockUtils.replaceUpperSphere(rand.nextInt(9999),
                (room.getWidthX() - 2) / 2f,
                (room.getHeight() - 3),
                (room.getWidthZ() - 2) / 2f,
                base,
                false,
                Material.INFESTED_STONE,
                Material.INFESTED_STONE,
                Material.CAVE_AIR,
                Material.STONE
        );

        // Silverfish spawner in the middle
        data.setSpawner(room.getX(), room.getY() + 1, room.getZ(), EntityType.SILVERFISH);

        // Spawn loot chests
        int[] upperBounds = room.getUpperCorner();
        int[] lowerBounds = room.getLowerCorner();

        for (int i = 0; i < GenUtils.randInt(rand, 1, 3); i++) {
            int x = GenUtils.randInt(rand, lowerBounds[0] + 1, upperBounds[0] - 1);
            int z = GenUtils.randInt(rand, lowerBounds[1] + 1, upperBounds[1] - 1);
            int ny = room.getY() + 1;
            while (data.getType(x, ny, z).isSolid() && ny < room.getHeight() + room.getY()) {
                ny++;
            }
            if (ny == room.getHeight() + room.getY()) {
                continue;
            }

            data.setType(x, ny, z, Material.CHEST);
            org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) Bukkit.createBlockData(Material.CHEST);
            chest.setFacing(BlockUtils.getDirectBlockFace(rand));
            data.setBlockData(x, ny, z, chest);
            data.lootTableChest(x, ny, z, TerraLootTable.STRONGHOLD_CORRIDOR);
        }
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return !room.isBig();
    }
}
