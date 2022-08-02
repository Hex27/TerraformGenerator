package org.terraform.structure.ancientcity;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.version.OneOneNineBlockHandler;
import org.terraform.utils.version.OneOneSevenBlockHandler;

public class AncientCityPillarSchematicParser extends SchematicParser {
	
	private int failCount = 0;
	private int totalCount = 0;
	ArrayList<SimpleBlock> touchedOffsets = new ArrayList<>();
	
	public ArrayList<SimpleBlock> getTouchedOffsets() {
		return touchedOffsets;
	}

	public float calculateFailRate() {
		return ((float) failCount)/((float) totalCount);
	}
	
    @Override
    public void applyData(SimpleBlock block, BlockData data) {
    	Random rand = new Random();
    	totalCount += 1;
    	if(block.isSolid() && block.getType() != OneOneNineBlockHandler.SCULK_VEIN) {
    		failCount += 1;
    		return;
    	}
    	else if(touchedOffsets.size() == 0 || touchedOffsets.get(0).getY() == block.getY())
    	{
    		touchedOffsets.add(block);
    	}
    	else if(touchedOffsets.get(0).getY() < block.getY()) {
    		touchedOffsets.clear();
    		touchedOffsets.add(block);
    	}
    	
    	if(data.getMaterial() == OneOneSevenBlockHandler.DEEPSLATE_TILES)
    	{ //Crack deepslate tiles
    		if(rand.nextBoolean())
    			data = Bukkit.createBlockData(OneOneSevenBlockHandler.CRACKED_DEEPSLATE_TILES);
    	}
		else if(data.getMaterial() == OneOneSevenBlockHandler.DEEPSLATE_BRICKS)
		{ //Crack deepslate bricks
    		if(rand.nextBoolean())
    			data = Bukkit.createBlockData(OneOneSevenBlockHandler.CRACKED_DEEPSLATE_BRICKS);
		}
    	super.applyData(block, data);
    }
}