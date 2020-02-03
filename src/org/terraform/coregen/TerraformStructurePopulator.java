package org.terraform.coregen;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.StrongholdPopulator;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.WitchHutPopulator;

public class TerraformStructurePopulator extends BlockPopulator {
	
	private ArrayList<StructurePopulator> structurePops = new ArrayList<StructurePopulator>(){{
		//add(new WitchHutPopulator());
	}};
	
	TerraformWorld tw;
	NMSInjectorAbstract inj;
	public TerraformStructurePopulator(TerraformWorld tw,NMSInjectorAbstract inj){
		this.tw = tw;
		this.inj = inj;
	}
	
	@Override
	public void populate(World world, Random random, Chunk chunk) {
		
		PopulatorDataPostGen data = new PopulatorDataPostGen(chunk);
		//PopulatorDataAbstract data = TerraformGeneratorPlugin.injector.getICAData(chunk);
		
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
		
		for(StructurePopulator spop:structurePops){
			if(spop.canSpawn(random,banks))
				spop.populate(tw, random, data);
		}
		
		//TODO: Remove after test.
		if(data.getChunkX() == 20 && data.getChunkZ() == 20){
			new StrongholdPopulator().populate(tw, random, data);
		}
	}

}
