package org.terraform.structure.monument;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;

public class MonumentSchematicParser extends SchematicParser {
    @Override
    public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
        if (data instanceof Waterlogged && block.getY() <= TerraformGenerator.seaLevel) {
            ((Waterlogged) data).setWaterlogged(true);
        }
        super.applyData(block, data);
    }
}