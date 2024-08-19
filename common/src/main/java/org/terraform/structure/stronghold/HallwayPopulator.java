package org.terraform.structure.stronghold;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Map.Entry;
import java.util.Random;

public class HallwayPopulator extends RoomPopulatorAbstract {

    public HallwayPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {

        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 1).entrySet()) {
            Wall w = entry.getKey();
            boolean wasAir = false;
            for (int i = 0; i < entry.getValue(); i++) {
                new StairBuilder(
                        Material.STONE_BRICK_STAIRS,
                        Material.MOSSY_STONE_BRICK_STAIRS
                ).setFacing(w.getDirection().getOppositeFace()).setHalf(Bisected.Half.TOP).apply(w.getUp(4));
                w.getUp(5).LPillar(room.getHeight(), rand, BlockUtils.stoneBricks);
                if (!w.getRear().getUp().isSolid()) {
                    wasAir = true;
                    w.getUp(5)
                     .setType(Material.CHISELED_STONE_BRICKS, Material.CHISELED_STONE_BRICKS, Material.COBBLESTONE);
                }
                else {
                    // Right or left wall is part of an entrance
                    if (wasAir || !w.getLeft().getRear().getUp().isSolid()) {
                        w.getRear().Pillar(5, rand, Material.STONE, Material.SMOOTH_STONE);
                    }
                    wasAir = false;
                }

                w = w.getLeft();
            }
        }

        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 1).entrySet()) {
            Wall w = entry.getKey().getRelative(0, room.getHeight() - 1, 0);
            for (int i = 0; i < entry.getValue(); i++) {
                new StairBuilder(
                        Material.ANDESITE_STAIRS,
                        Material.STONE_BRICK_STAIRS,
                        Material.MOSSY_STONE_BRICK_STAIRS
                ).setFacing(w.getDirection().getOppositeFace()).setHalf(Bisected.Half.TOP).apply(w);

                w = w.getLeft();
            }
        }

        // Corner Walls
        for (int[] coords : room.getAllCorners(1)) {
            new Wall(new SimpleBlock(data, coords[0], room.getY() + 1, coords[1])).Pillar(
                    room.getHeight() - 1,
                    rand,
                    BlockUtils.stoneBricks
            );
        }

        for (int i = 0; i < GenUtils.randInt(rand, room.getWidthX(), room.getWidthX() * room.getWidthZ() / 10); i++) {
            int[] randomCoords = room.randomCoords(rand, 1);
            SimpleBlock ceil = new SimpleBlock(data, randomCoords[0], room.getY() + room.getHeight(), randomCoords[2]);
            // Sometimes parts of the ceiling falls down
            if (GenUtils.chance(rand, 4, 25)) {
                for (int j = 0; j < GenUtils.randInt(rand, 1, 5); j++) {
                    dropDownBlock(ceil.getRelative(GenUtils.randInt(rand, -1, 1), 0, GenUtils.randInt(rand, -1, 1)));
                }
            }

            // Cobwebs
            if (GenUtils.chance(rand, 1, 5)) {
                SimpleBlock webBase = ceil.getDown();
                webBase.setType(Material.COBWEB);

                for (int j = 0; j < GenUtils.randInt(rand, 0, 3); j++) {
                    BlockFace face = CoralGenerator.getRandomBlockFace();
                    if (face == BlockFace.UP) {
                        face = BlockFace.SELF;
                    }
                    if (!webBase.getRelative(face).isSolid()) {
                        webBase.getRelative(face).setType(Material.COBWEB);
                    }
                }
            }
        }

    }

    private void dropDownBlock(@NotNull SimpleBlock block) {
        if (block.isSolid()) {
            BlockData type = block.getBlockData();
            block.setType(Material.CAVE_AIR);
            int depth = 0;
            while (!block.isSolid()) {
                block = block.getDown();
                depth++;
                if (depth > 50) {
                    return;
                }
            }

            if (type instanceof Slab) {
                ((Slab) type).setType(Slab.Type.BOTTOM);
            }
            else if (type instanceof Stairs) {
                ((Stairs) type).setHalf(Half.BOTTOM);
            }

            if (GenUtils.chance(1, 3)) {
                block.getUp().setBlockData(BlockUtils.infestStone(type));
            }
            else {
                block.getUp().setBlockData(type);
            }
        }
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        // Don't override prisons: Hallways are bloody dull.
        return !new PrisonRoomPopulator(new Random(), false, false).canPopulate(room) && room.isBig() && !room.isHuge();
    }


}
