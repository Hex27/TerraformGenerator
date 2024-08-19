package org.terraform.structure.pillager.mansion.ground;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.pillager.mansion.MansionStandardRoomPiece;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Map.Entry;
import java.util.Random;

public abstract class MansionStandardGroundRoomPiece extends MansionStandardRoomPiece {

    public MansionStandardGroundRoomPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, @NotNull Random rand) {
        int[] lowerCorner = this.getRoom().getLowerCorner(0);
        int[] upperCorner = this.getRoom().getUpperCorner(0);

        // Place flooring.
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {

                data.setType(x, this.getRoom().getY(), z, Material.STONE_BRICKS);

                // Supporting ground
                new Wall(new SimpleBlock(data, x, this.getRoom().getY() - 1, z)).downUntilSolid(rand,
                        Material.STONE_BRICKS
                );
            }
        }
    }

    /**
     * For forcefully removing anything in the area before any building.
     */
    public void purgeMinimalArea(@NotNull PopulatorDataAbstract data) {

        int[] lowerCorner = this.getRoom().getLowerCorner(-5);
        int[] upperCorner = this.getRoom().getUpperCorner(-5);

        // Places extended ground and roofing over the piece.
        // If this piece is identified as a corner, place a pillar.
        for (int nx = lowerCorner[0]; nx <= upperCorner[0]; nx++) {
            for (int nz = lowerCorner[1]; nz <= upperCorner[1]; nz++) {
                SimpleBlock b = new SimpleBlock(data, nx, this.getRoom().getY(), nz);
                new Wall(b).Pillar(8, Material.AIR);
            }
        }
    }

    @Override
    public void postBuildDecoration(Random random, @NotNull PopulatorDataAbstract data) {
        CubeRoom targetRoom = this.getRoom();
        if (!this.getWalledFaces().isEmpty()) {
            targetRoom = this.getExtendedRoom(6);
        }

        int[] lowerCorner = targetRoom.getLowerCorner(0);
        int[] upperCorner = targetRoom.getUpperCorner(0);

        // Places extended ground and roofing over the piece.
        // If this piece is identified as a corner, place a pillar.
        for (int nx = lowerCorner[0]; nx <= upperCorner[0]; nx++) {
            for (int nz = lowerCorner[1]; nz <= upperCorner[1]; nz++) {
                SimpleBlock b = new SimpleBlock(data, nx, targetRoom.getY(), nz);

                // Edge Piece of extended room. Place cobblestone on ceiling edges
                if (upperCorner[0] - lowerCorner[0] > this.getRoom().getWidthX() && (nx == lowerCorner[0]
                                                                                     || nx == upperCorner[0]
                                                                                     || nz == lowerCorner[1]
                                                                                     || nz == upperCorner[1]))
                {
                    if (b.getRelative(0, targetRoom.getHeight() + 1, 0).getType() != Material.STONE_BRICKS) {
                        b.getRelative(0, targetRoom.getHeight() + 1, 0).setType(Material.COBBLESTONE);
                    }
                }
                else {
                    if (b.getType() != Material.STONE_BRICKS) {

                        b.setType(Material.COBBLESTONE);
                    }

                    new Wall(b.getDown()).downUntilSolid(new Random(),
                            Material.COBBLESTONE,
                            Material.COBBLESTONE,
                            Material.COBBLESTONE,
                            Material.MOSSY_COBBLESTONE
                    );

                    b.getRelative(0, targetRoom.getHeight() + 1, 0).setType(Material.STONE_BRICKS);
                }
            }
        }
    }

    /**
     * Mansions are complex, and they need a third pass to properly ensure that
     * previous important details were planted before placing the final edge pieces.
     */
    public void thirdStageDecoration(Random random, @NotNull PopulatorDataAbstract data) {
        CubeRoom targetRoom;
        if (!this.getWalledFaces().isEmpty()) {
            targetRoom = this.getExtendedRoom(6);
            // Decorate the top and bottom areas
            for (Entry<Wall, Integer> entry : this.getRoom().getFourWalls(data, -5).entrySet()) {
                if (this.getWalledFaces().contains(entry.getKey().getDirection().getOppositeFace())) {
                    Wall w = entry.getKey();
                    for (int i = 0; i < entry.getValue(); i++) {
                        if (w.getRear().getRelative(0, targetRoom.getHeight(), 0).getType() == Material.COBBLESTONE) {
                            // Lower decorations
                            if (!w.isSolid() || w.getType() == Material.POLISHED_ANDESITE) {

                                new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(BlockUtils.getRight(w.getDirection()))
                                                                             .setHalf(Half.BOTTOM)
                                                                             .apply(w);
                                new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(BlockUtils.getRight(w.getDirection()))
                                                                             .setHalf(Half.TOP)
                                                                             .apply(w.getUp());

                                w.getLeft().get().lsetType(Material.POLISHED_ANDESITE);
                                w.getLeft().getUp().get().lsetType(Material.POLISHED_ANDESITE);
                                w.getRight().getUp().get().lsetType(Material.POLISHED_ANDESITE);
                                w.getRight().get().lsetType(Material.POLISHED_ANDESITE);
                            }
                            // Upper decorations
                            if (i % 2 == 0) {
                                w.getRear().getRelative(0, targetRoom.getHeight() + 1, 0).setType(Material.COBBLESTONE);
                            }
                            else {
                                new DirectionalBuilder(Material.DARK_OAK_FENCE_GATE).setFacing(w.getDirection())
                                                                                    .apply(w.getRear()
                                                                                            .getRelative(0,
                                                                                                    targetRoom.getHeight()
                                                                                                    + 1,
                                                                                                    0));
                            }
                        }

                        w = w.getLeft();
                    }
                }
            }
        }
    }
}
