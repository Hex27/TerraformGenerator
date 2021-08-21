package org.terraform.schematic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.terraform.data.SimpleBlock;

public class SchematicParser {
	
	private boolean isDelayedApply = false;
	private HashMap<SimpleBlock, BlockData> delayed = new HashMap<>();
	private static ArrayList<Material> fragile = new ArrayList<>()
			{{
				add(Material.BROWN_CARPET);
			}};
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
