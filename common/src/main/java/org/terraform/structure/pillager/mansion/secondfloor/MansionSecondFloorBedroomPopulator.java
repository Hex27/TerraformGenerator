package org.terraform.structure.pillager.mansion.secondfloor;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
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
import org.terraform.utils.BannerUtils;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.PaintingUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class MansionSecondFloorBedroomPopulator extends MansionRoomPopulator {

    // Refers to the bedroom room width, not the width of one room cell.
    private static final int roomWidth = 15;

    public MansionSecondFloorBedroomPopulator(CubeRoom room,
                                              HashMap<BlockFace, MansionInternalWallState> internalWalls)
    {
        super(room, internalWalls);
    }

    @Override
    public void decorateRoom(@NotNull PopulatorDataAbstract data, @NotNull Random random) {
        int[] lowerBounds = this.getRoom().getLowerCorner(1);
        BlockFace randomFace = BlockUtils.getDirectBlockFace(random);
        // TerraformGeneratorPlugin.logger.info("Library picking face: " + randomFace);
        try {
            if (randomFace == BlockFace.NORTH) {
                SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]);
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-bedroom", target);
                schema.parser = new MansionRoomSchematicParser(random, data);
                schema.setFace(randomFace);
                schema.apply();
            }
            else if (randomFace == BlockFace.SOUTH) {
                SimpleBlock target = new SimpleBlock(
                        data,
                        lowerBounds[0] + roomWidth,
                        this.getRoom().getY(),
                        lowerBounds[1] + roomWidth
                );
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-bedroom", target);
                schema.parser = new MansionRoomSchematicParser(random, data);
                schema.setFace(randomFace);
                schema.apply();
            }
            else if (randomFace == BlockFace.EAST) {
                SimpleBlock target = new SimpleBlock(
                        data,
                        lowerBounds[0] + roomWidth,
                        this.getRoom().getY(),
                        lowerBounds[1]
                );
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-bedroom", target);
                schema.parser = new MansionRoomSchematicParser(random, data);
                schema.setFace(randomFace);
                schema.apply();
            }
            else if (randomFace == BlockFace.WEST) {
                SimpleBlock target = new SimpleBlock(
                        data,
                        lowerBounds[0],
                        this.getRoom().getY(),
                        lowerBounds[1] + roomWidth
                );
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-bedroom", target);
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
    public void decorateEntrance(Random rand, Wall w) {
        w = w.getRear();
        w.getLeft(2).Pillar(5, Material.DARK_OAK_PLANKS);
        w.getRight(2).Pillar(5, Material.DARK_OAK_PLANKS);
        w.getLeft(2).Pillar(3, Material.DARK_OAK_LOG);
        w.getRight(2).Pillar(3, Material.DARK_OAK_LOG);

        w.getLeft().getUp(5).downPillar(2, Material.DARK_OAK_PLANKS);
        w.getRight().getUp(5).downPillar(2, Material.DARK_OAK_PLANKS);
        w.getUp(5).downPillar(2, Material.DARK_OAK_PLANKS);
        new StairBuilder(Material.DARK_OAK_STAIRS).setFacing(w.getDirection())
                                                  .setHalf(Half.TOP)
                                                  .apply(w.getUp(4))
                                                  .setFacing(BlockUtils.getLeft(w.getDirection()))
                                                  .apply(w.getUp(3).getLeft())
                                                  .setFacing(BlockUtils.getRight(w.getDirection()))
                                                  .apply(w.getUp(3).getRight());
    }

    @Override
    public void decorateWall(@NotNull Random rand, @NotNull Wall w) {
        int choice = rand.nextInt(2);
        // Table
        if (choice == 0) { // Andesite table with banner and lectern
            w.getLeft(3).Pillar(6, Material.DARK_OAK_LOG);
            w.getRight(3).Pillar(6, Material.DARK_OAK_LOG);
            new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setHalf(Half.TOP)
                                                               .setFacing(BlockUtils.getLeft(w.getDirection()))
                                                               .apply(w.getLeft(2))
                                                               .apply(w.getLeft(2).getUp(4))
                                                               .apply(w.getLeft(2).getUp(5))
                                                               .apply(w.getLeft().getUp(5))
                                                               .setFacing(BlockUtils.getRight(w.getDirection()))
                                                               .apply(w.getRight(2))
                                                               .apply(w.getRight(2).getUp(4))
                                                               .apply(w.getRight(2).getUp(5))
                                                               .apply(w.getRight().getUp(5));

            w.getUp(5).setType(Material.POLISHED_ANDESITE);
            BannerUtils.generatePillagerBanner(w.getUp(3).get(), w.getDirection(), true);

            new DirectionalBuilder(Material.LECTERN).setFacing(w.getDirection()).apply(w);

            new SlabBuilder(Material.POLISHED_ANDESITE_SLAB).setType(Type.TOP).apply(w.getLeft()).apply(w.getRight());
        }
        else {
            table(rand, w.getLeft(2));
            table(rand, w.getRight(2));
        }
    }

    @Override
    public void decorateWindow(@NotNull Random rand, @NotNull Wall w) {
        int choice = rand.nextInt(2);
        // Utility Block
        if (choice == 0) { // Table with flowers
            new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setHalf(Half.TOP)
                                                               .setFacing(BlockUtils.getLeft(w.getDirection()))
                                                               .apply(w.getLeft(2))
                                                               .setFacing(BlockUtils.getRight(w.getDirection()))
                                                               .apply(w.getRight(2));

            new SlabBuilder(Material.POLISHED_ANDESITE_SLAB).setType(Type.TOP)
                                                            .apply(w)
                                                            .apply(w.getLeft())
                                                            .apply(w.getRight());

            BlockUtils.pickPottedPlant().build(w.getUp());
            BlockUtils.pickPottedPlant().build(w.getUp().getLeft());
            BlockUtils.pickPottedPlant().build(w.getUp().getRight());
        }
        else {
            DecorationsBuilder.build(w,
                    DecorationsBuilder.CRAFTING_TABLE,
                    DecorationsBuilder.FLETCHING_TABLE,
                    DecorationsBuilder.CARTOGRAPHY_TABLE,
                    DecorationsBuilder.ENCHANTING_TABLE,
                    DecorationsBuilder.BREWING_STAND,
                    DecorationsBuilder.ANVIL,
                    DecorationsBuilder.NOTE_BLOCK,
                    DecorationsBuilder.JUKEBOX
            );
        }
    }

    private void table(@NotNull Random rand, @NotNull Wall w) {
        w.getLeft().getRear().Pillar(6, Material.DARK_OAK_LOG);
        w.getRight().getRear().Pillar(6, Material.DARK_OAK_LOG);

        w.getLeft().setType(Material.STRIPPED_DARK_OAK_LOG);
        w.getRight().setType(Material.STRIPPED_DARK_OAK_LOG);
        new SlabBuilder(Material.DARK_OAK_SLAB).setType(Type.TOP).apply(w);

        w.getUp().setType(Material.BROWN_CARPET);
        w.getLeft().getUp().setType(Material.BROWN_CARPET);
        w.getRight().getUp().setType(Material.BROWN_CARPET);

        PaintingUtils.placePainting(w.getUp(2).get(), w.getDirection(), PaintingUtils.getArtFromDimensions(rand, 1, 2));
    }


    @Override
    public @NotNull MansionRoomSize getSize() {
        return new MansionRoomSize(2, 2);
    }
}
