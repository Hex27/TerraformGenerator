package org.terraform.tree;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Leaves;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class TreeDB {
	
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
		
		setDistSevenLeaves(data,nx+x,yCoord,nz+z);
		setDistSevenLeaves(data,nx+x,yCoord+1,nz+z);
		int crownLength = GenUtils.randInt(rand, 2,3);
		
		//Fill up more leaves
		setDistSevenLeaves(data,nx+x+1,yCoord,nz+z+1);
		setDistSevenLeaves(data,nx+x-1,yCoord,nz+z+1);
		setDistSevenLeaves(data,nx+x+1,yCoord,nz+z-1);
		setDistSevenLeaves(data,nx+x-1,yCoord,nz+z-1);
		
		if(height > 7){
			//Crown is longer, and taller
			crownLength += 1;
			setDistSevenLeaves(data,nx+x,yCoord+2,nz+z);
			setDistSevenLeaves(data,nx+x+1,yCoord+1,nz+z);
			setDistSevenLeaves(data,nx+x-1,yCoord+1,nz+z);
			setDistSevenLeaves(data,nx+x,yCoord+1,nz+z+1);
			setDistSevenLeaves(data,nx+x,yCoord+1,nz+z-1);
			
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
			setDistSevenLeaves(data,nx+x-1*m,yCoord,nz+z);
			setDistSevenLeaves(data,nx+x+1*m,yCoord,nz+z);
			setDistSevenLeaves(data,nx+x,yCoord,nz+z-1*m);
			setDistSevenLeaves(data,nx+x,yCoord,nz+z+1*m);
		}
		setDistSevenLeaves(data,nx+x-1*crownLength,yCoord-1,nz+z);
		setDistSevenLeaves(data,nx+x+1*crownLength,yCoord-1,nz+z);
		setDistSevenLeaves(data,nx+x,yCoord-1,nz+z-1*crownLength);
		setDistSevenLeaves(data,nx+x,yCoord-1,nz+z+1*crownLength);

		setDistSevenLeaves(data,nx+x-1*crownLength,yCoord-2,nz+z);
		setDistSevenLeaves(data,nx+x+1*crownLength,yCoord-2,nz+z);
		setDistSevenLeaves(data,nx+x,yCoord-2,nz+z-1*crownLength);
		setDistSevenLeaves(data,nx+x,yCoord-2,nz+z+1*crownLength);
	}
	
	private static void dangleLeavesDown(Random rand,PopulatorDataAbstract data, int x, int y, int z){
		for(int i = 0; i < GenUtils.randInt(1, 3); i++){
			if(i == 0 && rand.nextBoolean())
			setDistSevenLeaves(data,x,y-1,z);
		}
	}
	
	private static void setDistSevenLeaves(PopulatorDataAbstract data, int x, int y, int z){
		Leaves leaves = (Leaves) Bukkit.createBlockData(Material.OAK_LEAVES);
		//leaves.setDistance(7);
		leaves.setPersistent(true);
		data.setBlockData(x, y, z, leaves);
	}
	

}
