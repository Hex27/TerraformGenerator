package org.terraform.structure.stronghold;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rotatable;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.Random;

public class PrisonRoomPopulator extends RoomPopulatorAbstract {

    public PrisonRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    private static void dangleIronBarsDown(Random rand, SimpleBlock base) {
        int length = GenUtils.randInt(rand, 1, 4);
        for (int i = 0; i < length; i++) {
            base.setType(Material.IRON_BARS);
            base = base.getRelative(0, -1, 0);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
        int[] upperBounds = room.getUpperCorner();
        int[] lowerBounds = room.getLowerCorner();

        //Wall object, to the length of the wall
        Entry<Wall, Integer>[] walls = new Entry[4];
        Wall north = new Wall(
                new SimpleBlock(data, lowerBounds[0] + 3, room.getY() + 1, upperBounds[1] - 3)
                , BlockFace.NORTH);
        Wall south = new Wall(
                new SimpleBlock(data, upperBounds[0] - 3, room.getY() + 1, lowerBounds[1] + 3)
                , BlockFace.SOUTH);
        Wall east = new Wall(
                new SimpleBlock(data, lowerBounds[0] + 3, room.getY() + 1, lowerBounds[1] + 3)
                , BlockFace.EAST);
        Wall west = new Wall(
                new SimpleBlock(data, upperBounds[0] - 3, room.getY() + 1, upperBounds[1] - 3)
                , BlockFace.WEST);

        walls[0] = new AbstractMap.SimpleEntry(north, room.getWidthX() - 6);
        walls[1] = new AbstractMap.SimpleEntry(south, room.getWidthX() - 6);
        walls[2] = new AbstractMap.SimpleEntry(east, room.getWidthZ() - 6);
        walls[3] = new AbstractMap.SimpleEntry(west, room.getWidthZ() - 6);

        //Initially, place a massive cell
        for (Entry<Wall, Integer> entry : walls) {
            Wall wall = entry.getKey().clone();

            for (int l = 0; l < entry.getValue(); l++) {
                wall.LPillar(room.getHeight(), rand, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
                //Some prison bars
                if (GenUtils.chance(rand, 2, 10)) {
                    wall.getRelative(0, 1, 0).setType(Material.IRON_BARS);
                    BlockUtils.correctSurroundingMultifacingData(wall.getRelative(0, 2, 0).get());
                }

                wall = wall.getLeft();
            }
        }

        //Decorate the inside of the cell
        int floor = room.getY() + 1;
        for (int x = lowerBounds[0] + 4; x <= upperBounds[0] - 4; x++) {
            for (int z = lowerBounds[1] + 4; z <= upperBounds[1] - 4; z++) {
                if (GenUtils.chance(rand, 1, 15)) {
                    data.setType(x, floor, z, Material.REDSTONE_WIRE);
                } else if (GenUtils.chance(rand, 1, 15)) {
                    data.setType(x, floor, z, Material.SKELETON_SKULL);
                    Rotatable skeleSkull = (Rotatable) Bukkit.createBlockData(Material.SKELETON_SKULL);
                    skeleSkull.setRotation(BlockUtils.getXZPlaneBlockFace(rand));
                    data.setBlockData(x, floor, z, skeleSkull);
                }
                if (GenUtils.chance(rand, 1, 15)) {
                    SimpleBlock ceiling = new SimpleBlock(data, x, room.getY() + room.getHeight() - 1, z);
                    dangleIronBarsDown(rand, ceiling);
                }

            }
        }

        //Now, separate the cell into multiple cells.
        int index = GenUtils.randInt(rand, 0, 1);
        Entry<Wall, Integer> entry = walls[index];
        Wall w = entry.getKey().clone();
        int bigCellLength = entry.getValue();

        //First partition
        int cellWidth = 5;
        int c = 0;
        for (int i = 0; i < bigCellLength; i++) {
            if (c >= cellWidth) {
                Wall front = w.getFront();
                c = 0;
                while (!front.get().getType().toString().endsWith("STONE_BRICKS")) {
                    front.LPillar(room.getHeight(), rand, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
                    front = front.getFront();
                }
            } else {
                c++;
            }
            w = w.getLeft();
        }

        //Split the cell into 3 parts
        index += 2;
        if (index > 3) index = index - 3;
        entry = walls[index];
        w = entry.getKey().clone();
        int bigCellWidth = entry.getValue();
        int splitOne = bigCellWidth / 3;
        int splitTwo = splitOne * 2;

        for (int i = 0; i < bigCellWidth; i++) {
            //Two main splits
            if (i == splitOne || i == splitTwo) {
                Wall front = w.getFront();
                for (int cor = 0; cor < bigCellLength - 1; cor++) {
                    front.LPillar(room.getHeight(), rand, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);

                    //Two blocks of iron bars
                    front.setType(Material.IRON_BARS);
                    front.getRelative(0, 1, 0).setType(Material.IRON_BARS);
                    BlockUtils.correctSurroundingMultifacingData(front.get());
                    BlockUtils.correctSurroundingMultifacingData(front.get().getRelative(0, 1, 0));

                    //Doors.
                    if ((front.getFront().getRight().get().getType().isSolid()
                            && !front.getRight().get().getType().isSolid())
                            || (front.getFront().getLeft().get().getType().isSolid()
                            && !front.getLeft().get().getType().isSolid())) {
                        BlockFace dir = front.getDirection();
                        if (i == splitTwo) dir = dir.getOppositeFace();
                        BlockUtils.placeDoor(data, Material.IRON_DOOR, front.getRight().get().getX(), front.get().getY(), front.get().getZ(), dir);
                    }
                    front = front.getFront();
                }

                //Make corridor
            } else if (i > splitOne && i < splitTwo) {
                Wall front = w.clone();
                for (int cor = 0; cor <= bigCellLength; cor++) {
                    front.Pillar(room.getHeight() - 1, rand, Material.AIR);
                    front = front.getFront();
                }
            }
            w = w.getLeft();
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return room.isBig() && !room.isHuge();
    }
}
