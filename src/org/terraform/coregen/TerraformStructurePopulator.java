package org.terraform.coregen;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.*;

public class TerraformStructurePopulator extends BlockPopulator {
	
	private ArrayList<StructurePopulator> structurePops = new ArrayList<StructurePopulator>(){{
		add(new StrongholdPopulator());
	}};
	
	TerraformWorld tw;
	NMSInjectorAbstract inj;
	public TerraformStructurePopulator(TerraformWorld tw,NMSInjectorAbstract inj){
		this.tw = tw;
		this.inj = inj;
	}
	
	@Override
	public void populate(World world, Random random, Chunk chunk) {
		//Don't attempt generation pre-injection.
		if(!TerraformGeneratorPlugin.injectedWorlds.contains(world.getName())) 
			return;
		PopulatorDataPostGen data = new PopulatorDataPostGen(chunk);
		//PopulatorDataAbstract data = TerraformGeneratorPlugin.injector.getICAData(chunk);
		//TerraformGeneratorPlugin.logger.debug("s-pop-1");
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
		//TerraformGeneratorPlugin.logger.debug("s-pop-2");
		for(StructurePopulator spop:structurePops){
			//TerraformGeneratorPlugin.logger.debug("s-pop-3");
			if(spop.canSpawn(random,tw,data.getChunkX(),data.getChunkZ(),banks)){
				//TerraformGeneratorPlugin.logger.debug("s-pop-4");
				spop.populate(tw, random, data);
			}
		}
		
	}

}
