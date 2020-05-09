package org.terraform.carving;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class CaveWormCreator extends Carver {
	private static Collection<SimpleChunkLocation> processed = new ArrayList<>();

	@Override
	public void carve(TerraformWorld tw, PopulatorDataAbstract data, Random random) {
		
			if(GenUtils.chance(9, 10))  //10% of chunks have a cave originating from them
				return;
			
			int x = data.getChunkX()*16 + GenUtils.randInt(random, 0, 15);
			int z = data.getChunkZ()*16 + GenUtils.randInt(random, 0, 15);
			
			//TODO: Fix if you plan to use CaveWorm in future.
			int h = 30;//GenUtils.getHighestGround(chunk, x, z);
			
			int y = GenUtils.randInt(random,0,h);
			if(GenUtils.chance(random,1,10)) y = h;
			CaveLiquid liq = CaveLiquid.AIR;
			if(data.getType(x,y+1,z) == Material.WATER){
				liq = CaveLiquid.WATER;
			}else if(data.getType(x,y+1,z) == Material.LAVA){
				liq = CaveLiquid.LAVA;
			}
			//TerraformWorld tw,PopulatorDataAbstract data, int x,int y, int z, int seed, int surface, CaveLiquid liq
			CaveWorm worm = new CaveWorm(tw,data,x,y,z, random.nextInt(9296572), h, liq);
			//CaveWorm worm = new CaveWorm(data, (int) world.getSeed(), h, liq);
			while(worm.hasNext()){
				try{
					worm.next();
				}catch(RuntimeException e){
					worm.die();
					break;
				}
			}
	}


}
