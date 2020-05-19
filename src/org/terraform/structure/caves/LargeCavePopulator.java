package org.terraform.structure.caves;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.util.Vector;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.StructurePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;
import org.terraform.utils.FastNoise.NoiseType;

public class LargeCavePopulator extends StructurePopulator{

	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
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
		Random rand = tw.getHashedRand(x, z, 999323);
		
		int highest = GenUtils.getHighestGround(data,x,z);
		int rY = (highest - 20)/2; //5 block padding bottom, 15 padding top.
//		if(rand.nextBoolean())
//			createLargeCave(tw,rand,data,rY,x,rY+6,z);
//		else
			new MushroomCavePopulator().createLargeCave(tw,rand,data,rY,x,rY+6,z);
	}
	
	public void createLargeCave(TerraformWorld tw, Random rand, PopulatorDataAbstract data, int rY, int x, int y, int z){
		TerraformGeneratorPlugin.logger.info("Generating Large Cave at " + x + "," + y + "," +z);
		int rX = GenUtils.randInt(rand, 30, 50);
		int rZ = GenUtils.randInt(rand, 30, 50);
		
		//Create main cave hole
		carveCaveSphere(rand.nextInt(876283),rX,rY,rZ,new SimpleBlock(data,x,y,z));
	
		//Decrease radius to only spawn spikes away from corners
		rX -= 10;
		rZ -= 10;
		
		for(int nx = x - rX; nx <= x + rX; nx++){
			for(int nz = z - rZ; nz <= z + rZ; nz++){
				
//				//Create water at the base.
//				for(int ny = y - (3*rY)/5; ny > y-rY-5; ny--){
//					if(!data.getType(nx, ny, nz).isSolid()){
//						data.setType(nx,ny,nz,Material.WATER);
//					}
//				}
				
				//Stalagmites  &Stalactites
				if(GenUtils.chance(rand,3,100)){
					if(rand.nextBoolean()){
						int ceil = getCaveCeiling(data,nx,y,nz);
						if(ceil != -1){
							int r = 2;
							int h = GenUtils.randInt(rand, rY/2, (int) ((3f/2f)*rY));
							stalactite(tw,rand,data, nx, ceil, nz, r, h);
						}
					}else{
						int ground = getCaveFloor(data,nx,y,nz);
						if(ground != -1){
							int r = 2;
							int h = GenUtils.randInt(rand, rY/2, (int) ((3f/2f)*rY));
							stalagmite(tw,rand,data, nx, ground, nz, r, h);
						}
					}
				}
				
				//Low luminosity sea pickles
				if(GenUtils.chance(rand,4,100)){
					int ground = getCaveFloor(data,nx,y,nz);
						if(data.getType(nx, ground, nz).isSolid()
								&& data.getType(nx, ground+1, nz) == Material.WATER){
							SeaPickle sp = (SeaPickle) Bukkit.createBlockData(Material.SEA_PICKLE);
							sp.setPickles(GenUtils.randInt(1,2));
							data.setBlockData(nx, ground+1, nz, sp);
						}
				}
			}
		}
	}
	
	public int getCaveCeiling(PopulatorDataAbstract data, int x, int y, int z){
		int ny = y;
		int highest = GenUtils.getHighestGround(data,x,z);
		while(ny < highest && !data.getType(x,ny,z).isSolid()){
			ny++;
		}
		if(ny >= highest) return -1;
		return ny;
	}
	
	public int getCaveFloor(PopulatorDataAbstract data, int x, int y, int z){
		int ny = y;
		while(ny > 2 && !data.getType(x,ny,z).isSolid()){
			ny--;
		}
		if(ny <= 2) return 2;
		return ny;
	}
	
	@Override
	public boolean canSpawn(Random rand,TerraformWorld tw, int chunkX, int chunkZ,ArrayList<BiomeBank> biomes) {

		MegaChunk mc = new MegaChunk(chunkX,chunkZ);
		int[][] allCoords = getCoordsFromMegaChunk(tw,mc);
		for(int[] coords:allCoords){
			if(coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ){
				return true;
			}
		}
		return false;
	}
	
	//Each mega chunk has 1 large cave
	protected int[][] getCoordsFromMegaChunk(TerraformWorld tw,MegaChunk mc){
		return new int[][]{
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),78889279)),
				//mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),7245322)),
		};
	}

	@Override
	public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
		MegaChunk mc = new MegaChunk(rawX,0,rawZ);
		
		double minDistanceSquared = Integer.MAX_VALUE;
		int[] min = null;
		for(int nx = -1; nx <= 1; nx++){
			for(int nz = -1; nz <= 1; nz++){
				for(int[] loc:getCoordsFromMegaChunk(tw,mc.getRelative(nx, nz))){
					double distSqr = Math.pow(loc[0]-rawX,2) + Math.pow(loc[1]-rawZ,2);
					if(distSqr < minDistanceSquared){
						minDistanceSquared = distSqr;
						min = loc;
					}
				}
			}
		}
		return min;
	}
	
	public static void carveCaveSphere(int seed, float rX, float rY, float rZ, SimpleBlock block){
		if(rX <= 0.5 &&
				rY <= 0.5 &&
				rZ <= 0.5){
			return;
		}
		
		Random rand = new Random(seed);
		FastNoise noise = new FastNoise(seed);
		noise.SetNoiseType(NoiseType.Simplex);
		noise.SetFrequency(0.09f);
		for(float x = -rX; x <= rX; x++){
			for(float y = -rY; y <= rY; y++){
				for(float z = -rZ; z <= rZ; z++){
					
					SimpleBlock rel = block.getRelative((int)Math.round(x),(int)Math.round(y),(int)Math.round(z));
					
					//Never above surface.
					if(rel.getY() >= GenUtils.getHighestGround(rel.getPopData(), rel.getX(), rel.getZ())-10)
						continue;
					
					double equationResult = Math.pow(x,2)/Math.pow(rX,2)
							+ Math.pow(y,2)/Math.pow(rY,2)
							+ Math.pow(z,2)/Math.pow(rZ,2);
						double n = 0.7*noise.GetNoise(rel.getX(), rel.getY(), rel.getZ());
						if(n < 0) n = 0;
					if(equationResult <= 1+n){
						if((BlockUtils.isStoneLike(rel.getType()) 
								&& rel.getType() != Material.COBBLESTONE)
								||!rel.getType().isSolid()
								|| rel.getType() == Material.STONE_SLAB
								|| rel.getType() == Material.ICE
								|| rel.getType() == Material.PACKED_ICE
								|| rel.getType() == Material.BLUE_ICE
								|| rel.getType() == Material.OBSIDIAN
								|| rel.getType() == Material.MAGMA_BLOCK
								|| rel.getType().toString().endsWith("WALL")){
							
							//Lower areas are water.
							if(y < 0 && Math.abs(y) >= 0.8*rY){
								rel.setType(Material.WATER);
							}else{
								//Replace drop blocks and water
								if(rel.getRelative(0,1,0).getType() == Material.SAND
										|| rel.getRelative(0,1,0).getType() == Material.GRAVEL
										|| rel.getRelative(0,1,0).getType() == Material.WATER)
									rel.getRelative(0,1,0).setType(Material.DIRT);
								
								//Replace water
								for(BlockFace face:BlockUtils.directBlockFaces){
									if(rel.getRelative(face).getType() == Material.WATER)
										rel.getRelative(face).setType(Material.DIRT);
								}
								
								//Carve the cave.
								rel.setType(Material.CAVE_AIR);
							}
						}
					}
				}
			}
		}
	}

	public static void stalagmite(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, int baseRadius, int height){
		
		//Vector one to two;
		Vector base = new Vector(x,y,z);
		Vector base2 = new Vector(x,y+height,z);
		Vector v = base2.subtract(base);
		Vector unitV = v.clone().multiply(1/v.length());
		int segments = height;
		SimpleBlock one = new SimpleBlock(data,x,y,z);
		double radius = baseRadius;
		for(int i=0; i<=segments; i++){
			Vector seg = v.clone().multiply((float) ((float)i)/((float)segments));
			SimpleBlock segment = one.getRelative(seg);

			BlockUtils.replaceSphere((int) (tw.getSeed()*12), (float)radius, 2, (float)radius, segment, false, false, Material.STONE);
			radius = ((double)baseRadius)*(1- ((double)i)/((double)segments));
		}
	}
	

	public static void stalactite(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, int baseRadius, int height){
		
		//Vector one to two;
		Vector base = new Vector(x,y,z);
		Vector base2 = new Vector(x,y-height,z);
		Vector v = base2.subtract(base);
		Vector unitV = v.clone().multiply(1/v.length());
		int segments = height;
		SimpleBlock one = new SimpleBlock(data,x,y,z);
		double radius = baseRadius;
		for(int i=0; i<=segments; i++){
			Vector seg = v.clone().multiply((float) ((float)i)/((float)segments));
			SimpleBlock segment = one.getRelative(seg);

			BlockUtils.replaceSphere((int) (tw.getSeed()*12), (float)radius, 2, (float)radius, segment, false, false, Material.STONE);
			radius = ((double)baseRadius)*(1- ((double)i)/((double)segments));
		}
	}
	
}
