package org.terraform.populators;

import java.util.Random;

import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;
import org.terraform.utils.TickTimer;

public class OrePopulator {
	
	private Material type;
	private int baseChance;
	private int maxOreSize;
	private int maxNumberOfVeins;
	private int maxRange;
	private int minRange = 5;
	private int rareMaxRange;

	public OrePopulator(Material type, int baseChance, int maxOreSize,
			int maxNumberOfVeins, int maxRange, int rareMaxRange) {
		super();
		this.type = type;
		this.baseChance = baseChance;
		this.maxOreSize = maxOreSize;
		this.maxNumberOfVeins = maxNumberOfVeins;
		this.maxRange = maxRange;
		this.rareMaxRange = rareMaxRange;
	}
	
	public OrePopulator(Material type, int baseChance, int maxOreSize,
			int maxNumberOfVeins, int minRange, int maxRange, int rareMaxRange) {
		super();
		this.type = type;
		this.baseChance = baseChance;
		this.maxOreSize = maxOreSize;
		this.maxNumberOfVeins = maxNumberOfVeins;
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.rareMaxRange = rareMaxRange;
	}

	public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
		TickTimer timer = new TickTimer("Ore-generation");
		int x, y, z;
		for (int i = 0; i < this.maxNumberOfVeins; i++) {
			// Number of veins
			
		    if (GenUtils.chance(random,this.baseChance, 100)) { 
				// The chance of spawning a vein
				x = GenUtils.randInt(random,0, 15) + data.getChunkX()*16;
				z = GenUtils.randInt(random,0, 15) + data.getChunkZ()*16;
				
				int range = maxRange;
				if(GenUtils.chance(random,1,50)) range = rareMaxRange;
				
				y = GenUtils.randInt(random,minRange, range);  // Get randomized coordinates
				//Bukkit.getLogger().info("Generated ore at " + x +"," + y + "," + z);
				
				for(int s = maxOreSize; s > 0; s--){
					//Bukkit.getLogger().info("1");
					Material type = data.getType(x, y, z);
					if(type != Material.STONE) break;
					//Bukkit.getLogger().info("2");
					
					data.setType(x,y,z,this.type);
					
					switch (random.nextInt(5)) {  // The direction chooser
						case 0: x++; break;
						case 1: y++; break;
						case 2: z++; break;
						case 3: x--; break;
						case 4: y--; break;
						case 5: z--; break;
					}
				}
		    }
		}
		timer.finish();
	}


}
