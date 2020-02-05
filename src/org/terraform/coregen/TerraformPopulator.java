package org.terraform.coregen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.populators.CaveWormCreator;
import org.terraform.populators.OrePopulator;

public class TerraformPopulator{
    private static Set<SimpleChunkLocation> chunks = new HashSet<SimpleChunkLocation>();
    
	private ArrayList<OrePopulator> orePops = new ArrayList<OrePopulator>(){{
		add(new OrePopulator(Material.COAL_ORE, 70, 30, 50, 128, 131));
		add(new OrePopulator(Material.IRON_ORE, 50, 10, 30, 64, 67));
		add(new OrePopulator(Material.GOLD_ORE, 40, 10, 15, 29, 33));
		add(new OrePopulator(Material.DIAMOND_ORE, 40, 7, 5, 12, 15));
		add(new OrePopulator(Material.LAPIS_ORE, 40, 7, 15, 14, 23, 33));
		add(new OrePopulator(Material.REDSTONE_ORE, 40, 10, 15, 12, 15));
	}};
	

	
	private CaveWormCreator cavePop;
	
	public TerraformPopulator(TerraformWorld tw){
		this.cavePop = new CaveWormCreator(tw);
	}
	
	public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
//		SimpleChunkLocation loc = new SimpleChunkLocation(tw.getName(),data.getChunkX(),data.getChunkZ());
//		if(chunks.contains(loc)) return;
//		chunks.add(loc);
		//TerraformGeneratorPlugin.logger.info("Populator called.");
		//All delayed changes.
//		TChunk c = TerraformChunkManager.get().getOrCreate(chunk);
//		if(c != null){
//			//TerraformGeneratorPlugin.logger.info("Attempting to apply changes...");
//			c.applyChanges();
//		}
		
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
	
	

}
