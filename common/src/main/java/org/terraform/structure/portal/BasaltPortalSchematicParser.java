package org.terraform.structure.portal;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class BasaltPortalSchematicParser extends SchematicParser {
    private final PopulatorDataAbstract pop;
    private final Random rand;
    private int placedChests = 0;

    public BasaltPortalSchematicParser(PopulatorDataAbstract pop, Random random) {
        this.pop = pop;
        this.rand = random;
    }

    private void placeBasalt(SimpleBlock base, int maxHeight) {
        base = base.getAtY(GenUtils.getHighestGround(pop, base.getX(), base.getZ()));
        int height = rand.nextInt(maxHeight + 1);

        for (int y = -height; y <= height; y++) {
            SimpleBlock block = base.getRelative(0, y, 0);
            if (block.getType() == Material.OBSIDIAN || block.getType() == Material.POLISHED_BLACKSTONE_BRICK_STAIRS)
                break;
            block.setType(Material.BASALT);
        }
    }

    @Override
    public void applyData(SimpleBlock block, BlockData data) {
        if (data.getMaterial() == Material.GREEN_CONCRETE) {
            SimpleBlock b = GenUtils.getHighestGround(pop, block);
            // Green concrete area should have 50% change of lava and 50% change of low basalt
            if (rand.nextBoolean()) {
                b.setType(Material.LAVA);
            } else {
                placeBasalt(b, GenUtils.randInt(rand, 1, 2));
                if (placedChests == 0) {
                    placedChests += 1;
                    SimpleBlock chest = GenUtils.getHighestGround(pop, block).getRelative(0, 1, 0);
                    chest.setType(Material.CHEST);
                    pop.lootTableChest(chest.getX(), chest.getY(), chest.getZ(), TerraLootTable.RUINED_PORTAL);
                }
            }
        } else if (data.getMaterial() == Material.YELLOW_CONCRETE
                || data.getMaterial() == Material.RED_CONCRETE) {
            // Occasional lava and basalt depending on the area.
            SimpleBlock b = GenUtils.getHighestGround(pop, block);

            if (rand.nextInt(5) == 0) {
                b.setType(Material.LAVA);
            } else {
                placeBasalt(b, GenUtils.randInt(rand, 1, data.getMaterial() == Material.YELLOW_CONCRETE ? 3 : 4));
            }
        } else if (data.getMaterial() == Material.MAGENTA_CONCRETE) {
            // Place low basalt on the edges and no lava.
            if (rand.nextBoolean())
                placeBasalt(GenUtils.getHighestGround(pop, block),
                        GenUtils.randInt(rand, 1, 2));
        } else {
            super.applyData(block, data);
        }
    }
}
