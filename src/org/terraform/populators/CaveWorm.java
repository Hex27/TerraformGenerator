package org.terraform.populators;

import java.util.Random;
import java.util.UUID;

import org.bukkit.util.Vector;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class CaveWorm {
	
	SimpleBlock base;
	private UUID id;
	
	private FastNoise noise1;
	private FastNoise noise2;
	private FastNoise noise3;
	
	private Random rand;
	private int length;
	private PopulatorDataAbstract data;
	
	Vector direction;
	CaveLiquid liq;
	
	int surface = 50;
	
	boolean dead = false;
	
	int seed;
	
	public CaveWorm(TerraformWorld tw,PopulatorDataAbstract data, int x,int y, int z, int seed, int surface, CaveLiquid liq){
		this.liq = liq;
		this.surface = surface;
		this.id = UUID.randomUUID();
		this.seed = seed;
		this.base = new SimpleBlock(data,x,y,z);
		//TerraformGeneratorPlugin.logger.info("Generating cave at " + x + "," + y + "," + z +"," + this.id.toString());
		rand = new Random((long) (seed*0.3));
		length = GenUtils.randInt(rand,50,200);
		this.noise1 = new FastNoise(seed);
		noise1.SetFrequency(0.09f);
		noise1.SetNoiseType(NoiseType.Perlin);

		this.noise2 = new FastNoise(seed*3);
		noise2.SetFrequency(0.01f);
		noise2.SetNoiseType(NoiseType.Perlin);

		this.noise3 = new FastNoise(seed*4);
		noise3.SetFrequency(0.09f);
		noise3.SetNoiseType(NoiseType.Perlin);
		
		direction = Vector.getRandom();
		if(direction.lengthSquared() == 0) direction = new Vector(1,0,0);
		direction = direction.multiply(3/direction.length()); //Length 3.
	}
	
	public boolean hasNext(){
		return !dead;
	}
	
	private void die(){
		int x = base.getX();
		int y = base.getY();
		int z = base.getZ();
		//TerraformGeneratorPlugin.logger.info("Finishing cave at " + x + "," + y + "," + z +"," + this.id.toString());
		this.dead = true;
	}
	
	public void next(){
		//if(dormant) return;
		if(length <= 0){
			//TerraformGeneratorPlugin.logger.info("Death by length");
			die(); return;
		}
//		if(!BlockUtils.areAdjacentChunksLoaded(base.getChunk())){
//		//BlockUtils.loadSurroundingChunks(base.getChunk());
//			this.dormant = true;
//			return;
//		}
//		if(!replaceSphere(base)){
//			TerraformGeneratorPlugin.logger.info("Nothing more to replace");
//			die(); return;
//		}
		int trueRadius = 3;
		
		if(base.getY() <= 8){
			trueRadius-=2;
		}
		if(base.getY() <= 3) die();
		replaceSphere(this.seed, trueRadius, base);
		length--;
		base = base.getRelative(direction);
		double noise1 = this.noise1.GetNoise(base.getX(), base.getY(), base.getZ());
		double noise2 = this.noise2.GetNoise(base.getX(), base.getY(), base.getZ());
		double noise3 = this.noise3.GetNoise(base.getX(), base.getY(), base.getZ());
		double y = noise2;
		

		//Force surface caves down
		if(base.getY() >= surface-3){ 
			y = -Math.abs(y)*2;
			if(direction.getY() > 0) 
				direction.setY(0);
		}else // don't double-apply downwards pressure
		//Caves arc down if too high
		if(base.getY() >= surface-10){ 
			y = -Math.abs(y)*2;
			if(direction.getY() > 0) 
				direction.setY(direction.getY()/2);
		}
		
		//Caves arc up if too low
		if(base.getY() < 20){
			if(direction.getY() < 0) 
				direction.setY(direction.getY()/2);
			y = Math.abs(y);
		}
		
		direction = direction.add(new Vector(noise1*2, y, noise3*2));
		
		if(direction.length() == 0) direction = new Vector(1,0,0);
		direction = direction.multiply(3/direction.length()); //Length 3.
	}
	
	private void replaceSphere(int seed, float trueRadius, SimpleBlock block){
		FastNoise noise = new FastNoise(seed);
		noise.SetNoiseType(NoiseType.Simplex);
		noise.SetFrequency(0.09f);
		for(float x = -trueRadius; x <= trueRadius; x++){
			for(float y = -trueRadius; y <= trueRadius; y++){
				for(float z = -trueRadius; z <= trueRadius; z++){
					SimpleBlock rel = block.getRelative((int)Math.round(x),(int)Math.round(y),(int)Math.round(z));
					double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
					if(rel.distanceSquared(block) <= radiusSquared){
						//replaced = true;
//						rel.setReplaceType(ReplaceType.STONE_LIKE);
//						if(liq == CaveLiquid.AIR){
//							rel.setType(Material.CAVE_AIR);
//						}else if(liq == CaveLiquid.WATER){
//							rel.setReplaceType(ReplaceType.STONE_LIKE_ICELESS);
//						}else if(liq == CaveLiquid.LAVA){
//							rel.setType(Material.LAVA);
//						}
						//rel.attemptApply();
					}
				}
			}
		}
	}

}
