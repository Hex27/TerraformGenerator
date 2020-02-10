package org.terraform.schematic;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class SchematicParser {
	
	public SchematicParser(){}
	
	public void applyData(Block block, BlockData data){
		block.setBlockData(data,true);
	}
	
}
