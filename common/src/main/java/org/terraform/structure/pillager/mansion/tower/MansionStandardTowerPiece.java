package org.terraform.structure.pillager.mansion.tower;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Random;

public class MansionStandardTowerPiece extends JigsawStructurePiece {

    private final MansionJigsawBuilder builder;
    boolean isHighestPieceInTower = false;

    public MansionStandardTowerPiece(MansionJigsawBuilder builder,
                                     int widthX,
                                     int height,
                                     int widthZ,
                                     JigsawType type,
                                     BlockFace[] validDirs)
    {
        super(widthX, height, widthZ, type, validDirs);
        this.builder = builder;
    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, Random rand) {
        int[] lowerCorner = this.getRoom().getLowerCorner(0);
        int[] upperCorner = this.getRoom().getUpperCorner(0);

        // Place ground
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                data.setType(x, this.getRoom().getY(), z, Material.DARK_OAK_PLANKS);

            }
        }
    }

    public void decorateAwkwardCorners(Random random)
    {
        // Make sure awkward corners are fixed
        SimpleBlock core = new SimpleBlock(builder.getCore().getPopData(),
                getRoom().getX(),
                getRoom().getY(),
                getRoom().getZ()
        );
        Wall target;

        if (getWalledFaces().contains(BlockFace.NORTH) && getWalledFaces().contains(BlockFace.WEST)) { // nw
            target = new Wall(core.getRelative(-4, 1, -4));
            decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.WEST);
        }
        if (getWalledFaces().contains(BlockFace.NORTH) && getWalledFaces().contains(BlockFace.EAST)) { // ne
            target = new Wall(core.getRelative(4, 1, -4));
            decorateAwkwardCorner(target, random, BlockFace.NORTH, BlockFace.EAST);
        }
        if (getWalledFaces().contains(BlockFace.SOUTH) && getWalledFaces().contains(BlockFace.WEST)) { // sw
            target = new Wall(core.getRelative(-4, 1, 4));
            decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.WEST);
        }
        if (getWalledFaces().contains(BlockFace.SOUTH) && getWalledFaces().contains(BlockFace.EAST)) { // se
            target = new Wall(core.getRelative(4, 1, 4));
            decorateAwkwardCorner(target, random, BlockFace.SOUTH, BlockFace.EAST);
        }
    }

    public void decorateAwkwardCorner(@NotNull Wall target, Random random, BlockFace one, BlockFace two) {
        // Fill in gap in the corner
        target.Pillar(MansionJigsawBuilder.roomHeight, Material.STONE_BRICKS);
    }

    public void placeTentRoof(@NotNull PopulatorDataAbstract data, BlockFace roofFacing, Random random) {
        Wall core = new Wall(this.getRoom()
                                 .getCenterSimpleBlock(data)
                                 .getRelative(0, MansionJigsawBuilder.roomHeight, 0),
                roofFacing
        ).getFront(1 + (MansionJigsawBuilder.groundFloorRoomWidth / 2));

        for (BlockFace dir : BlockUtils.getAdjacentFaces(core.getDirection())) {
            // Index from the side
            for (int i = 0; i <= 2 + (MansionJigsawBuilder.groundFloorRoomWidth / 2); i++) {
                for (int depth = 0; depth < 2 + MansionJigsawBuilder.groundFloorRoomWidth; depth++) {
                    Wall w = core.getRear(depth).getRelative(dir, i);

                    if (i == 0) { // Center
                        w.getUp(5).Pillar(2, Material.COBBLESTONE);
                        w.getUp(7).setType(Material.COBBLESTONE_WALL);
                        w.getUp(7).CorrectMultipleFacing(1);
                    }
                    else if (i == 1) {
                        new StairBuilder(getStairs(i, depth)).setFacing(dir.getOppositeFace()).apply(w.getUp(5));
                        w.getUp(3).Pillar(2, getBlock(i, depth));
                    }
                    else if (i == 2) {
                        new StairBuilder(getStairs(i, depth)).setFacing(dir.getOppositeFace()).apply(w.getUp(3));
                        w.getUp(2).setType(getBlock(i, depth));
                    }
                    else if (i == 3) {
                        new StairBuilder(getStairs(i, depth)).setFacing(dir.getOppositeFace()).apply(w.getUp(2));

                        new SlabBuilder(getSlab(i, depth)).setType(Type.TOP).apply(w.getUp());
                    }
                    else if (i == 4) {
                        w.getUp().setType(getBlock(i, depth));
                    }
                    else if (i == 5) {
                        w.getUp().setType(getSlab(i, depth));
                        new SlabBuilder(getSlab(i, depth)).setType(Type.TOP).apply(w);
                    }
                    else // Final
                    {
                        new SlabBuilder(getSlab(i, depth)).setType(Type.TOP).apply(w);
                    }

                    // Join the walls to the roof
                    if (i <= 3) {
                        if (depth == 1 || depth == MansionJigsawBuilder.groundFloorRoomWidth) {
                            w.getUp().setType(Material.DARK_OAK_PLANKS);
                            w.getUp(2).LPillar(4, new Random(), Material.DARK_OAK_PLANKS);
                        }
                    }

                }
            }
        }

    }

    //	i <= 2+(MansionJigsawBuilder.groundFloorRoomWidth/2
    //	depth < 2 + MansionJigsawBuilder.groundFloorRoomWidth

    private @NotNull Material getStairs(int i, int depth) {
        if (i == 0
            || i == 2 + (MansionJigsawBuilder.groundFloorRoomWidth / 2)
            || depth == 0
            || depth == 1 + MansionJigsawBuilder.groundFloorRoomWidth)
        {
            return Material.COBBLESTONE_STAIRS;
        }
        return Material.DARK_OAK_STAIRS;
    }

    private @NotNull Material getSlab(int i, int depth) {
        if (i == 0
            || i == 2 + (MansionJigsawBuilder.groundFloorRoomWidth / 2)
            || depth == 0
            || depth == 1 + MansionJigsawBuilder.groundFloorRoomWidth)
        {
            return Material.COBBLESTONE_SLAB;
        }
        return Material.DARK_OAK_SLAB;
    }

    private @NotNull Material getBlock(int i, int depth) {
        if (i == 0
            || i == 2 + (MansionJigsawBuilder.groundFloorRoomWidth / 2)
            || depth == 0
            || depth == 1 + MansionJigsawBuilder.groundFloorRoomWidth)
        {
            return Material.COBBLESTONE;
        }
        return Material.DARK_OAK_PLANKS;
    }

    public boolean isHighestPieceInTower() {
        return isHighestPieceInTower;
    }

    public void setHighestPieceInTower(boolean isHighestPieceInTower) {
        this.isHighestPieceInTower = isHighestPieceInTower;
    }


}
