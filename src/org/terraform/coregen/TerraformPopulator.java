package org.terraform.coregen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.populators.CaveWormCreator;
import org.terraform.populators.OrePopulator;
import org.terraform.populators.RiverWormCreator;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.Temperature;

public class TerraformPopulator{
    private static Set<SimpleChunkLocation> chunks = new HashSet<SimpleChunkLocation>();
    
    private RiverWormCreator rwc;
    
	private ArrayList<OrePopulator> orePops = new ArrayList<OrePopulator>(){{
		
		//Ores
		add(new OrePopulator(Material.COAL_ORE, 70, 30, 50, 128, 131));
		add(new OrePopulator(Material.IRON_ORE, 50, 10, 30, 64, 67));
		add(new OrePopulator(Material.GOLD_ORE, 40, 10, 15, 29, 33));
		add(new OrePopulator(Material.DIAMOND_ORE, 40, 7, 5, 12, 15));
		add(new OrePopulator(Material.LAPIS_ORE, 40, 7, 15, 14, 23, 33));
		add(new OrePopulator(Material.REDSTONE_ORE, 40, 10, 15, 12, 15));
		
		//Non-ores
		add(new OrePopulator(Material.GRAVEL, 70, 33, 8, 255, 255));
		add(new OrePopulator(Material.ANDESITE, 70, 33, 8, 80, 80));
		add(new OrePopulator(Material.DIORITE, 70, 33, 8, 80, 80));
		add(new OrePopulator(Material.GRANITE, 70, 33, 8, 80, 80));
	}};
	

	
	private CaveWormCreator cavePop;
	
	public TerraformPopulator(TerraformWorld tw){
		this.cavePop = new CaveWormCreator(tw);
		this.rwc = new RiverWormCreator(tw);
	}
	
	public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

//		//Rivers
//		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
//			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
//				if(tw.getRiverDepth(x, z) != 0){
//					int y = GenUtils.getHighestGround(data, x, z);//new HeightMap().getHeight(tw, x, z);
//					if(y <= TerraformGenerator.seaLevel) continue;
//					SimpleBlock base = new SimpleBlock(data,x,y,z);
//					for(int i = 0; i <= tw.getRiverDepth(x,z); i++){
//						SimpleBlock sb = base.getRelative(0,-i,0);
//						sb.setType(Material.WATER);
//						
//						//River bed
//						if(tw.getRiverDepth(x,z) == i){
//							sb.setType(GenUtils.randMaterial(Material.SAND,Material.SAND,Material.SAND,Material.GRAVEL,Material.DIRT,Material.DIRT));
//						}
//					}
////					riverSphere((int) (tw.getSeed()*7),6,6,GenUtils.randInt(tw.getHashedRand(x, 123456, z),6,10),base,
////							tw.getTemperature(x, z) <= Temperature.SNOWY);
//				}
//			}
//		}
		
		//rwc.populate(tw,random,data);
		
		
		//ores & caves
		for(OrePopulator ore:orePops){
			//TerraformGeneratorPlugin.logger.info("Generating ores...");
			ore.populate(tw, random, data);
		}
		//cavePop.populate(tw, random, data);
		

		//Biome specific populators
		ArrayList<BiomeBank> banks = new ArrayList<>();
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int height = new HeightMap().getHeight(tw, x, z);//GenUtils.getTrueHighestBlock(data, x, z);
				for(BiomeBank bank:BiomeBank.values()){
					BiomeBank currentBiome = tw.getBiomeBank(x, height, z);//BiomeBank.calculateBiome(tw,tw.getTemperature(x, z), height);
					
					if(bank == currentBiome){
						if(!banks.contains(bank))
							banks.add(bank);
						break;
					}
				}
			}
		}
		
		for(BiomeBank bank:banks){
			//TerraformGeneratorPlugin.logger.info("Populating for biome: " + bank.toString());
			bank.getHandler().populate(tw, random, data);
		}
		
		
		
	}
//	
//	public static void riverSphere(int seed, float rX, float rY, float rZ, SimpleBlock block, boolean ice){
//		Random rand = new Random(seed);
//		FastNoise noise = new FastNoise(seed);
//		noise.SetNoiseType(NoiseType.Simplex);
//		noise.SetFrequency(0.09f);
//		for(float x = -rX; x <= rX; x++){
//			for(float y = -rY; y <= rY; y++){
//				for(float z = -rZ; z <= rZ; z++){
//					
//					SimpleBlock rel = block.getRelative((int)Math.round(x),(int)Math.round(y),(int)Math.round(z));
//					//double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
//					double equationResult = Math.pow(x,2)/Math.pow(rX,2)
//							+ Math.pow(y,2)/Math.pow(rY,2)
//							+ Math.pow(z,2)/Math.pow(rZ,2);
//					if(equationResult <= 1+0.7*noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())){
//					//if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
//						if(rel.getY() > TerraformGenerator.seaLevel)
//							rel.setType(Material.AIR);
//						else
//							if(ice){
//								rel.setType(Material.ICE);
//							}else{
//								rel.setType(Material.WATER);
//							}
//						
//					}
//				}
//			}
//		}
//	}
	
	

}
