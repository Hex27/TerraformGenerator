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
import org.terraform.utils.BannerUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public class MansionGroundLevelMushroomFarmPopulator extends MansionRoomPopulator {

    // Refers to the kitchen room width, not the width of one room cell.
    private static final int roomWidthX = 6;
    private static final int roomWidthZ = 15;
    public MansionGroundLevelMushroomFarmPopulator(CubeRoom room,
                                                   HashMap<BlockFace, MansionInternalWallState> internalWalls)
    {
        super(room, internalWalls);
    }

    @Override
    public void decorateRoom(@NotNull PopulatorDataAbstract data, @NotNull Random random) {
        int[] lowerBounds = this.getRoom().getLowerCorner(1);
        BlockFace randomFace = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH}[random.nextInt(2)];
        // TerraformGeneratorPlugin.logger.info("Mushroom Farm at " + this.getRoom().getSimpleLocation() + " picking face: " + randomFace);
        try {
            if (randomFace == BlockFace.NORTH) {
                SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]);
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-mushroomfarm", target);
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
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-mushroomfarm", target);
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
        Entry<Wall, Integer> entry = this.getRoom()
                                         .getWall(w.get().getPopData(), w.getDirection().getOppositeFace(), 0);
        w = entry.getKey();
        for (int i = 0; i < entry.getValue(); i++) {
            if (w.getUp(4).getRear().getType() == Material.LIGHT_GRAY_STAINED_GLASS_PANE) {
                BannerUtils.generateBanner(w.getUp(4).get(), w.getDirection(), Material.BLACK_WALL_BANNER, null);
                BannerUtils.generateBanner(w.getUp(3).get(), w.getDirection(), Material.BLACK_WALL_BANNER, null);
                BannerUtils.generateBanner(w.getUp(2).get(), w.getDirection(), Material.BLACK_WALL_BANNER, null);
            }
            w = w.getLeft();
        }
    }

    @Override
    public void decorateWall(Random rand, @NotNull Wall w) {
        w.getLeft().setType(Material.COMPOSTER);
        w.getRight().setType(Material.COMPOSTER);
    }


    @Override
    public @NotNull MansionRoomSize getSize() {
        return new MansionRoomSize(1, 2);
    }

}
