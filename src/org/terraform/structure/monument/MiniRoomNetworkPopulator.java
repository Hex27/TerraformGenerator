package org.terraform.structure.monument;

import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

public class MiniRoomNetworkPopulator extends MonumentRoomPopulator {

	public MiniRoomNetworkPopulator(Random rand, MonumentDesign design, boolean forceSpawn,
			boolean unique) {
		super(rand, design, forceSpawn, unique);
	}
	
	@Override
	public boolean canPopulate(CubeRoom room){
		return room.getHeight() <= 13;
	}
	
	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room){
		super.populate(data, room);
		
		//Make the hashtag room structure
		for(Entry<Wall,Integer> entry:room.getFourWalls(data, 4).entrySet()){
			Wall w = entry.getKey();
			int l = entry.getValue();
			for(int i = 0; i < l + 4; i++){
				w.RPillar(room.getHeight()-1, rand, design.tileSet());
				
				//Lighting
				if(i%2==0)
					w.setType(Material.SEA_LANTERN);
				
				//Center hole
				if(i == l/2){
					w.getRelative(0,2,0).setType(Material.WATER);
				}
				
				//Side holes
				if(i == l+2){
					w.getRelative(0,2,0).setType(Material.WATER);
				}
				w = w.getLeft();
			}
		}
		
		Wall center = new Wall(new SimpleBlock(data, room.getX(),room.getY(),room.getZ()),BlockFace.NORTH);
		tetrapod(center);
	}
	
	public void tetrapod(Wall w){
		for(int width = 0; width < 3; width++){
			if(width%2==1){
				w.getLeft(width).RPillar(5, rand, design.tileSet());
				w.getRight(width).RPillar(5, rand, design.tileSet());
			}else{
				w.getLeft(width).getRelative(0,2,0).setType(design.mat(rand));
				w.getRight(width).getRelative(0,2,0).setType(design.mat(rand));
			}
		}
		w.getLeft().getRear().getRelative(0,2,0).setType(Material.SEA_LANTERN);
		w.getLeft().getFront().getRelative(0,2,0).setType(Material.SEA_LANTERN);
		w.getRight().getRear().getRelative(0,2,0).setType(Material.SEA_LANTERN);
		w.getRight().getFront().getRelative(0,2,0).setType(Material.SEA_LANTERN);
	}

}
