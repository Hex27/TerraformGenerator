package org.terraform.tree;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;
import org.terraform.utils.FastNoise.NoiseType;

public class TreeDB {
	
	public static void spawnCoconutTree(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z){
		SimpleBlock base = new SimpleBlock(data,x,y,z);
		//Spawn the base
		for(BlockFace face:BlockUtils.directBlockFaces){
			new Wall(base.getRelative(face),BlockFace.NORTH)
			.downUntilSolid(new Random(), Material.JUNGLE_WOOD);
		}
		
		new FractalTreeBuilder(FractalTreeType.COCONUT_TOP).build(tw, data, x, y, z);
	}
	
	public static void spawnBigDarkOakTree(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z){
		new FractalTreeBuilder(FractalTreeType.DARK_OAK_BIG_TOP).build(tw, data, x, y, z);
		new FractalTreeBuilder(FractalTreeType.DARK_OAK_BIG_BOTTOM).build(tw, data, x, y-5, z);
	}
	
	public static void spawnMushroomCap(int seed, float r, SimpleBlock block, boolean hardReplace,Material... type){

		Random rand = new Random(seed);
		FastNoise noise = new FastNoise(seed);
		noise.SetNoiseType(NoiseType.Simplex);
		noise.SetFrequency(0.09f);
		for(float x = -r; x <= r; x++){
			for(float y = 0; y <= r; y++){
				for(float z = -r; z <= r; z++){
					
					SimpleBlock rel = block.getRelative((int)Math.round(x),(int)Math.round(y),(int)Math.round(z));
					//double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
					double equationResult = Math.pow(x,2)/Math.pow(r,2)
							+ Math.pow(y,2)/Math.pow(r,2)
							+ Math.pow(z,2)/Math.pow(r,2);
					if(equationResult <= 1+0.3*noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())
							&& equationResult >= 0.5){
					//if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
						if(hardReplace || !rel.getType().isSolid())
						rel.setType(GenUtils.randMaterial(rand,type));
						//rel.setReplaceType(ReplaceType.ALL);
					}
				}
			}
		}
	}
	
	public static void spawnGiantMushroom(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z, FractalTreeType type){
		FractalTreeBuilder builder = new FractalTreeBuilder(type);
		builder.build(tw, data, x, y, z);
		Material cap = Material.BROWN_MUSHROOM_BLOCK;
		SimpleBlock capBase = builder.top.getRelative(0,-9,0);
		int size = 15;
		if(type == FractalTreeType.RED_MUSHROOM_BASE){
			cap = Material.RED_MUSHROOM_BLOCK;
			capBase = builder.top.getRelative(0,-5,0);
			size = 10;
		}
		
		//int seed, float r, SimpleBlock block, boolean hardReplace,Material... type
		spawnMushroomCap(tw.getHashedRand(x,y,z).nextInt(94929297),size,
				capBase,true,cap);
	}
	
	

}
