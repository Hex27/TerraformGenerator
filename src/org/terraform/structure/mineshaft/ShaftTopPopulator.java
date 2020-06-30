package org.terraform.structure.mineshaft;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

public class ShaftTopPopulator extends RoomPopulatorAbstract{

	public ShaftTopPopulator(Random rand, boolean forceSpawn, boolean unique) {
		super(rand, forceSpawn, unique);
	}

	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room) {
		
	}

	@Override
	public boolean canPopulate(CubeRoom room) {
		return false;
	}
	
	

}
