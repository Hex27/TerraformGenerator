package org.terraform.structure.pillager.mansion.secondfloor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSize;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class MansionSecondFloorStoreroomPopulator extends MansionRoomPopulator {

    // Refers to the kitchen room width, not the width of one room cell.
    private static final int roomWidthX = 15;
    private static final int roomWidthZ = 6;
    public MansionSecondFloorStoreroomPopulator(CubeRoom room,
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
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-storageroom", target);
                schema.parser = new MansionStoreroomSchematicParser(random);
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
                TerraSchematic schema = TerraSchematic.load("mansion/mansion-storageroom", target);
                schema.parser = new MansionStoreroomSchematicParser(random);
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
    public @NotNull MansionRoomSize getSize() {
        return new MansionRoomSize(2, 1);
    }

    // Do not extend MansionRoomSchematicParser, as we want the looting to
    // be done differently for this room specifically.
    private static class MansionStoreroomSchematicParser extends SchematicParser {
        private final Random rand;

        public MansionStoreroomSchematicParser(Random rand) {
            this.rand = rand;
        }

        @Override
        public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
            if (data.getMaterial() == Material.CHEST) {
                Material replacement = GenUtils.randChoice(rand,
                        Material.CHEST,
                        Material.CHEST,
                        Material.CHEST,
                        Material.CHEST,
                        Material.CHEST,
                        Material.BARREL,
                        Material.BARREL,
                        Material.BARREL,
                        Material.BARREL,
                        Material.BARREL,
                        Material.CRAFTING_TABLE,
                        Material.DARK_OAK_LOG,
                        Material.CAKE,
                        Material.LANTERN,
                        Material.COAL_BLOCK
                );
                data = Bukkit.createBlockData(replacement);
                BlockUtils.randRotateBlockData(rand, data);
            }

            super.applyData(block, data);
            if (data.getMaterial() == Material.CHEST || data.getMaterial() == Material.BARREL) {
                block.getPopData()
                     .lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.WOODLAND_MANSION);
            }
        }
    }

}
