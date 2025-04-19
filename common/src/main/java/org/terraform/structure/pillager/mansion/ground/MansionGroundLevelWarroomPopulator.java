package org.terraform.structure.pillager.mansion.ground;

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
import org.terraform.utils.BannerUtils;
import org.terraform.utils.BlockUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class MansionGroundLevelWarroomPopulator extends MansionRoomPopulator {

    // Refers to the library room width, not the width of one room cell.
    private static final int roomWidth = 15;

    public MansionGroundLevelWarroomPopulator(CubeRoom room,
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
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-warroom", target);
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
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-warroom", target);
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
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-warroom", target);
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
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-warroom", target);
                schema.setFace(randomFace);
                schema.apply();
            }
        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }

    @Override
    public void decorateWall(Random rand, @NotNull Wall w) {
        BannerUtils.generatePillagerBanner(w.getLeft().getUp(3).get(), w.getDirection(), true);
        BannerUtils.generatePillagerBanner(w.getRight().getUp(3).get(), w.getDirection(), true);
    }


    @Override
    public @NotNull MansionRoomSize getSize() {
        return new MansionRoomSize(2, 2);
    }

}
