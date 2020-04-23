package org.terraform.coregen;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;

public abstract class PopulatorDataICAAbstract extends PopulatorDataAbstract {
	
	public abstract void registerGuardians(int x0, int y0, int z0, int x1, int y1, int z1);
	
}
