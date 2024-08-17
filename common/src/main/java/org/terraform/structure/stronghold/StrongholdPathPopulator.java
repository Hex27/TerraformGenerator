package org.terraform.structure.stronghold;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.ArrayList;
import java.util.Random;

public class StrongholdPathPopulator extends PathPopulatorAbstract {
    private final Random rand;

    public StrongholdPathPopulator(Random rand) {
        this.rand = rand;
    }

    @Override
    public void populate(@NotNull PathPopulatorData ppd) {

        //Find the ceiling for easier management later
        SimpleBlock ceil = ppd.base.getRelative(0, 1, 0);
        int depth = 0;
        while (!ceil.getType().isSolid()) {
            ceil = ceil.getRelative(0, 1, 0);
            depth++;
            if (depth > 10) return;
        }


        //This is a mazecell. Represents a crossroad or turn etc.
        if (ppd.dir == BlockFace.UP) {
            decorateCrossroads(ppd.base, Half.BOTTOM);

            boolean isCrossroad = true;

            //Check for any walls.
            ArrayList<Wall> walls = new ArrayList<>();
            for (BlockFace face : BlockUtils.directBlockFaces) {
                if (ppd.base.getRelative(face.getModX() * 2, 2, face.getModZ() * 2)
                        .getType().isSolid()) {
                    isCrossroad = false;
                    walls.add(new Wall(
                            ppd.base.getRelative(face.getModX() * 2, 1, face.getModZ() * 2),
                            face.getOppositeFace()));
                }
            }

            decorateCrossroads(ceil, Half.TOP);

            if (isCrossroad) { //This is a crossroad (Has 4 pathways)
                for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
                    new Wall(ppd.base.getRelative(0, 1, 0).getRelative(face))
                            .LPillar(10, rand, Material.COBBLESTONE_WALL, Material.ANDESITE_WALL, Material.STONE_BRICK_WALL);
                }
            } else //this is a turn
            {
                //Decorate the walls
                for (Wall wall : walls) {
                    wall.setType(Material.STONE, Material.SMOOTH_STONE, Material.ANDESITE);
                    wall.getRelative(0, 1, 0).setType(Material.CHISELED_STONE_BRICKS, Material.COBBLESTONE);
                    wall.getRelative(0, 2, 0).setType(Material.STONE, Material.SMOOTH_STONE, Material.ANDESITE);
                    wall.getRelative(0, 1, 0).getLeft().setType(Material.STONE, Material.SMOOTH_STONE, Material.ANDESITE);
                    wall.getRelative(0, 1, 0).getRight().setType(Material.STONE, Material.SMOOTH_STONE, Material.ANDESITE);

                    new StairBuilder(Material.ANDESITE_STAIRS, Material.POLISHED_ANDESITE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS)
                            .setFacing(BlockUtils.getLeft(wall.getDirection()))
                            .setHalf(Half.TOP)
                            .apply(wall.getRight());

                    new StairBuilder(Material.STONE_BRICK_STAIRS, Material.POLISHED_ANDESITE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS)
                            .setFacing(BlockUtils.getRight(wall.getDirection()))
                            .setHalf(Half.TOP)
                            .apply(wall.getLeft());

                    new StairBuilder(Material.STONE_BRICK_STAIRS, Material.POLISHED_ANDESITE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS)
                            .setFacing(BlockUtils.getLeft(wall.getDirection()))
                            .setHalf(Half.BOTTOM)
                            .apply(wall.getRight().getRelative(0, 2, 0));

                    new StairBuilder(Material.STONE_BRICK_STAIRS, Material.POLISHED_ANDESITE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS)
                            .setFacing(BlockUtils.getRight(wall.getDirection()))
                            .setHalf(Half.BOTTOM)
                            .apply(wall.getLeft().getRelative(0, 2, 0));
                }
            }
        } else //This is a pathway
        {
            //Check if the walls present really make it a pathway
            //3x4 empty air area. (widthxheight)
            if (!verifyPathway(new Wall(ppd.base, ppd.dir))) return;

            if (ppd.calcRemainder(2) == 0) { //arch
                decoratePathways(ppd.base, ppd.dir, Half.BOTTOM);
                Wall base = new Wall(ppd.base, ppd.dir);
                base.getRelative(0, 1, 0).getLeft(2).setType(Material.SMOOTH_STONE, Material.POLISHED_ANDESITE);
                base.getRelative(0, 1, 0).getRight(2).setType(Material.SMOOTH_STONE, Material.POLISHED_ANDESITE);
                base.getRelative(0, 2, 0).getLeft(2).setType(Material.CHISELED_STONE_BRICKS, Material.CHISELED_STONE_BRICKS, Material.COBBLESTONE);
                base.getRelative(0, 2, 0).getRight(2).setType(Material.CHISELED_STONE_BRICKS, Material.CHISELED_STONE_BRICKS, Material.COBBLESTONE);
                base.getRelative(0, 3, 0).getLeft(2).setType(Material.SMOOTH_STONE, Material.POLISHED_ANDESITE);
                base.getRelative(0, 4, 0).getRight(2).setType(Material.SMOOTH_STONE, Material.POLISHED_ANDESITE);

                new StairBuilder(Material.STONE_BRICK_STAIRS, Material.COBBLESTONE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS, Material.ANDESITE_STAIRS)
                        .setFacing(BlockUtils.getLeft(ppd.dir))
                        .setHalf(Half.TOP)
                        .apply(base.getRelative(0, 4, 0).getLeft());

                new StairBuilder(Material.STONE_BRICK_STAIRS, Material.COBBLESTONE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS, Material.ANDESITE_STAIRS)
                        .setFacing(BlockUtils.getRight(ppd.dir))
                        .setHalf(Half.TOP)
                        .apply(base.getRelative(0, 4, 0).getRight());

                ceil.setType(Material.CHISELED_STONE_BRICKS);
            } else //not-arch
            {
                Wall base = new Wall(ppd.base, ppd.dir);

                new SlabBuilder(BlockUtils.stoneBrickSlabs)
                        .setType(Slab.Type.TOP)
                        .apply(base.getRelative(0, 4, 0).getLeft());

                new SlabBuilder(BlockUtils.stoneBrickSlabs)
                        .setType(Slab.Type.TOP)
                        .apply(base.getRelative(0, 4, 0).getRight());

                //Loot chests
                if (GenUtils.chance(rand, 1, 50)) {
                    //Left or right
                    int i = GenUtils.randInt(rand, 0, 1);
                    Wall w = base.getRelative(0, 1, 0);
                    depth = 0;
                    while (!w.get().getType().isSolid() && depth < 10) {
                        if (i == 0) w = w.getLeft();
                        if (i == 1) w = w.getRight();
                        depth++;
                    }

                    if (i == 1) w = w.getLeft();
                    if (i == 0) w = w.getRight();

                    SimpleBlock cBlock = w.get();
                    cBlock.setType(Material.CHEST);
                    org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) Bukkit.createBlockData(Material.CHEST);
                    if (i == 0) chest.setFacing(BlockUtils.getAdjacentFaces(ppd.dir)[1]);
                    if (i == 1) chest.setFacing(BlockUtils.getAdjacentFaces(ppd.dir)[0]);
                    cBlock.setBlockData(chest);
                    cBlock.getPopData().lootTableChest(cBlock.getX(), cBlock.getY(), cBlock.getZ(), TerraLootTable.STRONGHOLD_CORRIDOR);
                } else if (GenUtils.chance(rand, 4, 25)) { //If a chest spawns, don't overlap it with iron bars
                    setIronBars(ppd);
                }
            }
        }

        //Refind ceiling. It could have been changed above.
        ceil = new Wall(ppd.base.getRelative(0, 1, 0), ppd.dir).findCeiling(10).get();

        //Sometimes parts of the ceiling falls down
        if (GenUtils.chance(rand, 3, 25)) {
            for (int i = 0; i < GenUtils.randInt(rand, 1, 5); i++) {
                dropDownBlock(ceil.getRelative(GenUtils.randInt(rand, -1, 1), 0, GenUtils.randInt(rand, -1, 1)));
            }
        }

        //Cobwebs
        if (GenUtils.chance(rand, 1, 25)) {
            SimpleBlock webBase = ceil.getRelative(0, -1, 0);
            webBase.setType(Material.COBWEB);

            for (int i = 0; i < GenUtils.randInt(rand, 0, 3); i++) {
                BlockFace face = CoralGenerator.getRandomBlockFace();
                if (face == BlockFace.UP) face = BlockFace.SELF;
                webBase.getRelative(face).setType(Material.COBWEB);
            }
        }

    }

    private boolean verifyPathway(@NotNull Wall base) {
        for (int h = 0; h <= 5; h++) {
            for (int width = -2; width <= 2; width++) {
                Wall rel = base.getRelative(0, h, 0);
                if (h == 0 || h == 5 || width == -2 || width == 2) {
                    if (!rel.getLeft(width).getType().isSolid())
                        return false;
                    if (!rel.getRight(width).getType().isSolid())
                        return false;
                } else {
                    if (rel.getLeft(width).getType().isSolid())
                        return false;
                    if (rel.getRight(width).getType().isSolid())
                        return false;
                }
            }
        }


        return true;
    }

    private void decorateCrossroads(@NotNull SimpleBlock core, Bisected.@NotNull Half isCeil) {
        //Decorate the floor and ceiling
        core.RSolSetType(Material.CHISELED_STONE_BRICKS);
        for (BlockFace face : BlockUtils.directBlockFaces) {

            core.getRelative(face).RSolSetBlockData(
                    new StairBuilder(
                            Material.STONE_BRICK_STAIRS, Material.COBBLESTONE_STAIRS,
                            Material.MOSSY_STONE_BRICK_STAIRS, Material.ANDESITE_STAIRS)
                            .setFacing(face)
                            .setHalf(isCeil)
                            .get()
            );
        }
    }

    private void decoratePathways(@NotNull SimpleBlock core, @NotNull BlockFace dir, Bisected.@NotNull Half isCeil) {
        //Decorate the floor and ceiling
        core.RSolSetType(Material.CHISELED_STONE_BRICKS);
        for (BlockFace face : BlockUtils.getAdjacentFaces(dir)) {

            core.getRelative(face).RSolSetBlockData(
                    new StairBuilder(Material.STONE_BRICK_STAIRS, Material.COBBLESTONE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS, Material.ANDESITE_STAIRS)
                            .setFacing(face)
                            .setHalf(isCeil)
                            .get()
            );
        }
    }

    private boolean setIronBars(@NotNull PathPopulatorData ppd) {
        Wall wall = new Wall(ppd.base, ppd.dir).getRelative(0, 4, 0);

        wall.setType(Material.IRON_BARS);
        wall.getLeft().setType(Material.IRON_BARS);
        wall.getRight().setType(Material.IRON_BARS);
        wall.getLeft().downRPillar(new Random(), 4, Material.IRON_BARS);
        wall.getRight().downRPillar(new Random(), 4, Material.IRON_BARS);

        //Fix iron bar placement.
        for (int h = 3; h >= 0; h--) {
            Wall temp = wall.getRelative(0, -h, 0);
            BlockUtils.correctSurroundingMultifacingData(temp.get());
            BlockUtils.correctSurroundingMultifacingData(temp.getLeft().get());
            BlockUtils.correctSurroundingMultifacingData(temp.getRight().get());
        }

        return true;
    }

    private void dropDownBlock(@NotNull SimpleBlock block) {
        if (block.getType().isSolid()) {
            BlockData type = block.getBlockData();
            block.setType(Material.CAVE_AIR);
            int depth = 0;
            while (!block.getType().isSolid()) {
                block = block.getRelative(0, -1, 0);
                depth++;
                if (depth > 50) return;
            }

            if (type instanceof Slab) {
                ((Slab) type).setType(Slab.Type.BOTTOM);
            } else if (type instanceof Stairs) {
                ((Stairs) type).setHalf(Half.BOTTOM);
            }

            if (GenUtils.chance(1, 3)) {
                block.getRelative(0, 1, 0).setBlockData(BlockUtils.infestStone(type));
            } else {
                block.getRelative(0, 1, 0).setBlockData(type);
            }
        }
    }
}
