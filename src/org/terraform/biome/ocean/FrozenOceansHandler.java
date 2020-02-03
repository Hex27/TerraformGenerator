package org.terraform.biome.ocean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.CoralWallFan;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class FrozenOceansHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return true;
	}

	@Override
	public Biome getBiome() {
		return Biome.FROZEN_OCEAN;
	}

	@Override
	public Material[] getSurfaceCrust(Random rand) {
		return new Material[]{GenUtils.randMaterial(rand, Material.DIRT,Material.STONE,Material.COBBLESTONE,Material.STONE,Material.GRAVEL,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE,Material.STONE,Material.STONE,Material.GRAVEL,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE,Material.GRAVEL,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE)};
	}

	@Override
	public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {

		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getTrueHighestBlock(data, x, z);
				if(data.getBiome(x,y,z) != getBiome()) continue;
				
				//Full ice-sheets
				data.setType(x,TerraformGenerator.seaLevel, z, Material.ICE);
				
				if(!data.getType(x, y, z).isSolid()) continue;
				if(GenUtils.chance(random, 2, 100)){
					BlockUtils.generateClayDeposit(x,y,z,data,random);
				}
			}
		}
	}
	

}
