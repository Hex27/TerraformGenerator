package org.terraform.structure.mineshaft;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.GenUtils;

public class MineshaftPopulator extends StructurePopulator{

	private static ArrayList<int[]> positions;
	
	public static ArrayList<int[]> strongholdPositions(TerraformWorld tw){
		if(positions != null) return positions;
		Random rand = tw.getRand(3);
		ArrayList<int[]> positions = new ArrayList<>();
		int radius = 1408;
		for(int i = 0; i < 3; i++){
			int[] coords = randomCircleCoords(rand,radius);
			TerraformGeneratorPlugin.logger.info("Will spawn stronghold at: " + coords[0] + "," + coords[1]);

			positions.add(coords);
		}
		radius += 3072;
		//TerraformGeneratorPlugin.logger.debug("sp-1");
		for(int i = 0; i < 6; i++) positions.add(randomCircleCoords(rand,radius));
		radius += 3072;
		//TerraformGeneratorPlugin.logger.debug("sp-2");
		for(int i = 0; i < 10; i++) positions.add(randomCircleCoords(rand,radius));
		radius += 3072;
		//TerraformGeneratorPlugin.logger.debug("sp-3");
		for(int i = 0; i < 15; i++) positions.add(randomCircleCoords(rand,radius));
		radius += 3072;
		//TerraformGeneratorPlugin.logger.debug("s-pop-4");
		for(int i = 0; i < 21; i++) positions.add(randomCircleCoords(rand,radius));
		radius += 3072;
		//TerraformGeneratorPlugin.logger.debug("s-pop-5");
		for(int i = 0; i < 28; i++) positions.add(randomCircleCoords(rand,radius));
		radius += 3072;
		//TerraformGeneratorPlugin.logger.debug("s-pop-6");
		for(int i = 0; i < 36; i++) positions.add(randomCircleCoords(rand,radius));
		radius += 3072;
		//TerraformGeneratorPlugin.logger.debug("s-pop-7");
		for(int i = 0; i < 9; i++) positions.add(randomCircleCoords(rand,radius));
		//TerraformGeneratorPlugin.logger.debug("s-pop-8");
		MineshaftPopulator.positions = positions;
		return positions;
	}
	
	/**
	 * 
	 * @param rand
	 * @param radius
	 * @return x,z coords on the circumference of 
	 * a circle of the specified radius, center 0,0
	 */
	private static int[] randomCircleCoords(Random rand, int radius){
		double angle = Math.random()*Math.PI*2;
		int x = (int) (Math.cos(angle)*radius);
		int y = (int) (Math.sin(angle)*radius);
		return new int[]{x,y};
	}
	
	private boolean areCoordsEqual(int[] a,int[] b){
		return a[0] == b[0] && a[1] == b[1];
	}
	
	@Override
	public boolean canSpawn(Random rand,TerraformWorld tw, int chunkX, int chunkZ,ArrayList<BiomeBank> biomes) {
		ArrayList<int[]> positions = strongholdPositions(tw);
		for(int x = chunkX*16; x<chunkX*16+16;x++){
			for(int z = chunkZ*16; z<chunkZ*16+16;z++){
				for(int[] pos:positions){
					if(areCoordsEqual(pos,new int[]{x,z}))
						return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
		//TerraformGeneratorPlugin.logger.debug("s-populate");
		if(!TConfigOption.STRUCTURES_STRONGHOLD_ENABLED.getBoolean()) return;
		ArrayList<int[]> positions = strongholdPositions(tw);
		for(int x = data.getChunkX()*16; x<data.getChunkX()*16+16;x++){
			for(int z = data.getChunkZ()*16; z<data.getChunkZ()*16+16;z++){
				for(int[] pos:positions){
					if(areCoordsEqual(pos,new int[]{x,z})){
						int height = GenUtils.getHighestGround(data, x, z);
						//Strongholds start underground. Burrow down
						height -= 40;
						if(height < 3) height = 5;
						spawnMineshaft(tw,random,data,x,height,z);
						break;
					}
				}
			}
		}
		
		
	}
	
	public void spawnMineshaft(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z){
		TerraformGeneratorPlugin.logger.info("Spawning mineshaft at: " + x + "," + z);
		
		int numRooms = 10;
		int range = 150;
		
		//Level One
		Random hashedRand = tw.getHashedRand(x, y, z);
		RoomLayoutGenerator gen = new RoomLayoutGenerator(hashedRand,RoomLayout.RANDOM_BRUTEFORCE,numRooms,x,y,z,range);
		gen.setPathPopulator(new MineshaftPathPopulator(tw.getHashedRand(x, y, z, 2)));
		gen.setRoomMaxX(17);
		gen.setRoomMaxZ(17);
		gen.setRoomMinX(13);
		gen.setRoomMinZ(13);
		
		gen.registerRoomPopulator(new SmeltingHallPopulator(random, false, false));
		gen.registerRoomPopulator(new CaveSpiderDenPopulator(random, false, false));
		gen.setCarveRooms(true);
		gen.generate();
		//gen.fillPathsOnly(data, tw, Material.CAVE_AIR);
		gen.fill(data, tw, Material.CAVE_AIR);
	}

	@Override
	public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {

		double minDistanceSquared = Integer.MAX_VALUE;
		int[] min = null;
		for(int[] loc:strongholdPositions(tw)){
			double distSqr = Math.pow(loc[0]-rawX,2) + Math.pow(loc[1]-rawZ,2);
			if(distSqr < minDistanceSquared){
				minDistanceSquared = distSqr;
				min = loc;
			}
		}
		return new int[]{min[0],min[1]};
	}
	

	

}
