package org.terraform.structure;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.farmhouse.FarmhouseRoomPopulator;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.stronghold.StrongholdPathPopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class FarmhousePopulator extends StructurePopulator{

	@Override
	public boolean canSpawn(Random rand,TerraformWorld tw, int chunkX, int chunkZ,ArrayList<BiomeBank> biomes) {
		if(biomes.contains(BiomeBank.DESERT)||
				biomes.contains(BiomeBank.DESERT_MOUNTAINS)||
				biomes.contains(BiomeBank.BADLANDS)||
				biomes.contains(BiomeBank.BADLANDS_MOUNTAINS)) return false;
		return GenUtils.chance(rand,1,100);
	}

	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
		int seaLevel = TerraformGenerator.seaLevel;
		int x = data.getChunkX()*16 + random.nextInt(16);
		int z = data.getChunkZ()*16 + random.nextInt(16);
		int height = GenUtils.getHighestGround(data, x, z);
		
	}
	
	public void spawnFarmHouse(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z){
		int numRooms = GenUtils.randInt(3,5);
		int range = 15;
		BiomeBank biome = tw.getBiomeBank(x, y, z);
		y--; //Floor replaces ground.
		RoomLayoutGenerator gen = new RoomLayoutGenerator(tw.getHashedRand(x, y, z),RoomLayout.OVERLAP_CONNECTED,numRooms,x,y,z,range);
		gen.setGenPaths(false);
		gen.setAllowOverlaps(true);
		//gen.setPathPopulator(new FarmhousePathPopulator(tw.getHashedRand(x, y, z, 2)));
		gen.setRoomMinX(5);
		gen.setRoomMinZ(5);
		gen.setRoomMinHeight(4);
		gen.setRoomMaxX(9);
		gen.setRoomMaxZ(7);
		gen.setRoomMaxHeight(4);
		gen.registerRoomPopulator(new FarmhouseRoomPopulator(random, false, false, biome));
		gen.generate(false);
		gen.fill(data, tw, Material.COBBLESTONE,Material.MOSSY_COBBLESTONE);
		//BlockUtils.getWoodForBiome(biome,"PLANKS")
	}
	
}
