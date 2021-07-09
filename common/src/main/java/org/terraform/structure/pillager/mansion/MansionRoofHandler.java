package org.terraform.structure.pillager.mansion;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.blockdata.TrapdoorBuilder;

import java.util.Random;

public class MansionRoofHandler {

    public static boolean isRectangle(MansionJigsawBuilder builder) {
        int[] lowestCoords = null;
        int[] highestCoords = null;
        int y = 0;
        //SimpleLocation lowestCoords = new SimpleLocation();
        //SimpleLocation highestCoords = new SimpleLocation();
        for (JigsawStructurePiece piece : builder.getPieces().values()) {
            if (lowestCoords == null) {
                y = piece.getRoom().getY();
                lowestCoords = new int[]{piece.getRoom().getX(), piece.getRoom().getZ()};
            }
            if (highestCoords == null) highestCoords = new int[]{piece.getRoom().getX(), piece.getRoom().getZ()};
            if (piece.getRoom().getX() < lowestCoords[0])
                lowestCoords[0] = piece.getRoom().getX();
            if (piece.getRoom().getZ() < lowestCoords[1])
                lowestCoords[1] = piece.getRoom().getZ();

            if (piece.getRoom().getX() > highestCoords[0])
                highestCoords[0] = piece.getRoom().getX();
            if (piece.getRoom().getZ() > highestCoords[1])
                highestCoords[1] = piece.getRoom().getZ();
        }

        //Check and see if every piece is accounted for when looping through the coords.
        int count = 0;
        for (int x = lowestCoords[0]; x <= highestCoords[0]; x += builder.getPieceWidth()) {
            for (int z = lowestCoords[1]; z <= highestCoords[1]; z += builder.getPieceWidth()) {
                if (!builder.getPieces().containsKey(new SimpleLocation(x, y, z))) {
                    return false;
                } else
                    count++;
            }
        }

        //If all pieces accounted for by looping the coords, this is a rectangle.
        return count == builder.getPieces().size();
    }


