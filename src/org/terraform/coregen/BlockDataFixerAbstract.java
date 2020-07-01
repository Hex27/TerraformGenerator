package org.terraform.coregen;

import java.util.ArrayList;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;

public abstract class BlockDataFixerAbstract {
	
	public boolean hasFlushed = false;
	
	private ArrayList<Vector> multifacing = new ArrayList<>();
	
	@SuppressWarnings("unchecked")
	public ArrayList<Vector> flush(){
		ArrayList<Vector> stuff =  (ArrayList<Vector>) multifacing.clone();
		multifacing.clear();
		hasFlushed = true;
		return stuff;
	}
	
	public void pushChanges(Vector e) {
		multifacing.add(e);
	}
	
	public abstract String updateSchematic(String schematic);

	public abstract void correctFacing(Vector v, SimpleBlock b, BlockData data, BlockFace face); 
}
