package org.terraform.structure.stronghold;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Map.Entry;
import java.util.Random;

public class PrisonRoomPopulator extends RoomPopulatorAbstract {


    public PrisonRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    private static void dangleIronBarsDown(Random rand, int length, @NotNull SimpleBlock base) {
        for (int i = 0; i < length; i++) {
            base.setType(Material.CHAIN);
            base = base.getDown();
        }
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {

        int[] lowerCorner = room.getLowerCorner(1);
        int[] upperCorner = room.getUpperCorner(1);

        // Dangling chains
        for (int i = 0; i < GenUtils.randInt(12, 25); i++) {
            dangleIronBarsDown(rand, GenUtils.randInt(room.getHeight() / 4 - 1, room.getHeight() / 2), new SimpleBlock(
                    data,
                    GenUtils.randInt(lowerCorner[0], upperCorner[0]),
                    room.getY() + room.getHeight() - 1,
                    GenUtils.randInt(lowerCorner[1], upperCorner[1])
            ));
        }

        // Spawn platform
        Material[] slabs = {
                Material.STONE_BRICK_SLAB,
                Material.MOSSY_STONE_BRICK_SLAB,
                Material.STONE_SLAB,
                Material.SMOOTH_STONE_SLAB
        };

        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                data.setBlockData(x,
                        room.getY() + room.getHeight() / 2,
                        z,
                        new SlabBuilder(slabs).setType(Type.TOP).get()
                );
            }
        }

        lowerCorner = room.getLowerCorner(7);
        upperCorner = room.getUpperCorner(7);

        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                data.setType(x, room.getY() + room.getHeight() / 2, z, Material.AIR);
            }
        }

        // 4 pillars
        for (int[] coords : room.getAllCorners(6)) {
            new Wall(new SimpleBlock(data, coords[0], room.getY() + 1, coords[1])).Pillar(room.getHeight(),
                    this.rand,
                    Material.STONE_BRICKS,
                    Material.MOSSY_STONE_BRICKS,
                    Material.CRACKED_STONE_BRICKS
            );
        }

        // Walls
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 6).entrySet()) {
            Wall w = entry.getKey().getRelative(0, room.getHeight() / 2, 0);
            for (int i = 0; i < entry.getValue(); i++) {
                if (!w.isSolid()) {

                    if (i != entry.getValue() / 2) {
                        w.setType(Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
                    }
                    else {
                        w.setType(slabs);
                    }

                    BlockUtils.correctSurroundingMultifacingData(w.get());
                }

                w = w.getLeft();
            }
        }

        // Stairs leading to the second level
        BlockFace dir;
        if (room.getWidthX() > room.getWidthZ()) {
            dir = new BlockFace[] {BlockFace.WEST, BlockFace.EAST}[rand.nextInt(1)];
        }
        else {
            dir = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH}[rand.nextInt(1)];
        }

        for (int[] corner : room.getCornersAlongFace(dir, 7)) {
            Wall w = new Wall(new SimpleBlock(data, corner[0], room.getY() + 1, corner[1]), dir.getOppositeFace());

            // Remove wall at that area.
            w.getRear().getRelative(0, room.getHeight() / 2, 0).setType(Material.AIR);
            BlockUtils.correctSurroundingMultifacingData(w.getRear().getUp().get());

            for (int i = 0; i < room.getHeight() / 2; i++) {
                w.Pillar(room.getHeight() / 2 - i, rand, BlockUtils.stoneBricks);
                new StairBuilder(Material.STONE_BRICK_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS).setFacing(dir)
                                                                                                .apply(w.getRelative(0,
                                                                                                        room.getHeight()
                                                                                                        / 2 - i - 1,
                                                                                                        0
                                                                                                ));
                w = w.getFront();
            }
        }

        // Prison Cells
        for (int[] corner : room.getCornersAlongFace(BlockFace.NORTH, 2)) {
            placePrisonCell(new SimpleBlock(data, corner[0], room.getY() + 1 + room.getHeight() / 2, corner[1]),
                    BlockFace.SOUTH
            );
        }
        for (int[] corner : room.getCornersAlongFace(BlockFace.SOUTH, 2)) {
            placePrisonCell(new SimpleBlock(data, corner[0], room.getY() + 1 + room.getHeight() / 2, corner[1]),
                    BlockFace.NORTH
            );
        }

        // Iron Bars and chests
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 4).entrySet()) {
            Wall w = entry.getKey().getRelative(0, room.getHeight() / 2, 0);
            for (int i = 0; i < entry.getValue(); i++) {

                if (i == entry.getValue() / 2) { // Entrance
                    w.getUp(3).LPillar(room.getHeight() / 2 - 2, rand, BlockUtils.stoneBricks);

                    // chance for skeletons
                    if (rand.nextBoolean()) {
                        data.addEntity(w.getX(), w.getY(), w.getZ(), EntityType.SKELETON);
                    }

                }
                else if (i == entry.getValue() / 2 + 1 || i == entry.getValue() / 2 - 1) {
                    w.LPillar(room.getHeight() / 2, rand, BlockUtils.stoneBricks);
                }
                else {
                    w.LPillar(room.getHeight() / 2, rand, Material.IRON_BARS);
                    w.CorrectMultipleFacing(room.getHeight() / 2);
                }

                if (GenUtils.chance(rand, 1, 35)) {
                    new ChestBuilder(Material.CHEST).setFacing(w.getDirection())
                                                    .setLootTable(TerraLootTable.STRONGHOLD_CORRIDOR)
                                                    .apply(w.getRear(3));
                }
                w = w.getLeft();
            }
        }
    }

    public void placePrisonCell(@NotNull SimpleBlock location, @NotNull BlockFace doorDir) {
        Wall w = new Wall(location);

        Material[] prisonMats = {
                Material.STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS,
                Material.CRACKED_STONE_BRICKS,
                Material.IRON_BARS
        };

        int width = 2;

        // Walls
        for (int nx : new int[] {-width, width}) {
            for (int nz = -width; nz <= width; nz++) {
                w.getRelative(nx, 0, nz).LPillar(15, rand, prisonMats);
            }
        }

        for (int nz : new int[] {-width, width}) {
            for (int nx = -width; nx <= width; nx++) {
                w.getRelative(nx, 0, nz).LPillar(15, rand, prisonMats);
            }
        }

        SimpleBlock door = w.getRelative(doorDir, width).get();
        BlockUtils.placeDoor(door.getPopData(), Material.IRON_DOOR, door.getX(), door.getY(), door.getZ(), doorDir);
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getHeight() > 8 && room.getWidthX() > 14 && room.getWidthZ() > 14 && !room.isHuge();
    }
}