    public static void placeTentRoof(Random rand, MansionJigsawBuilder builder) {
        Axis superiorAxis = Axis.Z;
        PopulatorDataAbstract data = builder.getCore().getPopData();
        int[] lowestCoords = null;
        int[] highestCoords = null;
        int y = 0;

        for (JigsawStructurePiece piece : builder.getPieces().values()) {
            if (lowestCoords == null) {
                y = piece.getRoom().getY();
                lowestCoords = new int[]{piece.getRoom().getX(), piece.getRoom().getZ()};
            }
            if (highestCoords == null)
                highestCoords = new int[]{piece.getRoom().getX(), piece.getRoom().getZ()};
            if (piece.getRoom().getX() < lowestCoords[0])
                lowestCoords[0] = piece.getRoom().getX();
            if (piece.getRoom().getZ() < lowestCoords[1])
                lowestCoords[1] = piece.getRoom().getZ();

            if (piece.getRoom().getX() > highestCoords[0])
                highestCoords[0] = piece.getRoom().getX();
            if (piece.getRoom().getZ() > highestCoords[1])
                highestCoords[1] = piece.getRoom().getZ();
        }

        //Longer axis is the superior one
        if (highestCoords[0] - lowestCoords[0] > highestCoords[1] - lowestCoords[1])
            superiorAxis = Axis.X;
        else if (highestCoords[0] - lowestCoords[0] < highestCoords[1] - lowestCoords[1])
            superiorAxis = Axis.Z;
        else //Square house
            superiorAxis = new Axis[]{Axis.X, Axis.Z}[rand.nextInt(1)];


        lowestCoords[0] -= 3;
        lowestCoords[1] -= 3;
        highestCoords[0] += 3;
        highestCoords[1] += 3;

        Wall w;
        int length;
        int breadth;
        if (superiorAxis == Axis.X) {
            length = highestCoords[0] - lowestCoords[0] + 5;
            breadth = (highestCoords[1] - lowestCoords[1]) + 3;
            w = new Wall(new SimpleBlock(data, highestCoords[0] + 2, y + MansionJigsawBuilder.groundFloorRoomHeight - 1, lowestCoords[1] - 1), BlockFace.WEST);
        } else {
            length = highestCoords[1] - lowestCoords[1] + 5;
            breadth = (highestCoords[0] - lowestCoords[0]) + 3;
            w = new Wall(new SimpleBlock(data, lowestCoords[0] - 1, y + MansionJigsawBuilder.groundFloorRoomHeight - 1, lowestCoords[1] - 2), BlockFace.SOUTH);
        }


        for (int i = 0; i < length; i++) {
            Wall target = w;
            for (int right = 0; right < breadth; right++) {

                //Cover the holes
                if (i == 2 || i == length - 3) {
                    Material bottom = getLowestMaterial(target);
                    target.downUntilSolid(new Random(), bottom);
                    //target.CorrectMultipleFacing(1);
                }

                //Place logs at the sides
                if (right != 0 && right != breadth - 1) {
                    //Sandwiched by trapdoors
                    if (i == 0) {
                        new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR)
                                .setHalf(Half.TOP)
                                .setOpen(true)
                                .setFacing(target.getDirection().getOppositeFace())
                                .apply(target.getRelative(0, -1, 0));
                    } else if (i == length - 1) {
                        new TrapdoorBuilder(Material.DARK_OAK_TRAPDOOR)
                                .setHalf(Half.TOP)
                                .setOpen(true)
                                .setFacing(target.getDirection())
                                .apply(target.getRelative(0, -1, 0));
                    } else {
                        new OrientableBuilder(Material.DARK_OAK_LOG)
                                .setAxis(superiorAxis)
                                .apply(target.getRelative(0, -1, 0).get());
                    }
                }

                Material[] stairType = {Material.DARK_OAK_STAIRS};
                Material[] slabType = {Material.COBBLESTONE_SLAB, Material.MOSSY_COBBLESTONE_SLAB};

                if (right == 0 || right == breadth - 1 || i == 0 || i == length - 1) {
                    stairType = new Material[]{Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS};
                }

                if (breadth % 2 == 1) { //For odd breadth.
                    if (right > breadth / 2) {
                        //Slope down
                        new StairBuilder(stairType)
                                .setFacing(BlockUtils.getLeft(target.getDirection()))
                                .apply(target);
                        target = target.getRight().getRelative(0, -1, 0);
                    } else if (right < breadth / 2) {
                        //Slope up
                        new StairBuilder(stairType)
                                .setFacing(BlockUtils.getRight(target.getDirection()))
                                .apply(target);
                        target = target.getRight().getRelative(0, 1, 0);
                    } else {
                        //Top (Only exists when the breadth is odd.
                        target.setType(slabType);
                        target = target.getRight().getRelative(0, -1, 0);
                    }
                } else { //For even breadth
                    if (right == breadth / 2 - 1) {
                        new StairBuilder(stairType)
                                .setFacing(BlockUtils.getRight(target.getDirection()))
                                .apply(target);
                        target = target.getRight();
                    } else if (right >= breadth / 2) {
                        //Slope down
                        new StairBuilder(stairType)
                                .setFacing(BlockUtils.getLeft(target.getDirection()))
                                .apply(target);
                        target = target.getRight().getRelative(0, -1, 0);
                    } else if (right < breadth / 2) {
                        //Slope up
                        new StairBuilder(stairType)
                                .setFacing(BlockUtils.getRight(target.getDirection()))
                                .apply(target);
                        target = target.getRight().getRelative(0, 1, 0);
                    }
                }
            }
            w = w.getFront();
        }


    }

    private static Material getLowestMaterial(Wall w) {
        Wall other = w.findFloor(10);
        if (other != null) return other.getType();
        return null;
    }

}
