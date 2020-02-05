package org.terraform.tree;

import java.util.Random;

import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class TreeDB {
	
	public static void spawnSmallTree(Random rand, PopulatorDataAbstract data, int x, int y, int z, Material log, Material leaf){
		int height = GenUtils.randInt(rand, 5,7);
		for(int i = 0; i < height; i++){
			data.setType(x, y+i, z, log);
		}

		FastNoise noise = new FastNoise(rand.nextInt(995));
		int trueRadius = 3;
		noise.SetNoiseType(NoiseType.Simplex);
		noise.SetFrequency(0.09f);
		y += height;
		for(int nx = -trueRadius; nx <= trueRadius; nx++){
			for(int ny = -trueRadius; ny <= trueRadius; ny++){
				for(int nz = -trueRadius; nz <= trueRadius; nz++){
					//SimpleBlock rel = block.getRelative((int)Math.round(x),(int)Math.round(y),(int)Math.round(z));
					double radiusSquared = Math.pow(trueRadius+noise.GetNoise(nx+x,ny+y,nz+z)*2,2);
					if(BlockUtils.distanceSquared(x, y, z, x+nx, y+ny, z+nz) <= radiusSquared){

						//rel.setReplaceType(ReplaceType.ALL);
						BlockUtils.setPersistentLeaves(data,nx+x,y+ny,nz+z);
						//data.setType(x+nx,y+ny,z+nz,leaf);
						//BlockUtils.setPersistentLeaves(data,x+nx, y+ny, z+nz);
					}
				}
			}
		}
		
	}
	
	public static void spawnCoconutTree(Random rand, PopulatorDataAbstract data, int x, int y, int z){
		
		int height = GenUtils.randInt(rand,6,8);
		int multiplier = 1;
		if(rand.nextBoolean()) multiplier = -1;
		int cx = -1;
		int cz = -1;
		data.setType(x,y,z,Material.JUNGLE_WOOD);
		
		//roots
		BlockUtils.setDownUntilSolid(x+1,y,z, data, Material.JUNGLE_WOOD);
		BlockUtils.setDownUntilSolid(x-1,y,z, data, Material.JUNGLE_WOOD);
		BlockUtils.setDownUntilSolid(x,y,z+1, data, Material.JUNGLE_WOOD);
		BlockUtils.setDownUntilSolid(x,y,z-1, data, Material.JUNGLE_WOOD);
		int nx = 0;
		int nz = 0;
		int state = rand.nextInt(2);
		int yCoord = -1;
		for(int ny = 1; ny< height; ny++){
			data.setType(nx+x,ny+y,nz+z,Material.JUNGLE_WOOD);
//			subject = b.getRelative(nx,ny,nz);
//			subject.setType(Material.JUNGLE_WOOD,false);
			if(state == 0){
				//x = root(y)
				//
				nx = (int) Math.round(Math.pow(ny,0.5));
				//if(Math.round(Math.pow(ny, 0.5)) > nx) nx++;
			}else if(state == 1){
				nz = (int) Math.round(Math.pow(ny,0.5));
				//if(Math.round(Math.pow(ny, 0.5)) > nz) nz++;
			}else{
				nx = (int) Math.round(Math.pow(ny,0.5));
				nz = (int) Math.round(Math.pow(ny,0.5));
				//if(Math.round(Math.pow(ny, 0.5)) > nx) nx++;
				//if(Math.round(Math.pow(ny, 0.5)) > nz) nz++;
			}
			yCoord = ny+y;
		}
		
		BlockUtils.setPersistentLeaves(data,nx+x,yCoord,nz+z);
		BlockUtils.setPersistentLeaves(data,nx+x,yCoord+1,nz+z);
		int crownLength = GenUtils.randInt(rand, 2,3);
		
		//Fill up more leaves
		BlockUtils.setPersistentLeaves(data,nx+x+1,yCoord,nz+z+1);
		BlockUtils.setPersistentLeaves(data,nx+x-1,yCoord,nz+z+1);
		BlockUtils.setPersistentLeaves(data,nx+x+1,yCoord,nz+z-1);
		BlockUtils.setPersistentLeaves(data,nx+x-1,yCoord,nz+z-1);
		
		if(height > 7){
			//Crown is longer, and taller
			crownLength += 1;
			BlockUtils.setPersistentLeaves(data,nx+x,yCoord+2,nz+z);
			BlockUtils.setPersistentLeaves(data,nx+x+1,yCoord+1,nz+z);
			BlockUtils.setPersistentLeaves(data,nx+x-1,yCoord+1,nz+z);
			BlockUtils.setPersistentLeaves(data,nx+x,yCoord+1,nz+z+1);
			BlockUtils.setPersistentLeaves(data,nx+x,yCoord+1,nz+z-1);
			
			//Side leaves dangle down
			for(int nnx = -2; nnx <= 2; nnx++){
				for(int nnz = -2; nnz <= 2; nnz++){
					if(Math.abs(nnx) == Math.abs(nnz)) continue;
					if(rand.nextInt(3) == 0) continue;
					dangleLeavesDown(rand,data,nx+x+nnx,yCoord,nz+z+nnz);
				}
			}
			
		}
		for(int m = 1; m <= crownLength; m++){
			BlockUtils.setPersistentLeaves(data,nx+x-1*m,yCoord,nz+z);
			BlockUtils.setPersistentLeaves(data,nx+x+1*m,yCoord,nz+z);
			BlockUtils.setPersistentLeaves(data,nx+x,yCoord,nz+z-1*m);
			BlockUtils.setPersistentLeaves(data,nx+x,yCoord,nz+z+1*m);
		}
		BlockUtils.setPersistentLeaves(data,nx+x-1*crownLength,yCoord-1,nz+z);
		BlockUtils.setPersistentLeaves(data,nx+x+1*crownLength,yCoord-1,nz+z);
		BlockUtils.setPersistentLeaves(data,nx+x,yCoord-1,nz+z-1*crownLength);
		BlockUtils.setPersistentLeaves(data,nx+x,yCoord-1,nz+z+1*crownLength);

		BlockUtils.setPersistentLeaves(data,nx+x-1*crownLength,yCoord-2,nz+z);
		BlockUtils.setPersistentLeaves(data,nx+x+1*crownLength,yCoord-2,nz+z);
		BlockUtils.setPersistentLeaves(data,nx+x,yCoord-2,nz+z-1*crownLength);
		BlockUtils.setPersistentLeaves(data,nx+x,yCoord-2,nz+z+1*crownLength);
	}
	
	private static void dangleLeavesDown(Random rand,PopulatorDataAbstract data, int x, int y, int z){
		for(int i = 0; i < GenUtils.randInt(1, 3); i++){
			if(i == 0 && rand.nextBoolean())
			BlockUtils.setPersistentLeaves(data,x,y-1,z);
		}
	}

}
