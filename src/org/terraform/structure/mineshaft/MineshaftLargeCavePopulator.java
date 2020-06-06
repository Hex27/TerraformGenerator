package org.terraform.structure.mineshaft;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.util.Vector;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.caves.GenericLargeCavePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;
import org.terraform.utils.FastNoise.NoiseType;

public class MineshaftLargeCavePopulator extends GenericLargeCavePopulator{
	
	public void createLargeCave(TerraformWorld tw, Random rand, PopulatorDataAbstract data, int rY, int x, int y, int z){
		TerraformGeneratorPlugin.logger.info("Generating Mineshaft Cave at " + x + "," + y + "," +z);
		int rX = GenUtils.randInt(rand, 30, 50);
		int rZ = GenUtils.randInt(rand, 30, 50);
		
		//Create main cave hole
		int waterLevel = carveCaveSphere(rand.nextInt(876283),rX,rY,rZ,new SimpleBlock(data,x,y,z));
	
		//Decrease radius to only spawn spikes away from corners
		rX -= 10;
		rZ -= 10;
		
		int pathX = x;
		int pathZ = z;
		int pathY = GenUtils.randInt(rand,y-5,y-3);
		
		for(int i = 0; i < GenUtils.randInt(rand,1,3);i++)
			setPrimaryWoodenPathway(
					BlockUtils.getDirectBlockFace(rand)
					,data,pathX,pathY,pathZ
					);
		
		for(int nx = x - rX; nx <= x + rX; nx++){
			for(int nz = z - rZ; nz <= z + rZ; nz++){
				
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
				
				//Chest Platforms
				if(data.getType(nx, waterLevel, nz) == Material.WATER){
					int width = GenUtils.randInt(rand,2,3);
					int nny = waterLevel;
					for(int nnx = -width; nnx <= width; nnx++)
						for(int nnz = -width; nnz <= width; nnz++){
							if(Math.abs(nnx) == width || Math.abs(nnz) == width)
								data.setType(nx + nnx,nny,nz + nnz,Material.OAK_LOG);
							else
								data.setType(nx + nnx,nny,nz + nnz,Material.OAK_PLANKS);
							
							if(Math.abs(nnx) == width && Math.abs(nnz) == width){
								data.setType(nx + nnx,nny+1,nz+nnz,Material.TORCH);
							}
							
						}
					
					data.setType(nx, nny+1, nz, Material.CHEST);
					data.lootTableChest(nx, nny+1, nz, TerraLootTable.ABANDONED_MINESHAFT);
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
		TerraformGeneratorPlugin.logger.info("Finished generating mineshaft cave at " + x + "," + y + "," +z);
		
	}
	
	public static void setPrimaryWoodenPathway(BlockFace axis, PopulatorDataAbstract data, int x, int y, int z){
		SimpleBlock block = new SimpleBlock(data,x,y,z);
		Wall w = new Wall(block.untilSolid(axis.getOppositeFace()),axis);
		Slab slab = (Slab) Bukkit.createBlockData(Material.OAK_SLAB);
		slab.setType(Type.TOP);
		while(true){
			w = w.getFront();
			int placed = 1;
			int depth = 0;
			w.setType(Material.OAK_LOG);
			w.getRelative(0, -1, 0).downUntilSolid(new Random(), Material.OAK_LOG);
			
			boolean canMoveForward = true;
			while(placed < 2 && depth < 5){
				depth++;
				Wall target = w.getLeft(depth);
				if(!target.getType().isSolid()){
					target.setBlockData(slab);
					placed++;
				}
			}
			if(depth >= 5) canMoveForward = false;

			if(canMoveForward){
				placed = 1;
				depth = 0;
				while(placed < 2 && depth < 5){
					depth++;
					Wall target = w.getRight(depth);
					if(!target.getType().isSolid()){
						target.setBlockData(slab);
						placed++;
					}
				}
				if(depth >= 5) canMoveForward = false;
			}
			
			if(!canMoveForward) break;
		}
	}
	
}
