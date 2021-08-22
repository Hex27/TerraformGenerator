package org.terraform.structure.pillager.mansion;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import java.util.Random;

public class MansionRoomSchematicParser extends SchematicParser {

    protected final Random rand;
    protected final PopulatorDataAbstract pop;

    public MansionRoomSchematicParser(Random rand,
                                    PopulatorDataAbstract pop) {
        this.rand = rand;
        this.pop = pop;
    }

    @Override
    public void applyData(SimpleBlock block, BlockData data) {
        if (data.getMaterial() == Material.CHEST||data.getMaterial() == Material.BARREL) {
            if (GenUtils.chance(rand, 2, 5)) {
                block.setType(Material.AIR);
                return; //2 fifths of chests are not placed.
            }
            super.applyData(block, data);
            pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.WOODLAND_MANSION);
        } else if (data.getMaterial() == Material.POTTED_POPPY) {
            block.setType(BlockUtils.pickPottedPlant());
            return;
        } else {
            super.applyData(block, data);
        }
    }

}