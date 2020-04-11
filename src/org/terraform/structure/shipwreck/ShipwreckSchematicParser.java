package org.terraform.structure.shipwreck;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

public class ShipwreckSchematicParser extends SchematicParser{
	
	private Random rand;
	private PopulatorDataAbstract pop;
	String woodType;
	private final static String[] woods = new String[]{
		"OAK",
		"ACACIA",
		"BIRCH",
		"SPRUCE",
		"DARK_OAK",
		"SPRUCE",
		"JUNGLE"
	};
	
	public ShipwreckSchematicParser(Random rand,
			PopulatorDataAbstract pop) {
		super();
		this.rand = rand;
		this.pop = pop;
		this.woodType = woods[rand.nextInt(woods.length)];
		
	}

	@Override
	public void applyData(Block block, BlockData data){
		
		//Water logging
		if(data instanceof Waterlogged){
			Waterlogged logged = (Waterlogged) data;
			if(block.getType() == Material.AIR){
				logged.setWaterlogged(false);
			}else{
				logged.setWaterlogged(true);
			}
		}
		
		//Mossy cobble
		if(data.getMaterial().toString().contains("COBBLESTONE")){
			data = Bukkit.createBlockData(
					data.getAsString().replaceAll(
						"cobblestone",
						GenUtils.randMaterial(rand, Material.COBBLESTONE,Material.COBBLESTONE,Material.COBBLESTONE,Material.MOSSY_COBBLESTONE)
						.toString().toLowerCase()
					)
			);
		}

		//Holes
		if(GenUtils.chance(rand, 1,30)){
			if(block.getY() <= TerraformGenerator.seaLevel)
				data = Bukkit.createBlockData(Material.WATER);
			else 
				data = Bukkit.createBlockData(Material.AIR);
			
			super.applyData(block, data);
			return;
		}
		
		if(data.getMaterial().toString().startsWith("OAK")||
				data.getMaterial().toString().startsWith("STRIPPED_OAK")){
			data = Bukkit.createBlockData(data.getAsString().replace("OAK", woodType));
		}
		
		
		if(data.getMaterial() == Material.CHEST){
			if(GenUtils.chance(rand, 4, 5)){
				if(block.getY() <= TerraformGenerator.seaLevel)
					data = Bukkit.createBlockData(Material.WATER);
				else 
					data = Bukkit.createBlockData(Material.AIR);
				
				super.applyData(block, data);
				return;
			}
			super.applyData(block, data);
			if(GenUtils.chance(rand, 1,5)){
				pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.SHIPWRECK_TREASURE);
			}else
				pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.SHIPWRECK_SUPPLY);
			return;
		}
		
		if(data.getMaterial().isBlock() && data.getMaterial().isSolid()){
			if(GenUtils.chance(rand,1,60)){ //Corals
				CoralGenerator.generateCoral(new PopulatorDataPostGen(block.getChunk()),
						block.getX(),
						block.getY(),
						block.getZ());
			}else if(GenUtils.chance(rand,1,40)){ //kelp n stuff
				CoralGenerator.generateKelpGrowth(new PopulatorDataPostGen(block.getChunk()),
						block.getX(),
						block.getY()+1,
						block.getZ());
			}
		}
		
		super.applyData(block, data);
	}
	
}