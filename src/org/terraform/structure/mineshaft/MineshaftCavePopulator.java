package org.terraform.structure.mineshaft;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.Chest;
import org.terraform.biome.cave.AbstractCavePopulator;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataICAAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.stronghold.StrongholdPathPopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class MineshaftCavePopulator extends AbstractCavePopulator {

	private static boolean genned = false;
	
	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
//				if(!(tw.getBiomeBank(x, GenUtils.getHighestGround(data,x,z), z).getCavePop() 
//						instanceof MineshaftCavePopulator))
//					continue;
				for(int[] pair:GenUtils.getCaveCeilFloors(data, x, z)){
					BlockFace dir = null;
					int ceil = pair[0]; //non-solid
					int floor = pair[1]; //solid
					
					if(!genned){
						genned = true;
						TerraformGeneratorPlugin.logger.info("Spawning mineshaft cave at " + x + "," + floor + "," + z);
					}
					int caveHeight = ceil-floor;
					if(caveHeight <= 3) continue;
					
					//Don't touch slabbed floors or stalagmites
					if(data.getType(x, floor, z).toString().endsWith("SLAB")||
							data.getType(x, floor, z).toString().endsWith("WALL"))
						continue;

					//=========================
					//Upper decorations
					//=========================
					
					//Wooden Plank supports
					if(x%3 == 0 && z %3 == 0){
						//no present supports
						if(!data.getType(x, ceil, z).toString().contains("OAK")){
							//Find cave dir
							dir = getCaveDirection(dir,data,ceil,floor,x,z);
							if(dir != null) {
								Wall w = new Wall(new SimpleBlock(data,x,ceil,z),dir);
								w.setType(Material.OAK_FENCE);
								for(int i = 5; i < 5; i++){
									Wall adj = w.getLeft(i);
									//too high
									if(BlockUtils.isStoneLike(adj.getType())){
										while(adj.get().getY() > floor && BlockUtils.isStoneLike(adj.getType())){
											adj = adj.getRelative(0,-1,0);
										}
										//too low. You can go slightly higher than the normal ceiling
									}else if(!BlockUtils.isStoneLike(adj.getRelative(0,1,0).getType())){
										while(adj.get().getY() < ceil+3 && !BlockUtils.isStoneLike(adj.getRelative(0,1,0).getType())){
											adj = adj.getRelative(0,1,0);
										}
									}
									
									//If within range, this is a good spot.
									if(adj.get().getY() > floor && adj.get().getY() < ceil+3){
										adj.setType(Material.OAK_FENCE);
									}else{ //if not, this is a "wall"
										adj = adj.getRight();
										adj.downRPillar(random, ceil-floor+3, Material.OAK_FENCE,Material.OAK_PLANKS);
										break;
									}
								}
							}
						}
					}
					
					//=========================
					//Lower decorations 
					//=========================
					
					//Slabbing
					if(GenUtils.chance(random,1,25)){
						SimpleBlock base = new SimpleBlock(data,x,floor+1,z);
						if(BlockUtils.isStoneLike(base.getRelative(0,-1,0).getType()))
						//Only next to spots where there's some kind of solid block.
							if(base.getType() == Material.CAVE_AIR)
								for(BlockFace face:BlockUtils.directBlockFaces){
									if(base.getRelative(face).getType().isSolid()){
										if(random.nextBoolean())
											base.setType(Material.OAK_SLAB);
										else
											base.setType(Material.COBBLESTONE_SLAB);
										break;
									}
								}
					}else if(GenUtils.chance(random,1,150)){ //Chest
							SimpleBlock base = new SimpleBlock(data,x,floor+1,z);
							if(BlockUtils.isStoneLike(base.getRelative(0,-1,0).getType()))
							//Only next to spots where there's some kind of solid block.
								if(base.getType() == Material.CAVE_AIR)
									for(BlockFace face:BlockUtils.directBlockFaces){
										if(base.getRelative(face).getType().isSolid()){
											Chest chest = (Chest) Bukkit.createBlockData(Material.CHEST);
											chest.setFacing(face.getOppositeFace());
											base.setBlockData(chest);
											data.lootTableChest(base.getX(), base.getY(), base.getZ(), TerraLootTable.ABANDONED_MINESHAFT);
										}
									}
					}else if(GenUtils.chance(random,1,30)){ //Rails
						SimpleBlock base = new SimpleBlock(data,x,floor+1,z);
						if(base.getRelative(0,-1,0).getType().isSolid())
						//Only at spots with NO solid blocks nearby
							if(base.getType() == Material.CAVE_AIR){
								boolean spawn = true;
								for(BlockFace face:BlockUtils.directBlockFaces){
									if(base.getRelative(face).getType().isSolid()){
										spawn = false;
										break;
									}
								}
								if(spawn){
									Rail rail = (Rail) Bukkit.createBlockData(Material.RAIL);
									
									base.setBlockData(rail);
									BlockUtils.correctSurroundingRails(base);
//									if(GenUtils.chance(random, 1, 100)){
//										PopulatorDataICAAbstract ica = TerraformGeneratorPlugin.injector.getICAData(data);
//										ica.spawnMinecartWithChest(base.getX(), base.getY(), base.getZ(), TerraLootTable.ABANDONED_MINESHAFT, random);
//									}
								}
							}
					}
				}
			}
		}
	}
	
	public BlockFace getCaveDirection(BlockFace original, PopulatorDataAbstract data, int ceil, int floor, int x, int z){
		if(original != null) return original;
		int cutoff = 20;
		
		int y = floor + ((ceil-floor)/2);
		HashMap<BlockFace,Integer> directions = new HashMap<>();
		BlockFace dir = null;
		for(BlockFace face: BlockUtils.directBlockFaces){
			SimpleBlock block = new SimpleBlock(data,x,y,z);
			int dist = 0;
			while(BlockUtils.isStoneLike(block.getType()) && dist < cutoff){
				block = block.getRelative(face);
				dist++;
				cutoff++;
			}
			directions.put(face,dist);
			
			if(dir == null) 
				dir = face;
			else if(directions.get(dir) < dist){
				dir = face;
			}
		}
		int highest = directions.get(dir);
		for(BlockFace face:directions.keySet()){
			if(face == dir || face == dir.getOppositeFace()) continue;
			if(directions.get(face) == highest) 
				return null; //multiple axis
		}
		return dir;
	}
}
