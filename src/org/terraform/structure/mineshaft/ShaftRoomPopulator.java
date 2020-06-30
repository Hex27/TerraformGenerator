package org.terraform.structure.mineshaft;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

public class ShaftRoomPopulator extends RoomPopulatorAbstract{

	public ShaftRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
		super(rand, forceSpawn, unique);
	}

	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room) {
		//Carve upward shaft. HARD REPLACE.
		for(int i = 1; i < 20; i++){
			BlockUtils.carveCaveAir(new Random().nextInt(777123), 
					room.getWidthX()/2, 
					5, 
					room.getWidthZ()/2, 
					new SimpleBlock(data,room.getX(),room.getY()+i,room.getZ())
					, new ArrayList<Material>(){{add(Material.BARRIER);}});
		}
		
		
	}
	@Override
	public boolean canPopulate(CubeRoom room) {
		return false;
	}
	
	

}
