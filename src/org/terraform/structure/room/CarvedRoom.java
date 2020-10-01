package org.terraform.structure.room;

import java.util.Random;

import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.BlockUtils;

public class CarvedRoom extends CubeRoom{

	public CarvedRoom(int widthX, int widthZ, int height, int x, int y, int z) {
		super(widthX, widthZ, height, x, y, z);
	}
	
	public CarvedRoom(CubeRoom room) {
		super(room.getWidthX(), room.getWidthZ(), room.getHeight(), room.getX(), room.getY(), room.getZ());
	}
	
	/**
	 * Fillmat is always CAVE_AIR no matter what's being put lol.
	 */
	@Override
	public void fillRoom(PopulatorDataAbstract data, Material[] mat, Material fillMat){
		int heightOffset = height-(2*height/3);
		BlockUtils.carveCaveAir(new Random().nextInt(9999291), 
				widthX/2, 2*height/3, widthZ/2, 
				new SimpleBlock(data,x,y+heightOffset,z),
				false,BlockUtils.stoneLike);
		
	}
	
}
