package org.terraform.coregen.populatordata;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public interface IPopulatorDataPhysicsCapable {

	public void setType(int x, int y, int z, Material type, boolean updatePhysics);
	public void setBlockData(int x, int y, int z, BlockData type, boolean updatePhysics);
}
