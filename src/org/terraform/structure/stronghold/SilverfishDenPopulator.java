package org.terraform.structure.stronghold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class SilverfishDenPopulator extends RoomPopulatorAbstract{

	public SilverfishDenPopulator(Random rand, boolean forceSpawn, boolean unique) {
		super(rand, forceSpawn, unique);
	}

	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room) {
		//Spawn a random sphere of silverfish eggs
		SimpleBlock base = new SimpleBlock(data,room.getX(),room.getY()+room.getHeight()/2-2,room.getZ());
		BlockUtils.replaceSphere(rand.nextInt(9999), (room.getWidthX()-2)/2, (room.getHeight()-2)/2, (room.getWidthZ()-2)/2, base, false,Material.INFESTED_STONE,Material.INFESTED_STONE,Material.CAVE_AIR,Material.STONE);
		
		//Silverfish spawner in the middle
		data.setSpawner(room.getX(), room.getY()+1, room.getZ(), EntityType.SILVERFISH);
	}

	@Override
	public boolean canPopulate(CubeRoom room) {
		return !room.isBig();
	}
	
	

}
