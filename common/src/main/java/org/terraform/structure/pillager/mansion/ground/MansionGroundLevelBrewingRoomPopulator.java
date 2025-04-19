package org.terraform.structure.pillager.mansion.ground;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs.Shape;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSchematicParser;
import org.terraform.structure.pillager.mansion.MansionRoomSize;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class MansionGroundLevelBrewingRoomPopulator extends MansionRoomPopulator {

    // Refers to the kitchen room width, not the width of one room cell.
    private static final int roomWidthX = 15;
    private static final int roomWidthZ = 6;
    public MansionGroundLevelBrewingRoomPopulator(CubeRoom room,
                                                  HashMap<BlockFace, MansionInternalWallState> internalWalls)
    {
        super(room, internalWalls);
    }

    @Override
    public void decorateRoom(@NotNull PopulatorDataAbstract data, @NotNull Random random) {

        int[] lowerBounds = this.getRoom().getLowerCorner(1);
        BlockFace randomFace = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH}[random.nextInt(2)];
        // TerraformGeneratorPlugin.logger.info("Library picking face: " + randomFace);
        try {
            if (randomFace == BlockFace.NORTH) {
                SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]);
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-brewingroom", target);
                schema.parser = new MansionRoomSchematicParser(random, data);
                schema.setFace(randomFace);
                schema.apply();
            }
            else if (randomFace == BlockFace.SOUTH) {
                SimpleBlock target = new SimpleBlock(
                        data,
                        lowerBounds[0] + roomWidthX,
                        this.getRoom().getY(),
                        lowerBounds[1] + roomWidthZ
                );
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-brewingroom", target);
                schema.parser = new MansionRoomSchematicParser(random, data);
                schema.setFace(randomFace);
                schema.apply();
            }
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }

    }

    @Override
    public void decorateWindow(@NotNull Random rand, @NotNull Wall w) {
        // table with potted mushrooms
        new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setHalf(Half.TOP)
                                                           .setFacing(w.getDirection().getOppositeFace())
                                                           .apply(w)
                                                           .setShape(Shape.OUTER_RIGHT)
                                                           .apply(w.getLeft())
                                                           .setShape(Shape.OUTER_LEFT)
                                                           .apply(w.getRight());
        if (rand.nextBoolean()) {
            w.getUp().setType(Material.POTTED_RED_MUSHROOM, Material.POTTED_BROWN_MUSHROOM);
        }
        if (rand.nextBoolean()) {
            w.getRight().getUp().setType(Material.POTTED_RED_MUSHROOM, Material.POTTED_BROWN_MUSHROOM);
        }
        if (rand.nextBoolean()) {
            w.getLeft().getUp().setType(Material.POTTED_RED_MUSHROOM, Material.POTTED_BROWN_MUSHROOM);
        }
    }

    // Brewing stand and mini library
    @Override
    public void decorateWall(Random rand, @NotNull Wall w) {
        w.setType(Material.CAULDRON);
        w.getLeft().setType(Material.BARREL);
        w.getLeft().lootTableChest(TerraLootTable.VILLAGE_TEMPLE);
        w.getRight().setType(Material.BARREL);
        w.getRight().lootTableChest(TerraLootTable.VILLAGE_TEMPLE);
        w.getLeft().getUp().setType(Material.BREWING_STAND);
        w.getRight().getUp().setType(Material.BREWING_STAND);

        w.getRear().Pillar(5, Material.BOOKSHELF);
        w.getLeft().getRear().Pillar(5, Material.BOOKSHELF);
        w.getRight().getRear().Pillar(5, Material.BOOKSHELF);
        w.getLeft(2).getRear().Pillar(7, Material.DARK_OAK_LOG);
        w.getRight(2).getRear().Pillar(7, Material.DARK_OAK_LOG);

        w.getLeft(2).getUp(4).setType(Material.POLISHED_ANDESITE_SLAB);
        w.getRight(2).getUp(4).setType(Material.POLISHED_ANDESITE_SLAB);

        w.getUp(5).setType(Material.POLISHED_ANDESITE_SLAB);
        new SlabBuilder(Material.POLISHED_ANDESITE_SLAB).setType(Type.TOP)
                                                        .apply(w.getUp(4).getLeft())
                                                        .apply(w.getUp(4).getRight());

        w.getUp(4).setType(Material.CHAIN);
        w.getUp(3).setType(Material.CHAIN);
        Lantern lat = (Lantern) Bukkit.createBlockData(Material.LANTERN);
        lat.setHanging(true);
        w.getUp(2).setBlockData(lat);

        new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setFacing(BlockUtils.getLeft(w.getDirection()))
                                                           .apply(w.getRight(3))
                                                           .apply(w.getRight(2).getUp())
                                                           .apply(w.getRight(2).getUp(3))
                                                           .apply(w.getLeft(2))
                                                           .apply(w.getLeft(2).getUp(2));

        new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setFacing(BlockUtils.getRight(w.getDirection()))
                                                           .apply(w.getLeft(3))
                                                           .apply(w.getLeft(2).getUp())
                                                           .apply(w.getLeft(2).getUp(3))
                                                           .apply(w.getRight(2))
                                                           .apply(w.getRight(2).getUp(2));

    }

    @Override
    public @NotNull MansionRoomSize getSize() {
        return new MansionRoomSize(2, 1);
    }

}
