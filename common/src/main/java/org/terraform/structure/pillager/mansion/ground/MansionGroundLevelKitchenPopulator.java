package org.terraform.structure.pillager.mansion.ground;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.small_items.DecorationsBuilder;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSchematicParser;
import org.terraform.structure.pillager.mansion.MansionRoomSize;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class MansionGroundLevelKitchenPopulator extends MansionRoomPopulator {

    // Refers to the kitchen room width, not the width of one room cell.
    private static final int roomWidthX = 6;
    private static final int roomWidthZ = 15;
    public MansionGroundLevelKitchenPopulator(CubeRoom room,
                                              HashMap<BlockFace, MansionInternalWallState> internalWalls)
    {
        super(room, internalWalls);
    }

    @Override
    public void decorateRoom(@NotNull PopulatorDataAbstract data, @NotNull Random random) {
        int[] lowerBounds = this.getRoom().getLowerCorner(1);
        BlockFace randomFace = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH}[random.nextInt(2)];
        TerraformGeneratorPlugin.logger.info("Kitchen at "
                                             + this.getRoom().getSimpleLocation()
                                             + " picking face: "
                                             + randomFace);
        try {
            if (randomFace == BlockFace.NORTH) {
                SimpleBlock target = new SimpleBlock(
                        data,
                        lowerBounds[0],
                        this.getRoom().getY(),
                        lowerBounds[1] + roomWidthZ
                );
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-kitchen", target);
                schema.setFace(randomFace);
                schema.parser = new MansionKitchenSchematicParser(random, data);
                schema.apply();
            }
            else if (randomFace == BlockFace.SOUTH) {
                SimpleBlock target = new SimpleBlock(
                        data,
                        lowerBounds[0] + roomWidthX,
                        this.getRoom().getY(),
                        lowerBounds[1]
                );
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-kitchen", target);
                schema.setFace(randomFace);
                schema.parser = new MansionKitchenSchematicParser(random, data);
                schema.apply();
            }
        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }

    @Override
    public void decorateExit(Random rand, @NotNull Wall w) {
        w.getUp(6).setType(Material.DARK_OAK_PLANKS);
    }

    @Override
    public void decorateWindow(@NotNull Random rand, @NotNull Wall w) {
        w.getUp(6).setType(Material.DARK_OAK_PLANKS);
        int choice = rand.nextInt(3);
        switch (choice) {
            case 0: // Smokers & pressure plates
                new DirectionalBuilder(Material.SMOKER).setFacing(w.getDirection())
                                                       .apply(w)
                                                       .apply(w.getLeft())
                                                       .apply(w.getRight())
                                                       .apply(w.getLeft(2))
                                                       .apply(w.getRight(2));

                w.getUp().setType(Material.DARK_OAK_PRESSURE_PLATE);
                w.getUp().getLeft().setType(Material.DARK_OAK_PRESSURE_PLATE);
                w.getUp().getLeft(2).setType(Material.DARK_OAK_PRESSURE_PLATE);
                w.getUp().getRight().setType(Material.DARK_OAK_PRESSURE_PLATE);
                w.getUp().getRight(2).setType(Material.DARK_OAK_PRESSURE_PLATE);
                break;
            case 1: // Barrels
                Wall target = w.getRight(2);
                for (int i = 0; i < 5; i++) {
                    if (GenUtils.chance(rand, 1, 3)) {
                        target.setBlockData(BlockUtils.getRandomBarrel());
                        target.get()
                              .getPopData()
                              .lootTableChest(target.getX(),
                                      target.getY(),
                                      target.getZ(),
                                      TerraLootTable.VILLAGE_BUTCHER
                              );
                    }
                    target = target.getLeft();
                }
                break;
            default: // Table
                new SlabBuilder(Material.DARK_OAK_SLAB).setType(Type.TOP)
                                                       .apply(w)
                                                       .apply(w.getLeft())
                                                       .apply(w.getRight());
                new StairBuilder(Material.DARK_OAK_STAIRS).setHalf(Half.TOP)
                                                          .setFacing(BlockUtils.getLeft(w.getDirection()))
                                                          .apply(w.getLeft(2))
                                                          .setFacing(BlockUtils.getRight(w.getDirection()))
                                                          .apply(w.getRight(2));

                if (rand.nextBoolean()) {
                    DecorationsBuilder.CRAFTING_TABLE.build(w.getLeft());
                }

                if (rand.nextBoolean()) {
                    DecorationsBuilder.build(
                            w.getRight(),
                            DecorationsBuilder.MELON,
                            DecorationsBuilder.PUMPKIN,
                            DecorationsBuilder.CAKE
                    );
                }

                if (rand.nextBoolean()) {
                    DecorationsBuilder.OAK_PRESSURE_PLATE.build(w.getUp());
                }
                break;
        }

    }

    // Decorate with paintings and wall texturing
    @Override
    public void decorateWall(@NotNull Random rand, @NotNull Wall w) {
        // Shelves
        w.getRear().getLeft().Pillar(7, Material.DARK_OAK_LOG);
        if (rand.nextBoolean()) {
            new DirectionalBuilder(Material.FURNACE).setFacing(w.getDirection()).apply(w.getLeft());
            w.getLeft().getUp().Pillar(6, Material.COBBLESTONE_WALL);
            w.getLeft().getUp().CorrectMultipleFacing(6);
        }
        w.getRear().getRight().Pillar(7, Material.DARK_OAK_LOG);
        if (rand.nextBoolean()) {
            new DirectionalBuilder(Material.FURNACE).setFacing(w.getDirection()).apply(w.getRight());
            w.getRight().getUp().Pillar(6, Material.COBBLESTONE_WALL);
            w.getRight().getUp().CorrectMultipleFacing(6);
        }
        shelfify(rand, w.getRear());
        shelfify(rand, w.getLeft(2).getRear());
        shelfify(rand, w.getRight(2).getRear());
    }

    private void shelfify(@NotNull Random rand, @NotNull Wall w) {
        new SlabBuilder(Material.POLISHED_ANDESITE_SLAB).setType(Type.TOP).apply(w.getUp()).apply(w.getUp(3));
        w.setType(Material.AIR, Material.AIR, Material.CRAFTING_TABLE, Material.MELON, Material.PUMPKIN);

        w.getUp(2)
         .setType(Material.POTTED_RED_MUSHROOM,
                 Material.POTTED_BROWN_MUSHROOM,
                 Material.CAKE,
                 Material.TURTLE_EGG,
                 Material.AIR,
                 Material.AIR
         );

        w.getUp(4)
         .setType(Material.POTTED_RED_MUSHROOM,
                 Material.POTTED_BROWN_MUSHROOM,
                 Material.CAKE,
                 Material.TURTLE_EGG,
                 Material.AIR,
                 Material.AIR
         );

        // Barrel loot
        if (GenUtils.chance(rand, 1, 5)) {
            Wall target = w.getUp(2);
            target.setBlockData(BlockUtils.getRandomBarrel());
            target.get()
                  .getPopData()
                  .lootTableChest(target.getX(), target.getY(), target.getZ(), TerraLootTable.VILLAGE_BUTCHER);
        }
        if (GenUtils.chance(rand, 1, 5)) {
            Wall target = w.getUp(4);
            target.setBlockData(BlockUtils.getRandomBarrel());
            target.get()
                  .getPopData()
                  .lootTableChest(target.getX(), target.getY(), target.getZ(), TerraLootTable.VILLAGE_PLAINS_HOUSE);
        }

    }

    @Override
    public @NotNull MansionRoomSize getSize() {
        return new MansionRoomSize(1, 2);
    }

    private static class MansionKitchenSchematicParser extends MansionRoomSchematicParser {
        public MansionKitchenSchematicParser(Random rand, PopulatorDataAbstract pop) {
            super(rand, pop);
        }

        @Override
        public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
            if (data.getMaterial() == Material.MELON) {
                block.setType(Material.MELON, Material.PUMPKIN, Material.HAY_BLOCK, Material.DRIED_KELP_BLOCK);
            }
            else {
                super.applyData(block, data);
            }
        }
    }
}
