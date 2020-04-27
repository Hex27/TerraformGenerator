package org.terraform.structure.monument;

import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;

public class DecoratedSidesElderRoomPopulator extends MonumentRoomPopulator {

	public DecoratedSidesElderRoomPopulator(Random rand, MonumentDesign design,
			boolean forceSpawn, boolean unique) {
		super(rand, design, forceSpawn, unique);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room) {
		super.populate(data, room);
		int x = room.getX();
		int y = room.getY() + room.getHeight()/2;
		int z = room.getZ();
		TerraSchematic schema;
		
		//Stairs at the top
		for(Entry<Wall,Integer> walls:room.getFourWalls(data, 2).entrySet()){
			Wall w = walls.getKey().getRelative(0,room.getHeight()-2,0);
			int length = walls.getValue();
			for(int j = 0; j < length; j++){
				
				Stairs stair = (Stairs) Bukkit.createBlockData(design.stairs());
				stair.setFacing(w.getDirection().getOppositeFace());
				stair.setWaterlogged(true);
				stair.setHalf(Half.TOP);
				w.setBlockData(stair);
				
				w = w.getLeft();
			}
		}
		
		//Decorated walls
		for(Entry<Wall,Integer> walls:room.getFourWalls(data, 1).entrySet()){
			Wall w = walls.getKey();
			int length = walls.getValue();
			for(int j = 0; j < length; j++){
				if(!w.getRear().getType().isSolid()){
					Wall wall = w.getRelative(0,4,0);
					wall.LPillar(room.getHeight()-4,true,rand,Material.SEA_LANTERN,Material.DARK_PRISMARINE);
				}else{
					if(j % 2 == 0)
						w.LPillar(room.getHeight()-1, rand, Material.PRISMARINE_BRICKS);
					else{
						w.LPillar(room.getHeight()-1, rand, Material.PRISMARINE);
						w.getRelative(0,3,0).Pillar(4, rand, Material.SEA_LANTERN);
					}
					w.setType(Material.DARK_PRISMARINE);
					w.getRelative(0,room.getHeight()-2,0).setType(Material.DARK_PRISMARINE);
				}
				w = w.getLeft();
			}
		}
		
		//Elder
		data.addEntity(room.getX(), 
				room.getY()+room.getHeight()/2, 
				room.getZ(), 
				EntityType.ELDER_GUARDIAN);
		
		//Corners are sea lanterns
		Waterlogged wall = (Waterlogged) Bukkit.createBlockData(Material.PRISMARINE_WALL);
		wall.setWaterlogged(true);
		
		for(int[] corner: room.getAllCorners(2)){
			data.setType(corner[0],room.getY()+room.getHeight()-1,corner[1],Material.SEA_LANTERN);
			data.setBlockData(corner[0],room.getY()+room.getHeight()-2,corner[1],wall);
		}
	}
	
	@Override
	public boolean canPopulate(CubeRoom room){
		return room.getHeight() > 7;
	}
	
}
