package org.terraform.populators;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Temperature;

public class RiverWorm {
	
	SimpleBlock base;
	private UUID id;
	
	private FastNoise noise1;
	private FastNoise noise2;
	private FastNoise noise3;
	
	private Random rand;
	private int length;
	private PopulatorDataAbstract data;
	
	Vector direction;
	
	boolean dead = false;
	
	int seed;
	
	TerraformWorld tw;
	
	public RiverWorm(TerraformWorld tw,PopulatorDataAbstract data, int x, int z, int seed){
		this.tw = tw;
		this.id = UUID.randomUUID();
		rand = new Random((long) (seed*0.5));
		this.seed = seed;
		this.base = new SimpleBlock(data,x,GenUtils.getHighestGround(data, x, z),z);
		//TerraformGeneratorPlugin.logger.info("Generating cave at " + x + "," + y + "," + z +"," + this.id.toString());
		length = GenUtils.randInt(rand,100,300);
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
	
	private int depth = 6;
	
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
		int trueRadius = 5;
		
		if(base.getY() - depth > TerraformGenerator.seaLevel){
			die(); return;
		}
		replaceSphere(this.seed, trueRadius,depth,trueRadius, base,
				tw.getTemperature(base.getX(), base.getZ()) <= Temperature.SNOWY);
		length--;
		base = base.getRelative(direction);
		double noise1 = this.noise1.GetNoise(base.getX(), base.getY(), base.getZ());
		double noise2 = this.noise2.GetNoise(base.getX(), base.getY(), base.getZ());
		double noise3 = this.noise3.GetNoise(base.getX(), base.getY(), base.getZ());
		double y = HeightMap.getHeight(tw, base.getX(), base.getZ()) + GenUtils.randInt(rand, -12, -5);

		if(y <= TerraformGenerator.seaLevel) die();
		
		direction = direction.add(new Vector(noise1*2, 0, noise3*2));
		depth+= noise2*2;
		
		if(direction.length() == 0) direction = new Vector(1,0,0);
		direction = direction.multiply(3/direction.length()); //Length 3.
	}

	public static void replaceSphere(int seed, float rX, float rY, float rZ, SimpleBlock block, boolean ice){
		Random rand = new Random(seed);
		FastNoise noise = new FastNoise(seed);
		noise.SetNoiseType(NoiseType.Simplex);
		noise.SetFrequency(0.09f);
		for(float x = -rX; x <= rX; x++){
			for(float y = -rY; y <= rY; y++){
				for(float z = -rZ; z <= rZ; z++){
					
					SimpleBlock rel = block.getRelative((int)Math.round(x),(int)Math.round(y),(int)Math.round(z));
					//double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
					double equationResult = Math.pow(x,2)/Math.pow(rX,2)
							+ Math.pow(y,2)/Math.pow(rY,2)
							+ Math.pow(z,2)/Math.pow(rZ,2);
					if(equationResult <= 1+0.7*noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())){
					//if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
						if(rel.getY() > TerraformGenerator.seaLevel)
							rel.setType(GenUtils.randMaterial(rand,Material.AIR));
						else
							if(ice){
								rel.getRelative(0,1,0).lsetType(Material.ICE);
							}else{
								rel.getRelative(0,1,0).lsetType(Material.WATER);
							}
						
					}
				}
			}
		}
	}
	
}
