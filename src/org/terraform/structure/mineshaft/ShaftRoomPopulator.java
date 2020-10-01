package org.terraform.structure.mineshaft;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class ShaftRoomPopulator extends RoomPopulatorAbstract{

	public ShaftRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
		super(rand, forceSpawn, unique);
	}

	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room) {
		//Carve upward shaft. Replace stone-like objects and cave decorations.
		for(int i = 8; i < 20; i++){
			BlockUtils.carveCaveAir(new Random().nextInt(777123), 
					(room.getWidthX()-4)/2, 
					5, 
					(room.getWidthZ()-4)/2, 
					new SimpleBlock(data,room.getX(),room.getY()+i,room.getZ()),
					false,
					new ArrayList<Material>(){{
						add(Material.BARRIER);
					}});
		}

		int[] lowerCorner = room.getLowerCorner(3);
		int[] upperCorner = room.getUpperCorner(3);
		
		//Spawn a solid stone platform
		int y = room.getY();
		for(int x = lowerCorner[0]; x <= upperCorner[0]; x++){
			for(int z = lowerCorner[1]; z <= upperCorner[1]; z++){
				SimpleBlock b = new SimpleBlock(data,x,y,z);
				if(b.getType() == Material.CAVE_AIR
						|| b.getType() == Material.OAK_PLANKS
						|| b.getType() == Material.OAK_SLAB
						|| b.getType() == Material.GRAVEL){
					b.setType(GenUtils.randMaterial(
							Material.STONE_BRICKS,
							Material.CRACKED_STONE_BRICKS,
							Material.MOSSY_STONE_BRICKS,
							Material.MOSSY_COBBLESTONE,
							Material.COBBLESTONE,
							Material.CAVE_AIR));
				}
			}
		}
		
		//Support pillars
		for(int[] corner:room.getAllCorners(3)){
			int x = corner[0];
			int z = corner[1];
			Wall w = new Wall(new SimpleBlock(data,x,room.getY()+1,z));
			w.getRelative(0,-1,0).downUntilSolid(rand, Material.OAK_LOG);
		}
	}
	@Override
	public boolean canPopulate(CubeRoom room) {
		return true;
	}
	
	

}
