package org.terraform.structure;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.stronghold.*;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;
import org.terraform.utils.FastNoise.NoiseType;

public class StrongholdPopulator extends StructurePopulator{

	@Override
	public boolean canSpawn(Random rand,ArrayList<BiomeBank> biomes) {
		if(!biomes.contains(BiomeBank.SWAMP)) return false;
		return GenUtils.chance(rand,1,100);
	}

	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
		int seaLevel = TerraformGenerator.seaLevel;
		int x = data.getChunkX()*16 + random.nextInt(16);
		int z = data.getChunkZ()*16 + random.nextInt(16);
		int height = GenUtils.getHighestGround(data, x, z);
		//Strongholds start underground. Burrow down
		height -= 40;
		if(height < 3) height = 5;
		spawnStronghold(tw,random,data,x,height,z);
		
	}
	
	public void spawnStronghold(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z){
		int numRooms = 70;
		int range = 100;
		
		//Level One
		RoomLayoutGenerator gen = new RoomLayoutGenerator(tw.getRand(8),numRooms,x,y,z,range);
		gen.setPathPopulator(new StrongholdPathPopulator(tw.getRand(13)));
		gen.setRoomMaxX(30);
		gen.setRoomMaxZ(30);
		gen.setRoomMaxHeight(15);
		gen.forceAddRoom(25, 25, 15); //At least one room that can be the Portal room.
		gen.registerRoomPopulator(new PortalRoomPopulator(random, true, true));
		gen.registerRoomPopulator(new LibraryRoomPopulator(random, false, false));
		gen.registerRoomPopulator(new NetherPortalRoomPopulator(random, false, true));
		gen.registerRoomPopulator(new PrisonRoomPopulator(random, false, false));
		gen.registerRoomPopulator(new SilverfishDenPopulator(random, false, false));
		gen.registerRoomPopulator(new SupplyRoomPopulator(random, false, false));
		gen.registerRoomPopulator(new TrapChestRoomPopulator(random, false, false));
		gen.generate();
		gen.fill(data, tw, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
		
//		gen.reset();
//		
//		//Level Two
//		gen.setCentY(y+18);
//		gen.setRand(tw.getRand(9));
//		gen.setPathPopulator(new StrongholdPathPopulator(tw.getRand(14)));
//		gen.generate();
//		gen.fill(data, tw, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
//		
	}
	

	

}
