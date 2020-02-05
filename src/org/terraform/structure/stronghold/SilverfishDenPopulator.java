package org.terraform.structure.stronghold;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

public class SilverfishDenPopulator extends RoomPopulatorAbstract{

	public SilverfishDenPopulator(Random rand, boolean forceSpawn, boolean unique) {
		super(rand, forceSpawn, unique);
	}

	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room) {
		//Spawn a random sphere of silverfish eggs
		SimpleBlock base = new SimpleBlock(data,room.getX(),room.getY()+room.getHeight()/2-2,room.getZ());
		BlockUtils.replaceUpperSphere(rand.nextInt(9999), (room.getWidthX()-2)/2, (room.getHeight()-3), (room.getWidthZ()-2)/2, base, false,Material.INFESTED_STONE,Material.INFESTED_STONE,Material.CAVE_AIR,Material.STONE);
		
		//Silverfish spawner in the middle
		data.setSpawner(room.getX(), room.getY()+1, room.getZ(), EntityType.SILVERFISH);
	}

	@Override
	public boolean canPopulate(CubeRoom room) {
		return !room.isBig();
	}
	
	

}
