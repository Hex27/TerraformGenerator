package org.terraform.structure.ancientcity;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.version.V_1_19;

import java.util.ArrayList;
import java.util.Random;

public class AncientCityPillarSchematicParser extends SchematicParser {

    @NotNull
    final ArrayList<SimpleBlock> touchedOffsets = new ArrayList<>();
    private int failCount = 0;
    private int totalCount = 0;

    public @NotNull ArrayList<SimpleBlock> getTouchedOffsets() {
        return touchedOffsets;
    }

    public float calculateFailRate() {
        return ((float) failCount) / ((float) totalCount);
    }

    @Override
    public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
        Random rand = new Random();
        totalCount += 1;
        if (block.isSolid() && block.getType() != V_1_19.SCULK_VEIN) {
            failCount += 1;
            return;
        }
        else if (touchedOffsets.isEmpty() || touchedOffsets.get(0).getY() == block.getY()) {
            touchedOffsets.add(block);
        }
        else if (touchedOffsets.get(0).getY() < block.getY()) {
            touchedOffsets.clear();
            touchedOffsets.add(block);
        }

        if (data.getMaterial() == Material.DEEPSLATE_TILES) { // Crack deepslate tiles
            if (rand.nextBoolean()) {
                data = Bukkit.createBlockData(Material.CRACKED_DEEPSLATE_TILES);
            }
        }
        else if (data.getMaterial() == Material.DEEPSLATE_BRICKS) { // Crack deepslate bricks
            if (rand.nextBoolean()) {
                data = Bukkit.createBlockData(Material.CRACKED_DEEPSLATE_BRICKS);
            }
        }
        super.applyData(block, data);
    }
}