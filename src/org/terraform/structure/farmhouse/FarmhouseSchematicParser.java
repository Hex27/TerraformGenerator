package org.terraform.structure.farmhouse;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class FarmhouseSchematicParser extends SchematicParser{
	
	private BiomeBank biome;
	private Random rand;
	private PopulatorDataAbstract pop;
	
	public FarmhouseSchematicParser(BiomeBank biome, Random rand,
			PopulatorDataAbstract pop) {
		super();
		this.biome = biome;
		this.rand = rand;
		this.pop = pop;
	}

	@Override
	public void applyData(Block block, BlockData data){
		if(data.getMaterial().toString().contains("COBBLESTONE")){
			data = Bukkit.createBlockData(
					data.getAsString().replaceAll(
						"cobblestone",
						GenUtils.randMaterial(rand, Material.COBBLESTONE,Material.COBBLESTONE,Material.COBBLESTONE,Material.MOSSY_COBBLESTONE)
						.toString().toLowerCase()
					)
			);
			super.applyData(block, data);
			return;
		}else if(data.getMaterial().toString().contains("OAK")){
			data = Bukkit.createBlockData(
					data.getAsString().replaceAll(
						data.getMaterial().toString().toLowerCase(),
						BlockUtils.getWoodForBiome(biome, 
								data.getMaterial().toString()
								.replaceAll("OAK_", "")
					).toString().toLowerCase())
			);
			super.applyData(block, data);
			return;
		}else if(data.getMaterial() == Material.CHEST){
			if(GenUtils.chance(rand, 1, 5)){
				block.setType(Material.AIR,true);
				return; //A fifth of chests are not placed.
			}
			super.applyData(block, data);
			pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.VILLAGE_PLAINS_HOUSE);
			return;
		}else{
			super.applyData(block, data);
		}
	}
	
}