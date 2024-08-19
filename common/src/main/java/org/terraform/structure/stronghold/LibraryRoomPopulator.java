package org.terraform.structure.stronghold;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public class LibraryRoomPopulator extends RoomPopulatorAbstract {

    public LibraryRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        int[] upperBounds = room.getUpperCorner();
        int[] lowerBounds = room.getLowerCorner();

        HashMap<Wall, Integer> walls = room.getFourWalls(data, 1);
        // Bookshelves and entrance decor
        for (Entry<Wall, Integer> entry : walls.entrySet()) {
            Wall wall = entry.getKey().clone();
            int other = 0;
            for (int i = 0; i < entry.getValue(); i++) {
                // Tis' an entrance. Don't cover. Decorate it a bit.
                if (!wall.getRear().get().getType().toString().endsWith("STONE_BRICKS")) {
                    Wall temp = wall.getUp();
                    for (int t = 0; t < room.getHeight(); t++) {
                        temp = temp.getUp();
                        if (temp.getRear().get().getType().toString().endsWith("STONE_BRICKS")) {
                            break;
                        }
                    }
                    temp.setType(Material.CHISELED_STONE_BRICKS);
                    temp.getUp().LPillar(room.getHeight(), rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);


                }
                else { // If it isn't an entrance, make bookshelves
                    if (other <= 2) {
                        wall.LPillar(room.getHeight(), rand, Material.BOOKSHELF);
                        other++;
                    }
                    else {
                        other = 0;
                        wall.LPillar(room.getHeight(), rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
                        wall.getFront()
                            .LPillar(room.getHeight(), rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
                    }
                }
                wall = wall.getLeft();
            }
        }

        int pHeight = room.getHeight() / 2; // Platform height

        // Platforms for second level
        for (Entry<Wall, Integer> entry : walls.entrySet()) {
            Wall wall = entry.getKey().clone();
            for (int l = 0; l < entry.getValue(); l++) {
                Wall pWall = wall.getFront().getRelative(0, pHeight - 1, 0);
                if (pWall.get().getType().toString().contains("COBBLE")) {
                    // Make a cobble prop
                    for (int i = 0; i < 3; i++) {
                        pWall = pWall.getFront();
                        SimpleBlock front = pWall.get();
                        if (front.lsetType(Material.OAK_LOG)) {
                            Orientable o = (Orientable) Bukkit.createBlockData(Material.OAK_LOG);
                            o.setAxis(BlockUtils.getAxisFromBlockFace(wall.getDirection()));
                            front.setBlockData(o);
                        }
                    }
                }
                else {

                    // Spawn loot chest
                    if (GenUtils.chance(rand, 5, 100)) {
                        SimpleBlock cBlock = pWall.getUp().get();
                        cBlock.setType(Material.CHEST);

                        org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) Bukkit.createBlockData(
                                Material.CHEST);
                        chest.setFacing(pWall.getDirection());
                        cBlock.setBlockData(chest);
                        data.lootTableChest(
                                cBlock.getX(),
                                cBlock.getY(),
                                cBlock.getZ(),
                                TerraLootTable.STRONGHOLD_LIBRARY
                        );
                    }
                    // Place slabs
                    for (int i = 0; i < 3; i++) {
                        SimpleBlock front = pWall.get();
                        if (!front.lsetType(Material.OAK_SLAB)) {
                            pWall = pWall.getFront();
                            continue;
                        }
                        Slab s = (Slab) Bukkit.createBlockData(Material.OAK_SLAB);
                        s.setType(Type.TOP);
                        front.setBlockData(s);

                        pWall = pWall.getFront();
                    }
                }
                wall = wall.getLeft();
            }
        }

        // Fences
        walls.clear();
        walls = room.getFourWalls(data, 4);

        for (Entry<Wall, Integer> entry : walls.entrySet()) {
            Wall wall = entry.getKey().clone().getRelative(0, pHeight, 0);
            for (int l = 0; l < entry.getValue(); l++) {
                wall.setType(Material.OAK_FENCE);
                BlockUtils.correctSurroundingMultifacingData(wall.get());
                if (GenUtils.chance(rand, 1, 10)) {
                    wall.getUp().setType(Material.TORCH);
                }
                wall = wall.getLeft();
            }
        }

        // Stairway generation
        // Wall object, to the length of the wall
        ArrayList<Wall> stairWalls = new ArrayList<>();
        Wall stairWallOne = new Wall(new SimpleBlock(
                data,
                lowerBounds[0] + 5,
                room.getY() + pHeight,
                upperBounds[1] - 5
        ), BlockFace.NORTH);
        Wall stairWallTwo = new Wall(new SimpleBlock(
                data,
                upperBounds[0] - 5,
                room.getY() + pHeight,
                lowerBounds[1] + 5
        ), BlockFace.SOUTH);
        Wall stairWallThree = new Wall(new SimpleBlock(
                data,
                lowerBounds[0] + 5,
                room.getY() + pHeight,
                lowerBounds[1] + 5
        ), BlockFace.EAST);
        Wall stairWallFour = new Wall(new SimpleBlock(
                data,
                upperBounds[0] - 5,
                room.getY() + pHeight,
                upperBounds[1] - 5
        ), BlockFace.WEST);

        stairWalls.add(stairWallOne);
        stairWalls.add(stairWallTwo);
        stairWalls.add(stairWallThree);
        stairWalls.add(stairWallFour);

        Collections.shuffle(stairWalls, rand);
        int i = GenUtils.randInt(rand, 1, 4);
        for (int s = 0; s < i; s++) {
            Wall stairWall = stairWalls.get(s);
            // Remove the fences there
            stairWall.getRight().getUp().setType(Material.AIR);
            stairWall.getFront().getRight().getUp().setType(Material.AIR);

            // Place stairs
            while (stairWall.get().getY() > room.getY()) {
                stairWall.setType(Material.OAK_STAIRS);
                Stairs d = (Stairs) Bukkit.createBlockData(Material.OAK_STAIRS);
                d.setFacing(BlockUtils.getAdjacentFaces(stairWall.getDirection())[1]);
                stairWall.get().setBlockData(d);

                stairWall.getFront().setType(Material.OAK_STAIRS);
                stairWall.getFront().get().setBlockData(d);

                stairWall = stairWall.getLeft().getDown();
            }
        }

        SimpleBlock core = new SimpleBlock(data, room.getX(), room.getY() + 1, room.getZ());

        // Generate central bookpillar
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                for (int ny = 0; ny < room.getHeight() - 1; ny++) {
                    if (ny == pHeight || ny == 0 || ny == room.getHeight() - 2) {
                        core.getRelative(nx, ny, nz).setType(Material.CHISELED_STONE_BRICKS);
                        continue;
                    }
                    core.getRelative(nx, ny, nz).setType(Material.IRON_BARS);
                    BlockUtils.correctSurroundingMultifacingData(core.getRelative(nx, ny, nz));
                }
            }
        }

        BlockUtils.spawnPillar(rand,
                data,
                room.getX(),
                room.getY() + 1,
                room.getZ(),
                Material.BOOKSHELF,
                room.getHeight() - 2,
                room.getHeight() - 2
        );
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.isHuge();
    }
}
