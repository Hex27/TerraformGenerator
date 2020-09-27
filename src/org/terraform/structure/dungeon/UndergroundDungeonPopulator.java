package org.terraform.structure.dungeon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class UndergroundDungeonPopulator extends SmallDungeonPopulator{

	@Override
	public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
		MegaChunk mc = new MegaChunk(data.getChunkX(),data.getChunkZ());

		int[] spawnCoords = new int[]{data.getChunkX()*16,data.getChunkZ()*16};
		int[][] allCoords = getCoordsFromMegaChunk(tw,mc);
		for(int[] coords:allCoords){
			if(coords[0] >> 4 == data.getChunkX() && coords[1] >> 4 == data.getChunkZ()){
				spawnCoords = coords;
				break;
			}
		}
		
		int x = spawnCoords[0];//data.getChunkX()*16 + random.nextInt(16);
		int z = spawnCoords[1];//data.getChunkZ()*16 + random.nextInt(16);
		Random rand = this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ());
		
		int y = GenUtils.getHighestGround(data, x, z) - GenUtils.randInt(rand, 15, 50);
		
		if(y < 10) y = 10;
		
		while(!data.getType(x, y, z).isSolid()){
			y--;
		}
		
		spawnDungeonRoom(x,y,z,tw,rand,data);
	}
	
	public void spawnDungeonRoom(int x, int y, int z, TerraformWorld tw, Random rand,
			PopulatorDataAbstract data){
		TerraformGeneratorPlugin.logger.info("Spawning Underground Dungeon at " + x + "," + y  + "," + z);
		CubeRoom room = new CubeRoom(GenUtils.randOddInt(rand,9,15), 
				GenUtils.randOddInt(rand,9,15),
				GenUtils.randInt(rand,5,7),
				 x, y, z);
		
		room.fillRoom(data, new Material[]{
				Material.COBBLESTONE,
				Material.MOSSY_COBBLESTONE},
				Material.CAVE_AIR);
		
		//Make some fence pattern.
		for(Entry<Wall,Integer> entry:room.getFourWalls(data, 0).entrySet()){
			Wall w = entry.getKey().getRelative(0, 1, 0);
			int length = entry.getValue();
			while(length >= 0){
				if(length % 2 == 0 || length == 0 || length == entry.getValue()){
					
				}else
					w.CAPillar(room.getHeight()-3, rand, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
				
				for(int h = 0; h < room.getHeight()-3; h++){
					BlockUtils.correctSurroundingMultifacingData(w.getRelative(0,h,0).get());
				}
				
				length--;
				w = w.getLeft();
			}
		}
		
		//Holes
		for(int i = 0; i < GenUtils.randInt(rand,0,3); i++){
			int[] coords = room.randomCoords(rand);
			int nX = coords[0];
			int nY = coords[1];
			int nZ = coords[2];
			BlockUtils.replaceSphere(rand.nextInt(992), GenUtils.randInt(rand,1,3), new SimpleBlock(data,nX,nY,nZ), true, Material.CAVE_AIR);
		}
		
		//Dropdown blocks
		for(int nx = -room.getWidthX()/2; nx < room.getWidthX()/2; nx++){
			for(int nz = -room.getWidthZ()/2; nz < room.getWidthZ()/2; nz++){
				int ny = room.getHeight();
				if(GenUtils.chance(10, 13)) continue;
				dropDownBlock(new SimpleBlock(data,x+nx,y+ny,z+nz));
			}
		}
		
		//Make spikes from the ceiling
		for(int nx = -room.getWidthX()/2; nx < room.getWidthX()/2; nx++){
			for(int nz = -room.getWidthZ()/2; nz < room.getWidthZ()/2; nz++){
				int ny = room.getHeight()-1;
				if(GenUtils.chance(9, 10)) continue;
				for(int i = 0; i < GenUtils.randInt(rand,1,room.getHeight()-3);i++){
					data.setType(x+nx, y+ny, z+nz, GenUtils.randMaterial(Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));
					BlockUtils.correctSurroundingMultifacingData(new SimpleBlock(data,x+nx,y+ny,z+nz));
				}
			}
		}
		
		//Make spikes on the floor
		for(int nx = -room.getWidthX()/2; nx < room.getWidthX()/2; nx++){
			for(int nz = -room.getWidthZ()/2; nz < room.getWidthZ()/2; nz++){
				if(GenUtils.chance(9, 10)) continue;
				for(int i = 0; i < GenUtils.randInt(rand,1,room.getHeight()-3);i++){
					Wall w = new Wall(new SimpleBlock(data,x+nx, y+1, z+nz),BlockFace.NORTH);
					w.LPillar(room.getHeight()-2, rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
					BlockUtils.correctSurroundingMultifacingData(w.get());
				}
			}
		}
		
		//Place Spawner
		EntityType type = EntityType.ZOMBIE;
		switch(rand.nextInt(3)){
		case(0):
			type = EntityType.ZOMBIE;
			break;
		case(1):
			type = EntityType.SKELETON;
			break;
		case(2):
			type = EntityType.SPIDER;
			break;
		}
		data.setSpawner(x, y+1, z, type);
		
		//Spawn chests
		ArrayList<Entry<Wall,Integer>> entries = new ArrayList<>();
		HashMap<Wall, Integer> walls = room.getFourWalls(data, 1);
		for(Entry<Wall,Integer> entry:walls.entrySet()){
			if(rand.nextBoolean()){
				entries.add(entry);
			}
		}
			
		for(Entry<Wall,Integer> entry:entries){
			Wall w = entry.getKey();
			int length = entry.getValue();
			int chest = GenUtils.randInt(1,length-1);
			while(length >= 0){
				if(length == chest){
					Directional dir = (Directional) Bukkit.createBlockData(Material.CHEST);
					dir.setFacing(w.getDirection());
					w.setBlockData(dir);
					data.lootTableChest(w.get().getX(), w.get().getY(), w.get().getZ(), TerraLootTable.SIMPLE_DUNGEON);
				}
				length--;
				w = w.getLeft();
			}
		}
	}
	
	private void dropDownBlock(SimpleBlock block){
		if(block.getType().isSolid()){
			Material type = block.getType();
			block.setType(Material.CAVE_AIR);
			int depth = 0;
			while(!block.getType().isSolid()){
				block = block.getRelative(0,-1,0);
				depth++;
				if(depth > 50) return;
			}
			
			block.getRelative(0,1,0).setType(type);
		}
	}
	
//	public void spawnDungeonRoom(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z){
//		try {
//			BiomeBank biome = tw.getBiomeBank(x, y, z);
//			y += GenUtils.randInt(random, 1, 3);
//			TerraSchematic farmHouse = TerraSchematic.load("farmhouse", new Location(tw.getWorld(),x,y,z));
//			farmHouse.parser = new FarmhouseSchematicParser(biome,random,data);
//			farmHouse.setFace(BlockUtils.getDirectBlockFace(random));
//			farmHouse.apply();
//
//			TerraformGeneratorPlugin.logger.info("Spawning farmhouse at " + x + "," + y + "," + z + " with rotation of " + farmHouse.getFace().toString());
//		} catch (Throwable e) {
//			TerraformGeneratorPlugin.logger.error("Something went wrong trying to place farmhouse at " + x + "," + y + "," + z + "!");
//			e.printStackTrace();
//		}
//	}
	
}
