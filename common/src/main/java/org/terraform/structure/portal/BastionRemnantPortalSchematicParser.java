package org.terraform.structure.portal;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class BastionRemnantPortalSchematicParser extends SchematicParser {
    private final ArrayList<SimpleBlock> portalPositions = new ArrayList<>();
    private final PopulatorDataAbstract pop;
    private final Random rand;

    public BastionRemnantPortalSchematicParser(PopulatorDataAbstract pop, Random random) {
        this.pop = pop;
        this.rand = random;
    }

    @Override
    public void applyData(SimpleBlock block, BlockData data) {
        // Replace quarz stairs with blackstone ones but don't replace blocks (lset)
        if (data.getMaterial() == Material.QUARTZ_STAIRS) {
            Stairs stairs = (Stairs) Bukkit.createBlockData(Material.POLISHED_BLACKSTONE_BRICK_STAIRS);
            stairs.setFacing(((Stairs) data).getFacing());
            block.lsetBlockData(stairs);
        } else if (data.getMaterial() == Material.BLACK_CONCRETE) {
            portalPositions.add(block);
        } else if (data.getMaterial() == Material.CHEST) {
            if (rand.nextInt(6) == 0) {
                SimpleBlock chest = GenUtils.getHighestGround(pop, block).getRelative(0 , 1, 0);
                chest.setBlockData(data);
                pop.lootTableChest(chest.getX(), chest.getY(), chest.getZ(), TerraLootTable.RUINED_PORTAL);
            }
        } else {
            super.applyData(block, data);
        }
    }
}
