package org.terraform.structure.pyramid;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

public class TerracottaRoom extends RoomPopulatorAbstract{

	public TerracottaRoom(Random rand, boolean forceSpawn, boolean unique) {
		super(rand, forceSpawn, unique);
	}

	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room) {
		//Decorate the walls with Terracotta
		ArrayList<Wall> entrances = new ArrayList<>();
		for(Entry<Wall,Integer> entry:room.getFourWalls(data, 1).entrySet()) {
			Wall w = entry.getKey();
			for(int i = 0; i < entry.getValue(); i++) {
				if(w.getRear().getType().isSolid()) {
					if(!w.getRear().getLeft().getType().isSolid()||
							!w.getRear().getRight().getType().isSolid()) {
						w.Pillar(room.getHeight(), rand, Material.CHISELED_SANDSTONE);
					}else {
						if(i % 3 == 0) {
							w.Pillar(room.getHeight(), true, rand, Material.BLUE_TERRACOTTA,Material.YELLOW_TERRACOTTA);
							//w.getRear().Pillar(room.getHeight(), true, rand, Material.BLUE_TERRACOTTA,Material.BARRIER,Material.BLUE_TERRACOTTA,Material.BARRIER);
						}else {
							w.Pillar(room.getHeight(), true, rand, Material.YELLOW_TERRACOTTA,Material.BLUE_TERRACOTTA);
							//w.getRear().Pillar(room.getHeight(), true, rand, Material.BARRIER,Material.BLUE_TERRACOTTA,Material.BARRIER,Material.BLUE_TERRACOTTA);
						}
					}
				}else {
					entrances.add(w.clone());
					w.getRelative(0,3,0).Pillar(room.getHeight()-3, rand, Material.CHISELED_SANDSTONE);
				}
				
				w = w.getLeft();
			}
		}
		
		//Make sure entrances are not blocked
		for(Wall w:entrances) {
			w.Pillar(room.getHeight(),rand,Material.AIR);
		}
		entrances = null;
		
		//Decorate the floor with glazed terracotta
		int[] lowerCorner = room.getLowerCorner(2);
		int[] upperCorner = room.getUpperCorner(2);
		for(int x = lowerCorner[0]; x <= upperCorner[0]; x += 2) {
			for(int z = lowerCorner[1]; z <= upperCorner[1]; z += 2) {
				BlockUtils.horizontalGlazedTerracotta(data, x, room.getY(), z, Material.YELLOW_GLAZED_TERRACOTTA);
			}
		}
	}
	

	@Override
	public boolean canPopulate(CubeRoom room) {
		return room.getWidthX() >= 5 && room.getWidthZ() >= 5;
	}
	
	

}
