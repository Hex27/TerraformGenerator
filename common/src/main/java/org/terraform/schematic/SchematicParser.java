package org.terraform.schematic;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.terraform.data.SimpleBlock;

public class SchematicParser {
	
	private boolean isDelayedApply = false;
	private final HashMap<SimpleBlock, BlockData> delayed = new HashMap<>();
	private static final EnumSet<Material> fragile = EnumSet.of(
				Material.BROWN_MUSHROOM,
				Material.RED_MUSHROOM,
				Material.BROWN_CARPET,
				Material.RED_CARPET,
				Material.WHITE_CARPET,
				Material.SOUL_FIRE,
				Material.REDSTONE_WIRE,
				Material.REDSTONE_TORCH,
				Material.REPEATER,
                Material.RAIL,
				Material.LEVER,
				Material.POTATOES,
				Material.KELP
			);
	
    public void applyData(SimpleBlock block, BlockData data) {
    	if(isDelayedApply || !fragile.contains(data.getMaterial()))
	    	block.setBlockData(data);
    	else
    	{
    		delayed.put(block, data);
    	}
    }
    
    public void applyDelayedData() {
    	isDelayedApply = true;
    	for(Entry<SimpleBlock, BlockData> entry:delayed.entrySet()) {
    		applyData(entry.getKey(), entry.getValue());
    	}
    }
}
