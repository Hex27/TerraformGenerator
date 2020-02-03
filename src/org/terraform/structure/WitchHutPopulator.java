package org.terraform.structure;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;
import org.terraform.utils.FastNoise.NoiseType;

public class WitchHutPopulator extends StructurePopulator{

	@Override
	public boolean canSpawn(Random rand,ArrayList<BiomeBank> biomes) {
		if(!biomes.contains(BiomeBank.SWAMP)) return false;
		return GenUtils.chance(rand,1,100);
	}

	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
		int seaLevel = TerraformGenerator.seaLevel;
		int x = data.getChunkX()*16 + random.nextInt(16);
		int z = data.getChunkZ()*16 + random.nextInt(16);
		int height = GenUtils.getHighestGround(data, x, z);
		if(height < seaLevel){ //Assume. it's on water
			height = seaLevel + GenUtils.randInt(random, 2, 3);
		}else 
			height += GenUtils.randInt(random, 2, 3);

		//Refers to center of hut, above the water.
		SimpleBlock core = new SimpleBlock(data, x,height,z);
		
		//Generate a hashtag shape, this will be the bottom framework of the hut.
		int length = GenUtils.randInt(random, 4, 5)*2;
		for(int rx = -length/2; rx <= length/2; rx++){
			core.getRelative(rx,0,length/4).setType(Material.OAK_LOG);
			Orientable dir = (Orientable) Bukkit.createBlockData(Material.OAK_LOG);
			dir.setAxis(Axis.X);
			core.getRelative(rx,0,length/4).setBlockData(dir);
			core.getRelative(rx,0,-length/4).setType(Material.OAK_LOG);
			core.getRelative(rx,0,-length/4).setBlockData(dir);
		}

		for(int rz = -length/2; rz <= length/2; rz++){
			core.getRelative(length/4,0,rz).setType(Material.OAK_LOG);
			Orientable dir = (Orientable) Bukkit.createBlockData(Material.OAK_LOG);
			dir.setAxis(Axis.Z);
			core.getRelative(length/4,0,rz).setBlockData(dir);
			core.getRelative(-length/4,0,rz).setType(Material.OAK_LOG);
			core.getRelative(-length/4,0,rz).setBlockData(dir);
		}
		
		//Floor
		setFlatRandomCircle(random,core,((float) length+1)/2, Material.OAK_SLAB,false);
		
		//Walls
		for(int rx = -2; rx <= 2; rx++){
			for(int rz = -2; rz <= 2; rz++){
				if(Math.abs(rx) <= 1 && Math.abs(rz) <= 1) continue;
				cobblePillar(random, core.getRelative(rx,1,rz),5);
			}
		}
		
		//Door
		switch(random.nextInt(4)){
			case(0):
				core.getRelative(2,1,0).setType(Material.AIR);
				core.getRelative(2,2,0).setType(Material.AIR);
				break;
			case(1):
				core.getRelative(-2,1,0).setType(Material.AIR);
				core.getRelative(-2,2,0).setType(Material.AIR);
				break;
			case(2):
				core.getRelative(0,1,2).setType(Material.AIR);
				core.getRelative(0,2,2).setType(Material.AIR);
				break;
			case(3):
				core.getRelative(0,1,-2).setType(Material.AIR);
				core.getRelative(0,2,-2).setType(Material.AIR);
				break;
		}
		
		//Windows
		for(int windows = 0; windows < GenUtils.randInt(random, 5,10); windows++){
			SimpleBlock window = core.getRelative(GenUtils.randInt(-2, 2),3,GenUtils.randInt(-2, 2));
			if(window.getType().toString().contains("COBBLE")){
				window.setType(Material.AIR);
				if(window.getRelative(0,-1,0).getType().isSolid() 
						&& GenUtils.chance(random,1,3)){
					window.setType(Material.POTTED_RED_MUSHROOM);
				}
			}
		}
		
