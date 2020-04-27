package org.terraform.structure.monument;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
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
		return coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ &&
				rollSpawnRatio(tw,chunkX,chunkZ);
	}
	
	private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ){
		return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 92992),
				(int) (TConfigOption.STRUCTURES_MONUMENT_SPAWNRATIO
						.getDouble()*1000),
				1000);
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
		gen.registerRoomPopulator(new TreasureRoomPopulator(random, design, true, true));
		gen.registerRoomPopulator(new LevelledElderRoomPopulator(random, design, true, true));
		gen.registerRoomPopulator(new LevelledElderRoomPopulator(random, design, true, true));
		gen.registerRoomPopulator(new MiniRoomNetworkPopulator(random, design, false, false));
		gen.registerRoomPopulator(new CoralRoomPopulator(random, design, false, false));
		gen.registerRoomPopulator(new FishCageRoomPopulator(random, design, false, false));
		gen.registerRoomPopulator(new HollowPillarRoomPopulator(random, design, false, false));
		gen.registerRoomPopulator(new LanternPillarRoomPopulator(random, design, false, false));
		gen.generate(false);
		gen.fill(data, tw, Material.PRISMARINE_BRICKS, Material.PRISMARINE_BRICKS, Material.PRISMARINE);
		
		carveBaseHallways(tw,random,data,x,y,z,range);
		spawnMonumentEntrance(tw,design,random,data,x,y,z,range);
		vegetateNearby(random,data,range,x,z);
		setupGuardianSpawns(data,range,x,y,z);
	}
	
	private void entranceSegment(Wall w, Random random, MonumentDesign design){
		//Entrance hole
		for(int i = 0; i < 12; i++){
			w.getRear(i).Pillar(6, random, Material.WATER);
		}
		Stairs stair = (Stairs) Bukkit.createBlockData(Material.PRISMARINE_BRICK_STAIRS);
		stair.setWaterlogged(true);
		stair.setHalf(Half.TOP);
		stair.setFacing(w.getDirection().getOppositeFace());
		w.getRear(11).getRelative(0,5,0).setBlockData(stair);
		w.getFront().Pillar(6, random, Material.WATER);
	}
	
	public static void arch(Wall w, MonumentDesign design, Random random, int archHalfLength, int height){

		Wall arch = w.getRelative(0,height,0);
		BlockFace left = BlockUtils.getAdjacentFaces(w.getDirection())[1];
		BlockFace right = BlockUtils.getAdjacentFaces(w.getDirection())[0];

		Stairs ls = (Stairs) Bukkit.createBlockData(design.stairs());
		ls.setWaterlogged(true);
		ls.setFacing(left);

		Stairs rs = (Stairs) Bukkit.createBlockData(design.stairs());
		rs.setWaterlogged(true);
		rs.setFacing(right);
		
		//Top straight line
		for(int i = 0; i < archHalfLength-1; i++){
			if(i <= 1){
				Slab slab = (Slab) Bukkit.createBlockData(design.slab());
				arch.getLeft(i).setBlockData(slab);
				arch.getRight(i).setBlockData(slab);
			}
			arch.getLeft(i).setType(design.mat(random));
			arch.getRight(i).setType(design.mat(random));
		}
		
		//Top decor
		arch.getRelative(0,1,0).setType(Material.SEA_LANTERN);
		arch.getRelative(0,2,0).setType(design.slab());
		arch.getRelative(0,1,0).getLeft(1).setType(design.mat(random));
		arch.getRelative(0,1,0).getRight(1).setType(design.mat(random));
		arch.getRelative(0,1,0).getLeft(2).setBlockData(ls);
		arch.getRelative(0,1,0).getRight(2).setBlockData(rs);
		
		//Bending sides
		arch.getLeft(archHalfLength-2).setBlockData(ls);
		arch.getRelative(0,-1,0).getLeft(archHalfLength).setBlockData(ls);

		arch.getRight(archHalfLength-2).setBlockData(rs);
		arch.getRelative(0,-1,0).getRight(archHalfLength).setBlockData(rs);
		
		arch.getLeft(archHalfLength-1).setType(design.slab());
		arch.getRight(archHalfLength-1).setType(design.slab());
		arch.getLeft(archHalfLength-1).getRelative(0,-1,0).setType(Material.SEA_LANTERN);
		arch.getRight(archHalfLength-1).getRelative(0,-1,0).setType(Material.SEA_LANTERN);
		
		//Vertical area
		arch.getLeft(archHalfLength).getRelative(0,-2,0).downUntilSolid(random, design.tileSet);
		arch.getRight(archHalfLength).getRelative(0,-2,0).downUntilSolid(random, design.tileSet);
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
		int halfLength = 4 + random.nextInt(3);
		for(int i = 0; i < halfLength; i++){
			entranceSegment(leftClone,random,design);
			entranceSegment(rightClone,random,design);
			
			rightClone = rightClone.getRight();
			leftClone = leftClone.getLeft();
		}
		
		//Build entrance archs.
		for(int i = 0; i < 12; i+= 3){
			arch(w.getRear(i),design, random,halfLength+2,10);
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
		
		//Light the floor in the hallway
		for(int nx = x - range/2+3; nx <= x+range/2-3; nx+=2){
			data.setType(nx,y,z-range/2+3,Material.SEA_LANTERN);
			data.setType(nx,y,z+range/2-3,Material.SEA_LANTERN);
		}

		for(int nz = z - range/2+3; nz <= z+range/2-3; nz+=2){
			data.setType(x-range/2+3,y,nz,Material.SEA_LANTERN);
			data.setType(x+range/2-3,y,nz,Material.SEA_LANTERN);
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
				if(distSqr < minDistanceSquared && rollSpawnRatio(tw,loc[0]>>4,loc[1]>>4)){
					minDistanceSquared = distSqr;
					min = loc;
				}
			}
		}
		return min;
	}

	private void vegetateNearby(Random rand, PopulatorDataAbstract data, int range, int x, int z){
		int i = 25;
		for(int nx = x - range/2 - i; nx <= x+range/2 + i; nx++){
			for(int nz = z - range/2 - i; nz <= z+range/2 + i; nz++){
				if(GenUtils.chance(rand,2,5)){
					int y = GenUtils.getTrueHighestBlock(data, nx, nz);
					//Don't place on weird blocks
					if(data.getType(nx, y, nz).toString().contains("SLAB") ||
							data.getType(nx, y, nz).toString().contains("STAIR") ||
							data.getType(nx, y, nz).toString().contains("WALL"))
						continue;
					if(y < TerraformGenerator.seaLevel){
						if(GenUtils.chance(rand,9,10))
							CoralGenerator.generateKelpGrowth(data, nx, y+1, nz);
						else
							CoralGenerator.generateSeaPickles(data, nx, y+1, nz);
					}
				}
			}
		}
	}
	
	private void setupGuardianSpawns(PopulatorDataAbstract data, int range, int x, int y, int z){
		int i = -5;
		ArrayList<Integer> done = new ArrayList<>();
		for(int nx = x - range/2 - i; nx <= x+range/2 + i; nx++){
			for(int nz = z - range/2 - i; nz <= z+range/2 + i; nz++){
				int chunkX = nx>>4;
				int chunkZ = nz>>4;
				int hash = Objects.hash(chunkX,chunkZ);
				
				if(done.contains(hash)) 
					continue;
				
				done.add(hash);

				TerraformGeneratorPlugin.injector.getICAData(((PopulatorDataPostGen) data).getWorld().getChunkAt(chunkX, chunkZ))
				.registerGuardians(x - range/2,y,z - range/2,
						x + range/2,TerraformGenerator.seaLevel,z + range/2);
			}
		}
		
	}
	

	

}
