package org.terraform.structure.monument;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.animalfarm.AnimalFarmPopulator;
import org.terraform.structure.farmhouse.FarmhousePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class MonumentPopulator extends StructurePopulator{

	@Override
	public boolean canSpawn(Random rand,TerraformWorld tw, int chunkX, int chunkZ,ArrayList<BiomeBank> biomes) {

		MegaChunk mc = new MegaChunk(chunkX,chunkZ);
		int[] coords = getCoordsFromMegaChunk(tw,mc);
		
		for(BiomeBank biome:biomes){
			if(biome.getType() != BiomeType.DEEP_OCEANIC)
				return false;
		}
		return coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ;
	}

	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {

		ArrayList<BiomeBank> banks = new ArrayList<>();
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int height = new HeightMap().getHeight(tw, x, z);//GenUtils.getTrueHighestBlock(data, x, z);
				for(BiomeBank bank:BiomeBank.values()){
					BiomeBank currentBiome = tw.getBiomeBank(x, height, z);//BiomeBank.calculateBiome(tw,tw.getTemperature(x, z), height);
					
					//Must be in deep ocean.
					if(currentBiome.getType() != BiomeType.DEEP_OCEANIC) 
						return;
					
					if(bank == currentBiome){
						if(!banks.contains(bank))
							banks.add(bank);
						break;
					}
				}
			}
		}
		int[] coords = getCoordsFromMegaChunk(tw,new MegaChunk(data.getChunkX(),data.getChunkZ()));
		int x = coords[0];
		int z = coords[1];
		int y = GenUtils.getHighestGround(data, x, z);
		
		spawnMonument(tw, tw.getHashedRand(x, y, z, 9299724), data, x,y,z);
	}
	
	public void spawnMonument(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z){
		TerraformGeneratorPlugin.logger.info("Spawning Monument at: " + x + "," + z);
		MonumentDesign design = MonumentDesign.values()[random.nextInt(MonumentDesign.values().length)];
		int numRooms = 1000;
		int range = 50;
		spawnMonumentBase(tw,design,random,data,x,y,z,range);
		
		Random hashedRand = tw.getHashedRand(x, y, z);
		RoomLayoutGenerator gen = new RoomLayoutGenerator(hashedRand,RoomLayout.RANDOM_BRUTEFORCE,numRooms,x,y,z,range);
		gen.setPathPopulator(new MonumentPathPopulator(design, tw.getHashedRand(x, y, z, 77)));
		gen.setRoomMaxX(15);
		gen.setRoomMaxZ(15);
		gen.setRoomMinX(10);
		gen.setRoomMinZ(10);
		gen.setRoomMaxHeight(22);
		gen.setRoomMinHeight(9);
		//gen.setPyramid(false);
		//gen.setAllowOverlaps(false);
		gen.registerRoomPopulator(new MonumentRoomPopulator(random, design, false, false));
		gen.generate(false);
		gen.fill(data, tw, Material.PRISMARINE_BRICKS, Material.PRISMARINE_BRICKS, Material.PRISMARINE);
		
		carveBaseHallways(tw,random,data,x,y,z,range);
		spawnMonumentEntrance(tw,design,random,data,x,y,z,range);
	}
	
	private void entranceSegment(Wall w, Random random, MonumentDesign design){
		//Entrance hole
		for(int i = 0; i < 12; i++){
			w.getRear(i).Pillar(6, random, Material.WATER);
		}
		w.getFront().Pillar(6, random, Material.WATER);
		//Ceiling
		//w.getRelative(0,4,0).setType(design.mat(random));
		//w.getRear().getRelative(0,4,0).setType(GenUtils.randMaterial(Material.PRISMARINE_BRICKS, Material.PRISMARINE_BRICKS, Material.PRISMARINE));
		//w.getRear(2).getRelative(0,4,0).setType(GenUtils.randMaterial(Material.PRISMARINE_BRICKS, Material.PRISMARINE_BRICKS, Material.PRISMARINE));
		//w.getRear(3).getRelative(0,4,0).setType(GenUtils.randMaterial(Material.PRISMARINE_BRICKS, Material.PRISMARINE_BRICKS, Material.PRISMARINE));
	}
	
	/**
	 * Carve a monument entrance.
	 */
	public void spawnMonumentEntrance(TerraformWorld tw, MonumentDesign design, Random random, PopulatorDataAbstract data, int x, int y, int z, int range){
		range += 38;
		BlockFace dir = BlockUtils.getDirectBlockFace(random);
		SimpleBlock base = new SimpleBlock(data,x,y+1,z);
		for(int i = 0; i < range/2; i++)
			base = base.getRelative(dir);
		Wall w = new Wall(base,dir);
		Wall leftClone = w.clone();
		Wall rightClone = w.clone();
		for(int i = 0; i < 4 + random.nextInt(3); i++){
			entranceSegment(leftClone,random,design);
			entranceSegment(rightClone,random,design);
			
			rightClone = rightClone.getRight();
			leftClone = leftClone.getLeft();
		}
		
//		for(int i = 0; i < 5; i++){
//			if(i % 2 == 0){
//				design.upSpire(rightClone.getRelative(0,6,0).get(), random);
//				design.upSpire(leftClone.getRelative(0,6,0).get(), random);
//			}
//			
//			//rightClone.Pillar(6, random, Material.PRISMARINE_BRICKS, Material.PRISMARINE_BRICKS, Material.PRISMARINE);
//			rightClone = rightClone.getRear();
//			//leftClone.Pillar(6, random, Material.PRISMARINE_BRICKS, Material.PRISMARINE_BRICKS, Material.PRISMARINE);
//			leftClone = leftClone.getRear();
//		}
	}
	
	/**
	 * Spawns a pyramid-ish base
	 */
	public void spawnMonumentBase(TerraformWorld tw, MonumentDesign design, Random random, PopulatorDataAbstract data, int x, int y, int z, int range){
		range += 30;
		for(int i = 6; i >= 0; i--){
			for(int nx = x - range/2 - i; nx <= x+range/2 + i; nx++){
				for(int nz = z - range/2 - i; nz <= z+range/2 + i; nz++){
					
					//Spires on the corners
					if(i % 2 == 0)
						if(nx == x-range/2-i || nx == x+range/2+i){
							if(nz == z - range/2 - i||nz == z+range/2+i){
								design.spire(new Wall(new SimpleBlock(data, nx, y+(6-i)+1, nz),BlockFace.NORTH),random);
							}
						}
					

					data.setType(nx, y+(6-i), nz,GenUtils.randMaterial(random,Material.PRISMARINE_BRICKS, Material.PRISMARINE_BRICKS, Material.PRISMARINE));
					
					//data.setType(nx, y+(6-i), nz,Material.GLASS);
					
					
				}
			}
		}
		
		//Spawn large lamps
		int pad = 5;
		lightPlatform(data, x - range/2 + pad, y + 7, z - range/2 + pad);
		design.spawnLargeLight(data, x - range/2 + pad, y + 8, z - range/2 + pad);
		
		lightPlatform(data, x + range/2 - pad, y + 7, z - range/2 + pad);
		design.spawnLargeLight(data, x + range/2 - pad, y + 8, z - range/2 + pad);
		
		lightPlatform(data, x + range/2 - pad, y + 7, z + range/2 - pad);
		design.spawnLargeLight(data, x + range/2 - pad, y + 8, z + range/2 - pad);
		
		lightPlatform(data, x - range/2 + pad, y + 7, z + range/2 - pad);
		design.spawnLargeLight(data, x - range/2 + pad, y + 8, z + range/2 - pad);
	}
	
	/**
	 * Create a small platform.
	 */
	private void lightPlatform(PopulatorDataAbstract data, int x, int y, int z){
		for(int nx = -2; nx <= 2; nx++)
			for(int nz = -2; nz <= 2; nz++){
				data.setType(x+nx,y,z+nz,Material.PRISMARINE_BRICKS);
			}
	}
	
	/**
	 * Carves a main hallway in the monument
	 */
	public void carveBaseHallways(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, int range){
		range += 29;
		for(int ny = y+1; ny <= y + 4; ny++){
			for(int nx = x - range/2 ; nx <= x+range/2 ; nx++){
				for(int nz = z - range/2 ; nz <= z+range/2 ; nz++){
					
					//Don't touch the middle
					if(nx > x + 5 - range/2
							&& nx < x - 5 + range/2
							&& nz > z + 5 - range/2
							&& nz < z - 5 + range/2)
						continue;
					data.setType(nx, ny, nz,Material.WATER);
				}
			}
		}
	}

	protected int[] getCoordsFromMegaChunk(TerraformWorld tw,MegaChunk mc){
		return mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),17322223));
	}

	@Override
	public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
		MegaChunk mc = new MegaChunk(rawX,0,rawZ);
		
		double minDistanceSquared = Integer.MAX_VALUE;
		int[] min = null;
		for(int nx = -1; nx <= 1; nx++){
			for(int nz = -1; nz <= 1; nz++){
				int[] loc = getCoordsFromMegaChunk(tw,mc.getRelative(nx, nz));
				double distSqr = Math.pow(loc[0]-rawX,2) + Math.pow(loc[1]-rawZ,2);
				if(distSqr < minDistanceSquared){
					minDistanceSquared = distSqr;
					min = loc;
				}
			}
		}
		return min;
	}


	

	

}
