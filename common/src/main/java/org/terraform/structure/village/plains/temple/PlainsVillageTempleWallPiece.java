package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Random;

public class PlainsVillageTempleWallPiece extends JigsawStructurePiece {


    private static final List<Material> BRIGHT_STAINED_GLASS_PANES = List.of(
        Material.BLUE_STAINED_GLASS_PANE,
        Material.CYAN_STAINED_GLASS_PANE,
        Material.LIGHT_BLUE_STAINED_GLASS_PANE,
        Material.LIME_STAINED_GLASS_PANE,
        Material.MAGENTA_STAINED_GLASS_PANE,
        Material.PINK_STAINED_GLASS_PANE,
        Material.PURPLE_STAINED_GLASS_PANE,
        Material.RED_STAINED_GLASS_PANE,
        Material.YELLOW_STAINED_GLASS_PANE
    );


    public PlainsVillageTempleWallPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
    }

    public static void setLargeWindow(@NotNull PopulatorDataAbstract data,
                                      @NotNull BlockFace rotation,
                                      @NotNull CubeRoom room,
                                      @NotNull BlockFace face)
    {
        Material pane;
        Wall w = new Wall(new SimpleBlock(data, room.getX(), room.getY() + 2, room.getZ()), rotation);
        w = w.getRelative(rotation.getOppositeFace(), 2).getRelative(face, 2);

        // Remove roof ledge for windows
        w.getRear().getUp(3).Pillar(3, new Random(), Material.AIR);
        w.getRear(2).getUp(3).Pillar(4, new Random(), Material.AIR);

        // Interior stair decor
        new StairBuilder(Material.POLISHED_DIORITE_STAIRS).setHalf(Half.TOP)
                                                          .setFacing(w.getDirection())
                                                          .apply(w.getRear().getUp(5))
                                                          .apply(w.getRear(2).getUp(6));

        // Place the window
        if (BRIGHT_STAINED_GLASS_PANES.contains(w.getRelative(face).getType())) {
            pane = w.getRelative(face).getType();
        }
        else {
            pane = BRIGHT_STAINED_GLASS_PANES.get(new Random().nextInt(BRIGHT_STAINED_GLASS_PANES.size()));
        }

        w.Pillar(5, new Random(), pane);
        w.getDown()
         .getRelative(face.getOppositeFace())
         .Pillar(6,
                 new Random(),
                 Material.POLISHED_DIORITE
         );// Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
        w.CorrectMultipleFacing(5);

        // Decorate the sides
        w = w.getRelative(face.getOppositeFace()).getFront().getUp();
        new SlabBuilder(Material.STONE_BRICK_SLAB).setType(Slab.Type.TOP).apply(w);
        w = w.getUp();
        w.Pillar(2, new Random(), Material.COBBLESTONE_WALL);
        w.CorrectMultipleFacing(2);
        w = w.getUp(2);

        new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(face)
                                                     .apply(w)
                                                     .apply(w.getRear().getUp())
                                                     .apply(w.getRear(2).getUp(2))
                                                     .apply(w.getUp().getRelative(face))
                                                     .apply(w.getRear().getUp().getRelative(face))
                                                     .apply(w.getRear().getUp(2).getRelative(face))
                                                     .apply(w.getRear(2).getUp(3).getRelative(face));

        w.getUp().getRelative(face).getRear().setType(Material.CHISELED_STONE_BRICKS);
        w.getUp().getRear(2).setType(Material.CHISELED_STONE_BRICKS);
        w.getUp(2).getRelative(face).getRear(2).setType(Material.CHISELED_STONE_BRICKS);
        w.getUp(3).getRelative(face).getRear(3).setType(Material.CHISELED_STONE_BRICKS);


    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, @NotNull Random rand) {
        Material[] stoneBricks = {
                Material.STONE_BRICKS,
                Material.STONE_BRICKS,
                Material.STONE_BRICKS,
                Material.CRACKED_STONE_BRICKS
        };
        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getDown();
        Wall core = null;
        for (int i = 0; i < entry.getValue(); i++) {
            w.getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.Pillar(5, rand, stoneBricks);

            if (i == 2) {
                core = w;
                setTempleWindows(w);
            }
            w = w.getLeft();
        }

        assert core != null;

        new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(core.getDirection().getOppositeFace())
                                                     .apply(core.getFront());
        core.getFront().getDown().downUntilSolid(rand, stoneBricks);

        Wall doorAdj = core.getFront().getRight();
        if (!doorAdj.getUp(2).isSolid()) {
            new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(doorAdj.getDirection().getOppositeFace())
                                                         .apply(doorAdj.getUp(2));
            doorAdj.getUp().downUntilSolid(rand, stoneBricks);
        }
        else {
            doorAdj.getUp(2).setType(Material.CHISELED_STONE_BRICKS);
            doorAdj.getUp(3).setType(Material.STONE_BRICK_WALL);
        }

        doorAdj = core.getFront().getLeft();
        new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(doorAdj.getDirection().getOppositeFace())
                                                     .apply(doorAdj.getUp(2));
        doorAdj.getUp().downUntilSolid(rand, stoneBricks);

    }

    private void setTempleWindows(Wall w) {
        Material pane = BRIGHT_STAINED_GLASS_PANES.get(new Random().nextInt(BRIGHT_STAINED_GLASS_PANES.size()));
        w = w.getUp();
        new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(w.getDirection().getOppositeFace())
                                                     .apply(w)
                                                     .setHalf(Half.TOP)
                                                     .apply(w.getUp(3));
        w.getUp().downUntilSolid(new Random(), Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);

        w.getUp().Pillar(2, new Random(), pane);
        w.getUp().CorrectMultipleFacing(2);

    }
}
