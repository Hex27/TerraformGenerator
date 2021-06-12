package org.terraform.v1_14_R1;

import org.apache.commons.lang.StringUtils;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import org.terraform.coregen.BlockDataFixerAbstract;
import org.terraform.data.SimpleBlock;

public class BlockDataFixer extends BlockDataFixerAbstract {

	//Instead of being progressive, this shit will revert the changes in 1.16
    @Override
    public String updateSchematic(String schematic) {
        if (schematic.contains("_wall[")) {
            schematic = StringUtils.replace(schematic, "north=none", "north=false");
            schematic = StringUtils.replace(schematic, "south=none", "south=false");
            schematic = StringUtils.replace(schematic, "east=none", "east=false");
            schematic = StringUtils.replace(schematic, "west=none", "west=false");
            schematic = StringUtils.replace(schematic, "north=low", "north=true");
            schematic = StringUtils.replace(schematic, "south=low", "south=true");
            schematic = StringUtils.replace(schematic, "east=low", "east=true");
            schematic = StringUtils.replace(schematic, "west=low", "west=true");
            schematic = StringUtils.replace(schematic, "north=tall", "north=true");
            schematic = StringUtils.replace(schematic, "south=tall", "south=true");
            schematic = StringUtils.replace(schematic, "east=tall", "east=true");
            schematic = StringUtils.replace(schematic, "west=tall", "west=true");
        }
        
        schematic = StringUtils.replace(schematic, "minecraft:chain[axis=y", "iron_bars[north=false,south=false,east=false,west=false");
        
        if(schematic.contains(":lantern[")) {
            schematic = StringUtils.replace(schematic, "minecraft:lantern[hanging=false,waterlogged=false]", "minecraft:lantern[hanging=false]");
            schematic = StringUtils.replace(schematic, "minecraft:lantern[hanging=true,waterlogged=false]", "minecraft:lantern[hanging=true]");
            schematic = StringUtils.replace(schematic, "minecraft:lantern[waterlogged=false,hanging=true]", "minecraft:lantern[hanging=true]");
            schematic = StringUtils.replace(schematic, "minecraft:lantern[waterlogged=false,hanging=false]", "minecraft:lantern[hanging=false]");
        }
        return schematic;
    }

    @Override
    public void correctFacing(Vector v, SimpleBlock b, BlockData data, BlockFace face) {
    	
    }
}
