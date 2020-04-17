package org.terraform.structure.monument;

import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Waterlogged;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.GenUtils;

public class CageRoomPopulator extends MonumentRoomPopulator {

	public CageRoomPopulator(Random rand, MonumentDesign design, boolean forceSpawn,
			boolean unique) {
		super(rand, design, forceSpawn, unique);
	}
	
	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room){
		super.populate(data, room);
		if(GenUtils.chance(rand, 1,5)) 
			return;
		for(Entry<Wall,Integer> entry:room.getFourWalls(data, 0).entrySet()){
			Wall w = entry.getKey().getRelative(0,7,0);
			int length = entry.getValue();
			for(int i=0;i<length;i++){
				if(i % 2 == 0){
					Waterlogged wall = (Waterlogged) Bukkit.createBlockData(Material.PRISMARINE_WALL);
					if(w.get().getY() <= TerraformGenerator.seaLevel)
						wall.setWaterlogged(true);
					else wall.setWaterlogged(false);
					for(int j = 0; j < room.getHeight()-9; j++){
						w.getRelative(0,j,0).setBlockData(wall);
					}
					//w.Pillar(room.getHeight()-9, rand, Material.PRISMARINE_WALL);
				}else{
					w.Pillar(room.getHeight()-9, rand, Material.WATER);
				}
				w = w.getLeft();
			}
		}
		
		//Corners are dark prismarine
		for(int[] corner:room.getAllCorners()){
			for(int i = 0; i < room.getHeight(); i++){
				if(data.getType(corner[0],i+room.getY(),corner[1]).isSolid()){
					data.setType(corner[0],i+room.getY(),corner[1],Material.DARK_PRISMARINE);
				}
			}
		}
	}

}
