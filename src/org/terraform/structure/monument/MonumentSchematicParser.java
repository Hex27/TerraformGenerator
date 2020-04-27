package org.terraform.structure.monument;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.schematic.SchematicParser;

public class MonumentSchematicParser extends SchematicParser{
	
	private PopulatorDataAbstract pop;
	
	public MonumentSchematicParser(PopulatorDataAbstract pop) {
		super();
		this.pop = pop;
	}

	@Override
	public void applyData(Block block, BlockData data){
		if(data instanceof Waterlogged 
				&& block.getY()<=TerraformGenerator.seaLevel){
			((Waterlogged) data).setWaterlogged(true);
		}
		super.applyData(block, data);
	}
	
}