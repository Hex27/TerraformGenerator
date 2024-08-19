package org.terraform.structure.stronghold;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

import java.util.Map.Entry;
import java.util.Random;

public class NetherPortalRoomPopulator extends RoomPopulatorAbstract {

    public NetherPortalRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        // Wall up all entrances with wooden planks
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 0).entrySet()) {
            Wall wall = entry.getKey().clone();
            int length = entry.getValue();
            for (int i = 0; i < length; i++) {
                wall.RPillar(room.getHeight(), rand, Material.OAK_PLANKS);
                wall = wall.getLeft();
            }
        }

        int rX = (room.getWidthX() - 2) / 2;
        int rZ = (room.getWidthZ() - 2) / 2;
        int rY = 2;

        int y = room.getY();
        int x = room.getX();
        int z = room.getZ();

        // Make some nether corruption
        BlockUtils.replaceUpperSphere(rand.nextInt(123),
                rX,
                rY,
                rZ,
                new SimpleBlock(data, x, y, z),
                true,
                Material.NETHERRACK,
                Material.NETHERRACK,
                Material.SOUL_SAND,
                Material.NETHERRACK,
                Material.NETHERRACK,
                Material.MAGMA_BLOCK
        );

        while (data.getType(x, y, z).isSolid() && y < room.getY() + room.getHeight() - 5) {
            y++;
        }
        y--;
        Wall wall = new Wall(new SimpleBlock(data, x, y, z), BlockUtils.getXZPlaneBlockFace(rand));

        Material[] blocks = {
                Material.OBSIDIAN,
                Material.OBSIDIAN,
                Material.OBSIDIAN,
                Material.OBSIDIAN,
                Material.OBSIDIAN,
                Material.OBSIDIAN,
                Material.OBSIDIAN,
                Material.CAVE_AIR
        };

        // Spawn a nether portal
        wall.getRight().getRight().Pillar(5, rand, blocks);
        wall.Pillar(5, rand, blocks);
        wall.getRight().Pillar(5, rand, blocks);
        wall.getLeft().Pillar(5, rand, blocks);

        wall.getUp(4).getRight().getRight().setType(Material.CHISELED_STONE_BRICKS);
        wall.getUp(4).getLeft().setType(Material.CHISELED_STONE_BRICKS);
        wall.getRight().getRight().setType(Material.CHISELED_STONE_BRICKS);
        wall.getLeft().setType(Material.CHISELED_STONE_BRICKS);

        wall = wall.getUp();

        wall.Pillar(3, rand, Material.CAVE_AIR);
        wall.getRight().Pillar(3, rand, Material.CAVE_AIR);
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.isBig() && !room.isHuge() && room.getHeight() > 8;
    }
}
