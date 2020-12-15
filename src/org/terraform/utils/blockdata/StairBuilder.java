package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;

public class StairBuilder {
	
	private Stairs blockData;
	
	public StairBuilder(Material mat) {
		this.blockData = (Stairs) Bukkit.createBlockData(mat);
	}

	public StairBuilder(Material... mat) {
		this.blockData = (Stairs) Bukkit.createBlockData(GenUtils.randMaterial(mat));
	}
	
	
	public StairBuilder setFacing(BlockFace face) {
		this.blockData.setFacing(face);
		return this;
	}
	
	public StairBuilder setHalf(Bisected.Half half) {
		this.blockData.setHalf(half);
		return this;
	}
	
	public StairBuilder setShape(Stairs.Shape shape) {
		this.blockData.setShape(shape);
		return this;
	}
	
	public StairBuilder setWaterlogged(boolean bool) {
		this.blockData.setWaterlogged(bool);
		return this;
	}
	
	public void apply(SimpleBlock block) {
		block.setBlockData(blockData);
	}
	
	public void apply(Wall block) {
		block.setBlockData(blockData);
	}
	
	public void apply(PopulatorDataAbstract data, int x, int y, int z) {
		data.setBlockData(x, y, z, blockData);
	}
	
	public Stairs get() {
		return blockData;
	}
}
