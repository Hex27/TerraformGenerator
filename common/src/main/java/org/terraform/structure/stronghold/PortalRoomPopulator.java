package org.terraform.structure.stronghold;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Map.Entry;
import java.util.Random;

public class PortalRoomPopulator extends RoomPopulatorAbstract {

    public PortalRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    private @NotNull Slab randTopSlab() {
        Slab slab = (Slab) Bukkit.createBlockData(BlockUtils.stoneBrickSlab(rand));
        slab.setType(Type.TOP);
        return slab;
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        int[] lowerBounds = room.getLowerCorner();
        int[] upperBounds = room.getUpperCorner();

        // Bookshelves and entrance decor
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 0).entrySet()) {
            Wall wall = entry.getKey().getUp(3);
            for (int i = 1; i <= entry.getValue(); i++) {
                wall.setType(Material.CHISELED_STONE_BRICKS);
                if (i % 5 == 2 || i % 5 == 4) {
                    wall.getUp().Pillar(15 - 7, rand, Material.CHISELED_STONE_BRICKS);
                }
                if (i % 5 == 3) {
                    wall.getUp().Pillar(15 - 7, rand, Material.COBBLESTONE_WALL);
                }

                for (int h = 1; h < 15 - 7; h++) {
                    BlockUtils.correctSurroundingMultifacingData(wall.getRelative(0, h, 0).get());
                }

                wall = wall.getLeft();
            }
        }

        // Obisidian line around the room
        for (int x = lowerBounds[0] + 1; x < upperBounds[0]; x++) {
            for (int z = lowerBounds[1] + 1; z < upperBounds[1]; z++) {
                data.setType(x, room.getY(), z, Material.OBSIDIAN);
            }
        }
        for (int x = lowerBounds[0] + 2; x < upperBounds[0] - 1; x++) {
            for (int z = lowerBounds[1] + 2; z < upperBounds[1] - 1; z++) {
                data.setType(x, room.getY(), z, BlockUtils.stoneBrick(rand));
            }
        }

        // Texture the sides of the ceiling
        for (int x = lowerBounds[0] + 1; x < upperBounds[0]; x++) {
            for (int z = lowerBounds[1] + 1; z < upperBounds[1]; z++) {
                data.setType(x, room.getY() + room.getHeight() - 1, z, Material.COBBLESTONE);
                data.setType(x, room.getY() + room.getHeight() - 2, z, Material.COBBLESTONE);
            }
        }
        for (int x = lowerBounds[0] + 3; x <= upperBounds[0] - 3; x++) {
            for (int z = lowerBounds[1] + 3; z <= upperBounds[1] - 3; z++) {
                data.setType(x, room.getY() + room.getHeight() - 1, z, Material.CAVE_AIR);
            }
        }
        for (int x = lowerBounds[0] + 2; x <= upperBounds[0] - 2; x++) {
            for (int z = lowerBounds[1] + 2; z <= upperBounds[1] - 2; z++) {
                data.setType(x, room.getY() + room.getHeight() - 2, z, Material.CAVE_AIR);
            }
        }

        // Add ceiling decor
        SimpleBlock ceil = new SimpleBlock(data, room.getX(), room.getHeight() - 1 + room.getY(), room.getZ());
        ceilDecor(ceil);

        // Create the portal
        SimpleBlock base = new SimpleBlock(data, room.getX(), room.getY() + 2, room.getZ());
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Wall w = new Wall(base.getRelative(face).getRelative(face), face);
            EndPortalFrame portalFrame = (EndPortalFrame) Bukkit.createBlockData(Material.END_PORTAL_FRAME);
            portalFrame.setFacing(face.getOppositeFace());
            portalFrame.setEye(GenUtils.chance(rand, 1, 10));
            w.setBlockData(portalFrame);
            portalFrame.setEye(GenUtils.chance(rand, 1, 10));
            w.getLeft().setBlockData(portalFrame);
            portalFrame.setEye(GenUtils.chance(rand, 1, 10));
            w.getRight().setBlockData(portalFrame);
            w.getLeft().getLeft().setType(Material.CHISELED_STONE_BRICKS);
            w.getRight().getRight().setType(Material.CHISELED_STONE_BRICKS);

            w = w.getDown();
            w.setType(BlockUtils.stoneBrick(rand));
            w.getLeft().setType(BlockUtils.stoneBrick(rand));
            w.getRight().setType(BlockUtils.stoneBrick(rand));
        }

        base = new SimpleBlock(data, room.getX(), room.getY() + 1, room.getZ());

        // Lava
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                base.getRelative(nx, 0, nz).setType(Material.LAVA);
            }
        }

        // Create the portal base
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Wall w = new Wall(base.getRelative(face).getRelative(face).getRelative(face), face);
            w.setType(BlockUtils.stoneBrick(rand));
            w.getLeft().setType(BlockUtils.stoneBrick(rand));
            w.getRight().setType(BlockUtils.stoneBrick(rand));
            w.getLeft().getLeft().setType(BlockUtils.stoneBrick(rand));
            w.getRight().getRight().setType(BlockUtils.stoneBrick(rand));

            w.getUp().setType(BlockUtils.stoneBrickSlab(rand));
            w.getUp().getLeft().setType(BlockUtils.stoneBrickSlab(rand));
            w.getUp().getRight().setType(BlockUtils.stoneBrickSlab(rand));

            w.getLeft().getLeft().getLeft().setType(BlockUtils.stoneBrickSlab(rand));
            w.getRight().getRight().getRight().setType(BlockUtils.stoneBrickSlab(rand));

            Stairs stairs = (Stairs) Bukkit.createBlockData(Material.POLISHED_ANDESITE_STAIRS);
            stairs.setFacing(face.getOppositeFace());
            w.getFront().setBlockData(stairs);
            w.getLeft().getFront().setBlockData(stairs);
            w.getRight().getFront().setBlockData(stairs);
            w.getLeft().getFront().getLeft().setType(BlockUtils.stoneBrickSlab(rand));
            w.getRight().getFront().getRight().setType(BlockUtils.stoneBrickSlab(rand));
        }

        decoratedPillar(rand, data, room.getX() + 6, room.getY() + 1, room.getZ() + 6, room.getHeight() - 2);
        decoratedPillar(rand, data, room.getX() - 6, room.getY() + 1, room.getZ() + 6, room.getHeight() - 2);
        decoratedPillar(rand, data, room.getX() + 6, room.getY() + 1, room.getZ() - 6, room.getHeight() - 2);
        decoratedPillar(rand, data, room.getX() - 6, room.getY() + 1, room.getZ() - 6, room.getHeight() - 2);

        // Lava pools.
        lavaPool(data, room.getX() + 8, room.getY() + 1, room.getZ(), room.getHeight() - 2);
        lavaPool(data, room.getX() - 8, room.getY() + 1, room.getZ(), room.getHeight() - 2);

        // Connect the pools to the center
        Wall w = new Wall(new SimpleBlock(data, room.getX() + 6, room.getY() + 1, room.getZ()), BlockFace.WEST);
        for (int i = 0; i < 4; i++) {
            w.setType(Material.LAVA);
            if (i == 0 || i == 3) {
                w.getLeft().setType(Material.CHISELED_STONE_BRICKS);
                w.getRight().setType(Material.CHISELED_STONE_BRICKS);
            }
            else {
                Stairs stairs = (Stairs) Bukkit.createBlockData(Material.POLISHED_ANDESITE_STAIRS);
                stairs.setFacing(BlockUtils.getAdjacentFaces(w.getDirection())[1]);
                w.getLeft().setBlockData(stairs);

                stairs = (Stairs) Bukkit.createBlockData(Material.POLISHED_ANDESITE_STAIRS);
                stairs.setFacing(BlockUtils.getAdjacentFaces(w.getDirection())[0]);
                w.getRight().setBlockData(stairs);
            }
            w = w.getFront();
        }

        w = new Wall(new SimpleBlock(data, room.getX() - 6, room.getY() + 1, room.getZ()), BlockFace.EAST);
        for (int i = 0; i < 4; i++) {
            w.setType(Material.LAVA);
            if (i == 0 || i == 3) {
                w.getLeft().setType(Material.CHISELED_STONE_BRICKS);
                w.getRight().setType(Material.CHISELED_STONE_BRICKS);
            }
            else {
                Stairs stairs = (Stairs) Bukkit.createBlockData(Material.POLISHED_ANDESITE_STAIRS);
                stairs.setFacing(BlockUtils.getAdjacentFaces(w.getDirection())[1]);
                w.getLeft().setBlockData(stairs);

                stairs = (Stairs) Bukkit.createBlockData(Material.POLISHED_ANDESITE_STAIRS);
                stairs.setFacing(BlockUtils.getAdjacentFaces(w.getDirection())[0]);
                w.getRight().setBlockData(stairs);
            }
            w = w.getFront();
        }
    }

    public void lavaPool(@NotNull PopulatorDataAbstract data, int x, int y, int z, int height) {
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Wall w = new Wall(new SimpleBlock(data, x, y, z), face).getFront().getFront();
            Stairs stairs = (Stairs) Bukkit.createBlockData(Material.POLISHED_ANDESITE_STAIRS);
            stairs.setFacing(face.getOppositeFace());
            w.setBlockData(stairs);
            w.getLeft().setBlockData(stairs);
            w.getRight().setBlockData(stairs);
            w.getRight().getRight().setType(Material.CHISELED_STONE_BRICKS);
        }
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                data.setType(x + nx, y, z + nz, Material.LAVA);
            }
        }

        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                data.setType(x + nx, y + height, z + nz, Material.CHISELED_STONE_BRICKS);
                if (nx == 0 && nz == 0) {
                    data.setType(x, y + height, z, Material.LAVA);
                }
            }
        }
    }

    public void decoratedPillar(@NotNull Random rand,
                                @NotNull PopulatorDataAbstract data,
                                int x,
                                int y,
                                int z,
                                int height)
    {
        BlockUtils.spawnPillar(rand, data, x, y, z, Material.CHISELED_STONE_BRICKS, height, height);
        BlockUtils.spawnPillar(rand, data, x + 1, y, z + 1, Material.COBBLESTONE_WALL, height, height);
        BlockUtils.spawnPillar(rand, data, x - 1, y, z + 1, Material.COBBLESTONE_WALL, height, height);
        BlockUtils.spawnPillar(rand, data, x + 1, y, z - 1, Material.COBBLESTONE_WALL, height, height);
        BlockUtils.spawnPillar(rand, data, x - 1, y, z - 1, Material.COBBLESTONE_WALL, height, height);

        data.setType(x + 1, y, z + 1, Material.CHISELED_STONE_BRICKS);
        data.setType(x - 1, y, z + 1, Material.CHISELED_STONE_BRICKS);
        data.setType(x + 1, y, z - 1, Material.CHISELED_STONE_BRICKS);
        data.setType(x - 1, y, z - 1, Material.CHISELED_STONE_BRICKS);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Stairs stairs = (Stairs) Bukkit.createBlockData(Material.POLISHED_ANDESITE_STAIRS);
            stairs.setFacing(face.getOppositeFace());
            data.setBlockData(x + face.getModX(), y, z + face.getModZ(), stairs);
        }

        for (BlockFace face : BlockUtils.directBlockFaces) {
            Stairs stairs = (Stairs) Bukkit.createBlockData(Material.POLISHED_ANDESITE_STAIRS);
            stairs.setFacing(face.getOppositeFace());
            stairs.setHalf(Half.TOP);
            data.setBlockData(x + face.getModX(), y + height, z + face.getModZ(), stairs);
        }

        data.setType(x + 1, y + height, z + 1, Material.CHISELED_STONE_BRICKS);
        data.setType(x - 1, y + height, z + 1, Material.CHISELED_STONE_BRICKS);
        data.setType(x + 1, y + height, z - 1, Material.CHISELED_STONE_BRICKS);
        data.setType(x - 1, y + height, z - 1, Material.CHISELED_STONE_BRICKS);
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() == 25 && room.getWidthZ() == 25 && room.getHeight() == 15;
    }

    private void ceilDecor(@NotNull SimpleBlock ceil) {
        ceil.setType(Material.CHISELED_STONE_BRICKS);
        ceil.getRelative(0, 0, -1).setType(Material.CHISELED_STONE_BRICKS);
        ceil.getRelative(0, 0, -2).setType(Material.MOSSY_STONE_BRICKS);
        ceil.getRelative(0, 0, -3).setType(Material.MOSSY_COBBLESTONE);
        ceil.getRelative(0, 0, -4).setBlockData(randTopSlab());

        ceil.getRelative(0, 0, 1).setType(Material.CHISELED_STONE_BRICKS);
        ceil.getRelative(0, 0, 2).setType(Material.MOSSY_STONE_BRICKS);
        ceil.getRelative(0, 0, 3).setType(Material.MOSSY_COBBLESTONE);
        ceil.getRelative(0, 0, 4).setBlockData(randTopSlab());
        for (int i : new int[] {-1, 1}) {
            ceil.getRelative(i, 0, -1).setType(Material.MOSSY_STONE_BRICKS);
            ceil.getRelative(i, 0, 0).setType(Material.MOSSY_STONE_BRICKS);
            ceil.getRelative(i, 0, 1).setType(Material.MOSSY_STONE_BRICKS);

            ceil.getRelative(2 * i, 0, -1).setType(Material.MOSSY_COBBLESTONE);
            ceil.getRelative(2 * i, 0, 0).setType(Material.MOSSY_COBBLESTONE);
            ceil.getRelative(2 * i, 0, 1).setType(Material.MOSSY_COBBLESTONE);
            ceil.getRelative(i, 0, -2).setType(Material.MOSSY_COBBLESTONE);
            ceil.getRelative(i, 0, 2).setType(Material.MOSSY_COBBLESTONE);

            SimpleBlock[] blocks = new SimpleBlock[7];
            blocks[0] = ceil.getRelative(3 * i, 0, -1);
            blocks[1] = ceil.getRelative(3 * i, 0, 0);
            blocks[2] = ceil.getRelative(3 * i, 0, 1);
            blocks[3] = ceil.getRelative(i, 0, 3);
            blocks[4] = ceil.getRelative(i, 0, -3);
            blocks[5] = ceil.getRelative(2 * i, 0, 2);
            blocks[6] = ceil.getRelative(2 * i, 0, -2);
            for (SimpleBlock b : blocks) {
                b.setType(Material.COBBLESTONE);
            }
            for (SimpleBlock b : blocks) {
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    b.getRelative(face).lsetBlockData(randTopSlab());
                }
            }
        }
    }

}
