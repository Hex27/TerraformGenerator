package org.terraform.structure.farmhouse;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.block.data.type.TrapDoor;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class FarmhouseRoomPopulator extends RoomPopulatorAbstract{

	private boolean door = false;
	private BiomeBank biome;
	public FarmhouseRoomPopulator(Random rand, boolean forceSpawn, boolean unique, BiomeBank biome) {
		super(rand, forceSpawn, unique);
		this.biome = biome;
	}
	
	private Slab randTopSlab(){
		Slab slab = (Slab) Bukkit.createBlockData(BlockUtils.stoneBrickSlab(rand));
		slab.setType(Type.TOP);
		return slab;
	}

	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room) {
		int[] lowerBounds = room.getLowerCorner();
		int[] upperBounds = room.getUpperCorner();
		
		//Make some windows and a doorway
		int doors = 0;
		for(Entry<Wall, Integer> entry:room.getFourWalls(data, 0).entrySet()){
			Wall w = entry.getKey();
			for(int i = 0; i < entry.getValue(); i++){
				
				//Don't make the corners windows or doors.
				if((i != 0 && i != entry.getValue()-1) && 
						w.getRelative(0,1,0).getType().toString().contains("COBBLESTONE")){  //Don't make air blocks windows/doors
					if(i % 5 == 3||i % 5 == 4){ //windows
//						TrapDoor t = (TrapDoor) Bukkit.createBlockData(BlockUtils.getWoodForBiome(biome, "TRAPDOOR"));
//						t.setFacing(w.getDirection().getOppositeFace());
//						t.setHalf(Half.BOTTOM);
//						t.setOpen(true);
						w.getRelative(0,1,0).setType(Material.AIR);
						//BlockUtils.correctSurroundingMultifacingData(w.get().getRelative(0,1,0));
					}else if(!door){
						if(!w.getRear().getType().isSolid()){
							door = true;
							w.setType(Material.AIR);
							w.getRelative(0,1,0).setType(Material.AIR);
							BlockUtils.placeDoor(data, 
									BlockUtils.getWoodForBiome(biome, "DOOR"),
									w.get().getX(), w.get().getY(), w.get().getZ(), w.getDirection());
							w.getLeft().Pillar(2, rand, Material.SMOOTH_STONE);
							w.getRight().Pillar(2, rand, Material.SMOOTH_STONE);
							w.getRelative(0, 2, 0).setType(Material.CHISELED_STONE_BRICKS);
						}
					}
				}
				w = w.getLeft();
			}
		}
		
		//Lowest layers are cobblestone. All space below the house is filled with cobble
		for(int x = lowerBounds[0]; x <= upperBounds[0]; x++)
			for(int z = lowerBounds[1]; z <= upperBounds[1]; z++){
				if(x == lowerBounds[0] || x == upperBounds[0] || z == lowerBounds[1] || z == upperBounds[1]){
//					if(data.getType(x, room.getY()+1, z).toString().contains("COBBLESTONE")) {
//						data.setType(x,room.getY(),z,GenUtils.randMaterial(rand,Material.COBBLESTONE,Material.COBBLESTONE,Material.COBBLESTONE,Material.MOSSY_COBBLESTONE));
//						data.setType(x,room.getY()+1,z,GenUtils.randMaterial(rand,Material.COBBLESTONE,Material.COBBLESTONE,Material.COBBLESTONE,Material.MOSSY_COBBLESTONE));
//					}
				}else{
					data.setType(x,room.getY(),z,Material.STONE_BRICKS);
				}
				BlockUtils.setDownUntilSolid(x, room.getY()-1, z, data, Material.COBBLESTONE,Material.COBBLESTONE,Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
			}
		
		//X logs
		for(int x = lowerBounds[0]-1; x <= upperBounds[0]+1; x++){
			Orientable logData = (Orientable) Bukkit.createBlockData(BlockUtils.getWoodForBiome(biome, "LOG"));
			logData.setAxis(Axis.X);
			
			if(data.getType(x,room.getHeight()+room.getY(),upperBounds[1]) != Material.CAVE_AIR)
				data.setBlockData(x,room.getHeight()+room.getY(),upperBounds[1],logData);
			
			if(data.getType(x,room.getHeight()+room.getY(),lowerBounds[1]) != Material.CAVE_AIR)
				data.setBlockData(x,room.getHeight()+room.getY(),lowerBounds[1],logData);
		}
		
		//Z logs
		for(int z = lowerBounds[1]-1; z <= upperBounds[1]+1; z++){
			Orientable logData = (Orientable) Bukkit.createBlockData(BlockUtils.getWoodForBiome(biome, "LOG"));
			logData.setAxis(Axis.Z);
			
			if(data.getType(lowerBounds[0],room.getHeight()+room.getY(),z) != Material.CAVE_AIR)
				data.setBlockData(lowerBounds[0],room.getHeight()+room.getY(),z,logData);
			
			if(data.getType(upperBounds[0],room.getHeight()+room.getY(),z) != Material.CAVE_AIR)
				data.setBlockData(upperBounds[0],room.getHeight()+room.getY(),z,logData);
		}
		
		//Set the inside of the roof to air
		for(int x = lowerBounds[0]+1; x <= upperBounds[0]-1;x++){
			for(int z = lowerBounds[1]+1; z <= upperBounds[1]-1;z++){
				data.setType(x,room.getY()+room.getHeight(),z,Material.CAVE_AIR);
			}
		}
		
		//The 4 corners are logs, if it intersects.
		for(int[] coords:room.getAllCorners()){
			Wall w = new Wall(new SimpleBlock(data,coords[0],room.getY(),coords[1]),BlockFace.NORTH);
			if(w.getRelative(0,1,0).getType() == Material.CAVE_AIR) 
				continue;
			w.Pillar(room.getHeight()+1, rand, BlockUtils.getWoodForBiome(biome,"LOG"));
		}
		
		
		//Roof
		double height = room.getY()+room.getHeight()+1;
		for(int i = -1; i < 6; i++){
			if(lowerBounds[0] + i >= upperBounds[0]-i||
					lowerBounds[1] + i >= upperBounds[1]-i) 
				break;
			//fill with wood planks
			for(int x = lowerBounds[0]+i; x <= upperBounds[0]-i;x++){
				for(int z = lowerBounds[1]+i; z <= upperBounds[1]-i;z++){
					if(height == (int) height){
						if(!data.getType(x, (int)height, z).toString().endsWith("PLANKS"))
							data.setType(x,(int)height,z,BlockUtils.getWoodForBiome(biome,"SLAB"));
					}else{
						data.setType(x,(int)height,z,BlockUtils.getWoodForBiome(biome,"PLANKS"));
					}
				}
			}
			height+=0.5;
		}
	}

	@Override
	public boolean canPopulate(CubeRoom room) {
		return true;
	}
}