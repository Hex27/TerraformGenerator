package org.terraform.structure.stronghold;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class SupplyRoomPopulator extends RoomPopulatorAbstract{

	public SupplyRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
		super(rand, forceSpawn, unique);
	}

	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room) {
		int[] upperBounds = room.getUpperCorner();
		int[] lowerBounds = room.getLowerCorner();
		int y = room.getY();

		//Spawn torches
		for(int i = 0; i < GenUtils.randInt(rand, 1, 4); i++){
			int x = GenUtils.randInt(rand,lowerBounds[0]+1,upperBounds[0]-1);
			int z = GenUtils.randInt(rand,lowerBounds[1]+1,upperBounds[1]-1);
			int ny = y+1;
			while(data.getType(x, ny, z).isSolid() && ny < room.getHeight() + room.getY()){
				ny++;
			}
			if(ny == room.getHeight() + room.getY()) continue;
			
			data.setType(x, ny, z, Material.TORCH);
		}
		
		//Spawn piles of supply blocks.
		for(int i = 0; i < GenUtils.randInt(rand, 1, 3); i++){
			int x = GenUtils.randInt(rand,lowerBounds[0]+1,upperBounds[0]-1);
			int z = GenUtils.randInt(rand,lowerBounds[1]+1,upperBounds[1]-1);
			BlockUtils.replaceUpperSphere(rand.nextInt(992),
					GenUtils.randInt(rand, 2, 4), 
					GenUtils.randInt(rand, 2, 4), 
					GenUtils.randInt(rand, 2, 4), 
					new SimpleBlock(data, x,y,z), false,
					GenUtils.randMaterial(rand, 
							Material.TNT,
							Material.HAY_BLOCK,
							Material.BARREL,
							Material.OAK_LOG,
							Material.COAL_BLOCK));
		}
		
		//Spawn loot chests
		for(int i = 0; i < GenUtils.randInt(rand, 3, 10); i++){
			int x = GenUtils.randInt(rand,lowerBounds[0]+1,upperBounds[0]-1);
			int z = GenUtils.randInt(rand,lowerBounds[1]+1,upperBounds[1]-1);
			int ny = y+1;
			while(data.getType(x, ny, z).isSolid() && ny < room.getHeight() + room.getY()){
				ny++;
			}
			if(ny == room.getHeight() + room.getY()) continue;
			
			data.setType(x, ny, z, Material.CHEST);
			org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) Bukkit.createBlockData(Material.CHEST);
			chest.setFacing(BlockUtils.getDirectBlockFace(rand));
			data.setBlockData(x, ny, z, chest);
			data.lootTableChest(x, ny, z, TerraLootTable.STRONGHOLD_CROSSING);
		}
		
		
	}

	@Override
	public boolean canPopulate(CubeRoom room) {
		return !room.isBig();
	}
	
	

}
