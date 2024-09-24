package org.terraform.structure.village.plains.forge;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PlainsVillageForgeMasonPiece extends PlainsVillageForgeStandardPiece {

    public PlainsVillageForgeMasonPiece(PlainsVillagePopulator plainsVillagePopulator,
                                        int widthX,
                                        int height,
                                        int widthZ,
                                        JigsawType type,
                                        BlockFace[] validDirs)
    {
        super(plainsVillagePopulator, widthX, height, widthZ, type, validDirs);
    }

    // Use postBuildDecoration.
    @Override
    public void postBuildDecoration(@NotNull Random random, @NotNull PopulatorDataAbstract data) {
        SimpleBlock core = new SimpleBlock(data, this.getRoom().getX(), this.getRoom().getY(), this.getRoom().getZ());
        if (this.getWalledFaces().isEmpty()) {
            spawnCenteredPileOfRocks(random, new Wall(core));
        }

        if (this.getWalledFaces().size() == 1) {
            if (core.getRelative(this.getWalledFaces().get(0), 3).getType() == Material.CHISELED_STONE_BRICKS) {
                spawnCenteredPileOfRocks(random, new Wall(core));
                return;
            }
        }
        ArrayList<BlockFace> walledFaces = this.getWalledFaces();
        Collections.shuffle(walledFaces);
        for (BlockFace face : walledFaces) {

            // Don't spawn stone pile against entrance
            if (core.getRelative(face, 3).getType() == Material.CHISELED_STONE_BRICKS) {
                continue;
            }

            Wall target = new Wall(core, face);
            spawnedWalledPileOfRocks(random, target);
            return;
        }
    }

    /**
     * Force 3x3 size
     */
    private void spawnCenteredPileOfRocks(@NotNull Random random, Wall core) {
        core = core.getUp();
        Material[] ores = new Material[] {
                Material.IRON_ORE,
                Material.COAL_ORE,
                Material.GOLD_ORE,
                Material.LAPIS_ORE,
                Material.ANDESITE,
                Material.DIORITE,
                Material.GRANITE,
                Material.STONE,
                Material.ANDESITE,
                Material.DIORITE,
                Material.GRANITE,
                Material.STONE,
                Material.ANDESITE,
                Material.DIORITE,
                Material.GRANITE,
                Material.STONE
        };
        core.Pillar(random.nextInt(3) + 1, random, ores);

        for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
            core.getRelative(face).Pillar(random.nextInt(3), random, ores);
        }

        // Place stone cutter
        Wall target = core.getRelative(BlockUtils.getXZPlaneBlockFace(random));
        while (target.isSolid()) {
            target = target.getUp();
        }
        new DirectionalBuilder(Material.STONECUTTER).setFacing(BlockUtils.getDirectBlockFace(new Random()))
                                                    .apply(target);

        // Place one or two lamps next to the cutter
        for (int i = 0; i < random.nextInt(2) + 1; i++) {
            target = target.getAtY(core.getY() + 1).getRelative(BlockUtils.getDirectBlockFace(random));
            while (target.isSolid()) {
                target = target.getUp();
            }
            if (target.getDown().getType() != Material.LANTERN) {
                target.setType(Material.LANTERN);
            }
        }
    }

    /**
     * No such limitation
     */
    private void spawnedWalledPileOfRocks(@NotNull Random random, Wall core) {
        core = core.getUp();
        Material[] ores = new Material[] {
                Material.IRON_ORE,
                Material.COAL_ORE,
                Material.GOLD_ORE,
                Material.LAPIS_ORE,
                Material.ANDESITE,
                Material.DIORITE,
                Material.GRANITE,
                Material.STONE,
                Material.ANDESITE,
                Material.DIORITE,
                Material.GRANITE,
                Material.STONE,
                Material.ANDESITE,
                Material.DIORITE,
                Material.GRANITE,
                Material.STONE
        };

        // Place stone cutter
        new DirectionalBuilder(Material.STONECUTTER).setFacing(BlockUtils.getDirectBlockFace(new Random())).apply(core);
        // core.setType(Material.STONECUTTER);

        // Move against the wall
        core = core.getRelative(core.getDirection(), 2);

        // Highest ores are against the wall
        core.Pillar(random.nextInt(3) + 1, random, ores);
        core.getLeft().Pillar(random.nextInt(3) + 1, random, ores);
        core.getRight().Pillar(random.nextInt(3) + 1, random, ores);

        core.getLeft(2).Pillar(random.nextInt(3), random, ores);
        core.getRight(2).Pillar(random.nextInt(3), random, ores);

        core.getRear().Pillar(random.nextInt(3), random, ores);
        core.getRear().getLeft().Pillar(random.nextInt(3), random, ores);
        core.getRear().getRight().Pillar(random.nextInt(3), random, ores);


        // Place one or two lamps next to the cutter
        for (int i = 0; i < random.nextInt(2) + 1; i++) {
            Wall target = core.getRelative(core.getDirection().getOppositeFace(), 2)
                              .getRelative(BlockUtils.getDirectBlockFace(random));
            while (target.isSolid()) {
                target = target.getUp();
            }
            target.setType(Material.LANTERN);
        }
    }
}
