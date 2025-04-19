package org.terraform.structure.pillager.mansion.secondfloor;

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
import org.terraform.utils.blockdata.StairBuilder;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class MansionSecondFloorPianoRoomPopulator extends MansionRoomPopulator {

    // Refers to the kitchen room width, not the width of one room cell.
    private static final int roomWidthX = 6;
    private static final int roomWidthZ = 15;
    public MansionSecondFloorPianoRoomPopulator(CubeRoom room,
                                                HashMap<BlockFace, MansionInternalWallState> internalWalls)
    {
        super(room, internalWalls);
    }

    @Override
    public void decorateRoom(@NotNull PopulatorDataAbstract data, @NotNull Random random) {
        int[] lowerBounds = this.getRoom().getLowerCorner(1);
        BlockFace randomFace = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH}[random.nextInt(2)];
        TerraformGeneratorPlugin.logger.info("Piano at "
                                             + this.getRoom().getSimpleLocation()
                                             + " picking face: "
                                             + randomFace);
        try {
            if (randomFace == BlockFace.NORTH) {
                SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]);
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-piano", target);
                schema.setFace(randomFace);
                schema.parser = new MansionRoomSchematicParser(random, data);
                schema.apply();
            }
            else if (randomFace == BlockFace.SOUTH) {
                SimpleBlock target = new SimpleBlock(
                        data,
                        lowerBounds[0] + roomWidthX,
                        this.getRoom().getY(),
                        lowerBounds[1] + roomWidthZ
                );
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-piano", target);
                schema.setFace(randomFace);
                schema.parser = new MansionRoomSchematicParser(random, data);
                schema.apply();
            }
        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }

    @Override
    public void decorateWindow(Random rand, @NotNull Wall w) {
        w.setType(Material.DARK_OAK_LOG);
        BlockUtils.pickPottedPlant().build(w.getUp());
        new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setFacing(BlockUtils.getLeft(w.getDirection()))
                                                           .apply(w.getLeft())
                                                           .setFacing(BlockUtils.getRight(w.getDirection()))
                                                           .apply(w.getRight());
    }

    @Override
    public void decorateWall(@NotNull Random rand, @NotNull Wall w) {
        PaintingUtils.placePainting(w.getUp(2).getLeft().get(),
                w.getDirection(),
                PaintingUtils.getArtFromDimensions(rand, 1, 2)
        );
        PaintingUtils.placePainting(w.getUp(2).getRight().get(),
                w.getDirection(),
                PaintingUtils.getArtFromDimensions(rand, 1, 2)
        );
        w.getRear().Pillar(6, Material.DARK_OAK_LOG);
        w.getLeft(2).getRear().Pillar(6, Material.DARK_OAK_LOG);
        w.getRight(2).getRear().Pillar(6, Material.DARK_OAK_LOG);
    }

    @Override
    public @NotNull MansionRoomSize getSize() {
        return new MansionRoomSize(1, 2);
    }
}
