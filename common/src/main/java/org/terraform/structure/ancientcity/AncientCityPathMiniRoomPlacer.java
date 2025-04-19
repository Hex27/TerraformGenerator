package org.terraform.structure.ancientcity;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Random;


public class AncientCityPathMiniRoomPlacer {

    public static void placeAltar(@NotNull Wall origin) {
        Material[] deepSlateBricks = new Material[] {Material.DEEPSLATE_BRICKS, Material.CRACKED_DEEPSLATE_BRICKS};
        // Place cylinder below
        cylinderDown(origin, 4, deepSlateBricks);

        cylinderDown(origin.getUp(), 3, deepSlateBricks);

        // Place stairs leading to altar
        new StairBuilder(Material.DEEPSLATE_BRICK_STAIRS).setFacing(origin.getDirection())
                                                         .apply(origin.getRear(2).getUp())
                                                         .apply(origin.getRear().getLeft().getUp())
                                                         .apply(origin.getRear().getRight().getUp());

        origin.getRear(3).getUp().fluidize();
        origin.getRear(2).getUp().getLeft().fluidize();
        origin.getRear(2).getUp().getRight().fluidize();

        // Place the actual altar
        Wall altarCore = origin.getFront(2).getUp(2);
        altarCore.setType(deepSlateBricks);
        altarCore.getLeft().setType(deepSlateBricks);
        altarCore.getRight().setType(deepSlateBricks);

        new StairBuilder(Material.DEEPSLATE_BRICK_STAIRS).setFacing(altarCore.getDirection())
                                                         .apply(altarCore.getRear())
                                                         .setFacing(altarCore.getDirection().getOppositeFace())
                                                         .apply(altarCore.getFront());

        altarCore.getFront().getDown().setType(deepSlateBricks);
        altarCore.getRear().getLeft().setType(Material.DEEPSLATE_BRICK_SLAB);
        altarCore.getRear().getRight().setType(Material.DEEPSLATE_BRICK_SLAB);
        altarCore.getLeft(2).setType(Material.DEEPSLATE_BRICK_SLAB);
        altarCore.getRight(2).setType(Material.DEEPSLATE_BRICK_SLAB);

        altarCore.getUp().Pillar(2, Material.COBBLED_DEEPSLATE_WALL);
        altarCore.getUp().getLeft().Pillar(2, Material.COBBLED_DEEPSLATE_WALL);
        altarCore.getUp().getRight().Pillar(2, Material.COBBLED_DEEPSLATE_WALL);

        altarCore.getUp(3).setType(Material.DEEPSLATE_BRICK_SLAB);
        altarCore.getUp(3).getLeft().setType(Material.DEEPSLATE_BRICK_SLAB);
        altarCore.getUp(3).getRight().setType(Material.DEEPSLATE_BRICK_SLAB);

        for (BlockFace adj : BlockUtils.getAdjacentFaces(altarCore.getDirection())) {
            new StairBuilder(Material.DEEPSLATE_BRICK_STAIRS).setFacing(adj.getOppositeFace())
                                                             .apply(altarCore.getRelative(adj, 2).getUp(2))
                                                             .setHalf(Half.TOP)
                                                             .apply(altarCore.getRelative(adj, 2).getUp());
        }

    }

    private static void cylinderDown(@NotNull SimpleBlock core, int radius, Material... mat) {
        for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
                SimpleBlock rel = core.getRelative(Math.round(x), 0, Math.round(z));
                // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);

                double equationResult = Math.pow(x, 2) / Math.pow(radius, 2) + Math.pow(z, 2) / Math.pow(radius, 2);

                if (equationResult <= 1.0) {
                    rel.downUntilSolid(new Random(), mat);
                }
            }
        }
    }

}
