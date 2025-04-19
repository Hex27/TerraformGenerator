package org.terraform.structure.pillager.mansion.ground;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
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
import org.terraform.utils.PaintingUtils;
import org.terraform.utils.blockdata.OrientableBuilder;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class MansionGroundLevelDiningRoomPopulator extends MansionRoomPopulator {

    // Refers to the kitchen room width, not the width of one room cell.
    private static final int roomWidthX = 15;
    private static final int roomWidthZ = 6;
    public MansionGroundLevelDiningRoomPopulator(CubeRoom room,
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
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-diningroom", target);
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
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-diningroom", target);
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
    public void decorateExit(Random rand, @NotNull Wall w) {
        OrientableBuilder builder = new OrientableBuilder(Material.DARK_OAK_LOG);
        builder.setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getLeft(w.getDirection())));
        for (int i = 0; i <= 4; i++) {
            builder.lapply(w.getUp(6).getLeft(i));
            builder.lapply(w.getUp(6).getRight(i));
        }
    }

    @Override
    public void decorateWindow(@NotNull Random rand, @NotNull Wall w) {
        decorateExit(rand, w); // same code to join the top to the ceiling decor

        // Pillars to connect ceiling decor to ground (less square)
        w = w.getUp(6).getRight(4);
        for (int i = 0; i <= 8; i++) {
            if (w.getFront().getType() == Material.POLISHED_ANDESITE_STAIRS) {
                w.downPillar(rand, 7, Material.DARK_OAK_LOG);
                w.getRear().downPillar(rand, 7, Material.DARK_OAK_PLANKS);
            }
            w = w.getLeft();
        }
    }

    // Decorate with paintings and wall texturing
    @Override
    public void decorateWall(@NotNull Random rand, @NotNull Wall w) {


        PaintingUtils.placePainting(w.getUp(2).get(), w.getDirection(), PaintingUtils.getArtFromDimensions(rand, 4, 4));


        w = w.getUp(6).getRight(4);
        for (int i = 0; i <= 8; i++) {
            if (w.getType() == Material.POLISHED_ANDESITE_STAIRS) {
                w.getRear().downPillar(rand, 7, Material.DARK_OAK_LOG);
            }
            w = w.getLeft();
        }
    }

    @Override
    public @NotNull MansionRoomSize getSize() {
        return new MansionRoomSize(2, 1);
    }
}
