package org.terraform.populators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;
import org.terraform.utils.FastNoise.NoiseType;

public class CaveWormCreator extends BlockPopulator {
		
	private FastNoise caveNoise;
	
	public CaveWormCreator(TerraformWorld tw){
		caveNoise = new FastNoise((int) (tw.getSeed()*0.3));
		caveNoise.SetNoiseType(NoiseType.Perlin);
		//caveNoise.SetFractalOctaves(2);
		caveNoise.SetFrequency(0.04f);
	}
	
	private static Collection<SimpleChunkLocation> processed = new ArrayList<>();

	@Override
	public void populate(World world, Random random, Chunk chunk) {
		SimpleChunkLocation sc = new SimpleChunkLocation(chunk);
		if(processed.contains(sc)) return;
		processed.add(sc);
		
		for(int i = 0; i < 2; i++){
			if(GenUtils.chance(9, 10)) continue;
			int x = GenUtils.randInt(random, 0, 15);
			int z = GenUtils.randInt(random, 0, 15);
			
			//TODO: Fix if you plan to use CaveWorm in future.
			int h = 30;//GenUtils.getHighestGround(chunk, x, z);
			
			int y = GenUtils.randInt(random,0,h);
			if(GenUtils.chance(random,1,10)) y = h;
			CaveLiquid liq = CaveLiquid.AIR;
			if(chunk.getBlock(x,y+1,z).getType() == Material.WATER){
				liq = CaveLiquid.WATER;
			}else if(chunk.getBlock(x,y+1,z).getType() == Material.LAVA){
				liq = CaveLiquid.LAVA;
			}
			
//			CaveWorm worm = new CaveWorm(chunk.getBlock(x,y,z), (int) world.getSeed(), h, liq);
//			while(worm.hasNext()){
//				worm.next();
//			}
		}
	}


}