		//Generate pillars to the floor, and the side fences.
		int[][] pillars = new int[][]{
			new int[]{2,2},
			new int[]{-2,-2},
			new int[]{-2,2},
			new int[]{2,-2}
		};
		for(int i = 0; i < 4; i++){
			SimpleBlock pillarTop = core.getRelative(pillars[i][0],0,pillars[i][1]);
			SimpleBlock fenceTop = pillarTop.getRelative(0,1,0);
			while(!BlockUtils.isStoneLike(pillarTop.getType())){
				pillarTop.setType(Material.OAK_LOG);
				pillarTop = pillarTop.getRelative(0,-1,0);
			}
			for(int h = 0; h < 4; h++){
				fenceTop.setType(Material.OAK_FENCE);
				fenceTop = fenceTop.getRelative(0,1,0);
			}
		}
		
		//Ceiling
		setFlatRandomCircle(random,core.getRelative(0,5,0),2.5f, Material.OAK_PLANKS,false);
		setFlatRandomCircle(random,core.getRelative(0,5,0),((float) length+3)/2, Material.OAK_SLAB,false);

		//Sharp top
		
		setFlatRandomCircle(random,core.getRelative(0,6,0),3, Material.OAK_PLANKS,false);
		setFlatRandomCircle(random,core.getRelative(0,7,0),3, Material.OAK_PLANKS,false);
		setFlatRandomCircle(random,core.getRelative(0,8,0),2, Material.OAK_PLANKS,false);
		setFlatRandomCircle(random,core.getRelative(0,9,0),2, Material.OAK_PLANKS,false);
		core.getRelative(0,10,0).setType(Material.OAK_STAIRS);
		
		//In the middle, there's a cauldron above a campfire
		core.getRelative(0,1,0).setType(Material.CAMPFIRE);
		core.getRelative(0,2,0).setType(Material.CAULDRON);
		Levelled cauld = (Levelled) Bukkit.createBlockData(Material.CAULDRON);
		cauld.setLevel(GenUtils.randInt(random, 1, cauld.getMaximumLevel()));
		core.getRelative(0,2,0).setBlockData(cauld);
		
		core.getRelative(0,3,0).setType(Material.IRON_BARS);
		core.getRelative(0,4,0).setType(Material.IRON_BARS);
		core.getRelative(0,5,0).setType(Material.IRON_BARS);
		
		//Make Vines
		for(int nx = -5; nx <= 5; nx++){
			for(int ny = -2; ny <= 8; ny++){
				for(int nz = -5; nz <= 5; nz++){
					SimpleBlock vineBase = core.getRelative(nx,ny,nz);
					if(vineBase.getType().isSolid() &&
							vineBase.getType() != Material.OAK_SLAB &&
							vineBase.getType() != Material.OAK_FENCE){
						if(GenUtils.chance(random,5,100)){
							BlockUtils.vineUp(vineBase, 3);
						}
					}
						
				}
			}
		}
		
		//Spawn witch and cat
		int mx = core.getX();
		int my = core.getY() + 1;
		int mz = core.getZ() + 1;
		data.addEntity(mx, my, mz, EntityType.WITCH);
		data.addEntity(mx, mz, mz, EntityType.CAT);
	}
	
	private void cobblePillar(Random random, SimpleBlock base, int height){
		for(int i = 0; i < height; i++){
			base.getRelative(0,i,0).setType(GenUtils.randMaterial(random, Material.COBBLESTONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE));
		}
	}
	
	private void setFlatRandomCircle(Random random, SimpleBlock center, float radius, Material mat, boolean force){

		FastNoise noise = new FastNoise(random.nextInt(992));
		noise.SetNoiseType(NoiseType.Simplex);
		noise.SetFrequency(0.09f);
		for(float rx = -radius; rx <= radius; rx++){
			for(float rz = -radius; rz <= radius; rz++){
				double equation = Math.pow(rx, 2) + Math.pow(rz,2);
				SimpleBlock rel = center.getRelative((int)Math.round(rx),0,(int)Math.round(rz));
				if(equation <= Math.pow(radius, 2) + noise.GetNoise(rel.getX(), rel.getZ())){
					if(force || !rel.getType().isSolid()){
						rel.setType(mat);
						if(mat.toString().contains("SLAB")){
							Slab slab = (Slab) Bukkit.createBlockData(mat);
							slab.setType(Type.TOP);
							rel.setBlockData(slab);
						}
					}
				}
			}
		}
	}
	
	

}
