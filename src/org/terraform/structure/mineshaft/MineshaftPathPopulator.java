package org.terraform.structure.mineshaft;

import java.util.Random;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.PopulatorDataICAAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class MineshaftPathPopulator extends PathPopulatorAbstract{
	
	Random rand;
	
	public MineshaftPathPopulator(Random rand) {
		super();
		this.rand = rand;
	}

	@Override
	public void populate(PathPopulatorData ppd) {
		Wall core = new Wall(ppd.base,ppd.dir);
		
		//Was populated before.
		if(core.getType() != Material.CAVE_AIR)
			return;
		
		Wall ceiling = core.findCeiling(10);
		if(ceiling != null){
			ceiling = ceiling.getRelative(0,-1,0);
		}
		Wall left = core.findLeft(10);
		Wall right = core.findRight(10);
		
		Material[] pathMat = new Material[]{
			Material.OAK_PLANKS,
			Material.OAK_SLAB,
			Material.OAK_PLANKS,
			Material.OAK_SLAB,
			Material.GRAVEL
		};
		
		//Central Pathway
		core.setType(GenUtils.randMaterial(
				Material.COBBLESTONE,
				Material.COBBLESTONE,
				Material.COBBLESTONE,
				Material.ANDESITE,
				Material.DIORITE,
				Material.MOSSY_COBBLESTONE));
		core.getRight().setType(GenUtils.randMaterial(pathMat));
		core.getLeft().setType(GenUtils.randMaterial(pathMat));
		 
		//Pillars supporting the mineshaft area
		if(GenUtils.chance(rand,1,5)){
			core.getRelative(0,-1,0).getRight().downUntilSolid(rand, Material.OAK_FENCE);
			core.getRelative(0,-1,0).getLeft().downUntilSolid(rand, Material.OAK_FENCE);
		}
		
		//Broken feel
		if(rand.nextBoolean()) core.getRight(2)
			.setType(GenUtils.randMaterial(pathMat));
		if(rand.nextBoolean()) core.getLeft(2)
			.setType(GenUtils.randMaterial(pathMat));
		
		Directional pebble = (Directional) Material.STONE_BUTTON.createBlockData("[face=floor]");
		//Decorate with pebbles n shit lol
		for(int i = -2; i <= 2; i++){
			if(i == 0) continue;
			Wall target = core.getLeft(i);
			if(!target.getType().toString().contains("SLAB") 
					&& target.getType().isSolid()
					&& target.getType() != Material.GRAVEL
					&& target.getRelative(0,1,0).getType() == Material.CAVE_AIR){
				if(GenUtils.chance(1,10)){ //Pebble
					pebble.setFacing(BlockUtils.getDirectBlockFace(rand));
					target.getRelative(0,1,0).setBlockData(pebble);
				}else if(GenUtils.chance(1,10)){ //Mushroom
					target.getRelative(0,1,0).setType(GenUtils.randMaterial(Material.BROWN_MUSHROOM,Material.RED_MUSHROOM));
				}
			}
		}
		
		//Rails
		if(core.getType().isSolid() && rand.nextBoolean()){
			Rail rail = (Rail) Bukkit.createBlockData(Material.RAIL);
			switch(ppd.dir){
			case NORTH:
				rail.setShape(Shape.NORTH_SOUTH);
				break;
			case SOUTH:
				rail.setShape(Shape.NORTH_SOUTH);
				break;
			case EAST:
				rail.setShape(Shape.EAST_WEST);
				break;
			case WEST:
				rail.setShape(Shape.EAST_WEST);
				break;
			default:
				break;
			}
			core.getRelative(0,1,0).setBlockData(rail);
			BlockUtils.correctSurroundingRails(core.getRelative(0,1,0).get());
			if(GenUtils.chance(rand,1,100)){
				TerraformGeneratorPlugin.logger.info("Minecart with chest at: " + core.getX() + "," + core.getY()+"," + core.getZ());
				PopulatorDataICAAbstract ica = TerraformGeneratorPlugin.injector.getICAData(((PopulatorDataPostGen) core.get().getPopData()).getChunk());
				ica.spawnMinecartWithChest(
						core.getX(), core.getY()+1, core.getZ(),
						TerraLootTable.ABANDONED_MINESHAFT, rand
						);
			}
		}
		
		boolean hasSupports = setMineshaftSupport(left,right, ceiling);
		
		if(hasSupports) return;
		
		//Now for the stuff that we've put in normal caves
		for(int i = -2; i <= 2; i++){
			Wall ceil = core.getLeft(i).findCeiling(10);
			Wall floor = core.getLeft(i).findFloor(10);
			
			//Decorations on the wall
			if(ceil != null && floor != null)
				for(int ny = 0; ny <= ceil.getY()-floor.getY(); ny++){
					Wall[] walls = new Wall[]{
							floor.getRelative(0,ny,0).findLeft(10),
							floor.getRelative(0,ny,0).findRight(10)
					};
					for(Wall target:walls){
						if(target != null){
							if(target.getType() == Material.STONE){
								if(GenUtils.chance(1,10)){
									target.setType(GenUtils.randMaterial(
											Material.COBBLESTONE,
											Material.MOSSY_COBBLESTONE
										));
								}
								if(GenUtils.chance(1, 10)){
									BlockUtils.vineUp(target.get(), 2);
								}
							}
						}
					}
				}
			
			//Vertical decorations
			if(ceil != null && !ceil.getType().toString().contains("SLAB") && !ceil.getType().toString().contains("LOG")){
				ceil = ceil.getRelative(0,-1,0);
				if(GenUtils.chance(rand,1,10)){
					//Stalactites
					boolean canSpawn = true;
					for(BlockFace face:BlockUtils.directBlockFaces){
						if(ceil.getRelative(face).getType().toString().contains("WALL")){
							canSpawn = false;
							break;
						}
					}
					if(canSpawn)
						ceil.downLPillar(rand, GenUtils.randInt(rand,1,3),
								Material.COBBLESTONE_WALL,
								Material.MOSSY_COBBLESTONE_WALL
								);
					
				}else if(GenUtils.chance(rand,1,6)){
					//Cobweb
					ceil.setType(Material.COBWEB);
				}else if(GenUtils.chance(rand,1,10)){
					//Slabbing
					Slab slab = (Slab) Bukkit.createBlockData(GenUtils.randMaterial(Material.COBBLESTONE_SLAB,Material.STONE_SLAB,Material.MOSSY_COBBLESTONE_SLAB));
					slab.setType(Type.TOP);
					ceil.setBlockData(slab);
				}
			}
			if(floor != null && !floor.getType().toString().contains("SLAB") && !floor.getType().toString().contains("LOG")){
				floor = floor.getRelative(0,1,0);
				if(GenUtils.chance(rand,1,10)){
					//Stalagmites
					boolean canSpawn = true;
					for(BlockFace face:BlockUtils.directBlockFaces){
						if(floor.getRelative(face).getType().toString().contains("WALL")){
							canSpawn = false;
							break;
						}
					}
					if(canSpawn)
						floor.LPillar(GenUtils.randInt(rand,1,3), false, rand,
								Material.COBBLESTONE_WALL,
								Material.MOSSY_COBBLESTONE_WALL
								);
					
				}else if(GenUtils.chance(rand,1,10)){
					//Slabbing
					for(BlockFace face:BlockUtils.directBlockFaces){
						if(floor.getRelative(face).getType().isSolid()){
							Slab slab = (Slab) Bukkit.createBlockData(GenUtils.randMaterial(Material.COBBLESTONE_SLAB,Material.STONE_SLAB,Material.MOSSY_COBBLESTONE_SLAB));
							slab.setType(Type.BOTTOM);
							floor.setBlockData(slab);
							break;
						}
					}
					
				}else if(GenUtils.chance(1,15)){ //Mushroom
					floor.setType(GenUtils.randMaterial(Material.BROWN_MUSHROOM,Material.RED_MUSHROOM));
				}
				
			}
		}
	}
	
	public boolean setMineshaftSupport(Wall left, Wall right, Wall ceil){
		if(left == null || right == null){
			return false; //Lol wtf is this situation even
		}
		
		//Check interval
		if(left.getDirection().getModX() != 0){
			if(left.getX() % 5 != 0) return false;
		}else if(left.getDirection().getModZ() != 0){
			if(left.getZ() % 5 != 0) return false;
		}
		
		//Check if the support distance is too small
		left = left.getRight();
		right = right.getLeft();
		
		//At least distance of 3
		int dist = (int) left.get().getVector().distance(right.get().getVector());
		if(dist >= 3){
			left.LPillar(10, false, rand, Material.OAK_FENCE);
			left.getRelative(0,-1,0).downUntilSolid(rand, Material.OAK_FENCE);
		
			right.LPillar(10, false, rand, Material.OAK_FENCE);
			right.getRelative(0,-1,0).downUntilSolid(rand, Material.OAK_FENCE);
		
			
			//Support
			if(ceil != null){
				Orientable log = (Orientable) Bukkit.createBlockData(Material.OAK_LOG);
				if(left.getDirection().getModX() != 0)
					log.setAxis(Axis.Z);
				if(left.getDirection().getModZ() != 0)
					log.setAxis(Axis.X);
				ceil = left.clone().getRelative(0, ceil.getY()-left.getY(), 0).getLeft();
				
				Lantern lantern = (Lantern) Bukkit.createBlockData(Material.LANTERN);
				lantern.setHanging(true);
				
				for(int i = 0; i < dist+2; i++){
					Wall support = ceil.getRight(i);
					if(!support.getType().isSolid()||
							support.getType() == Material.OAK_FENCE){
						if(support.getRelative(0,1,0).getType() != Material.OAK_LOG
								&& support.getRelative(0,-1,0).getType() != Material.OAK_LOG){
							support.setBlockData(log);
							
							//L A M P 
							if(GenUtils.chance(rand, 1, 100)){
								support.getRelative(0,-1,0).get()
								.lsetBlockData(lantern);
							}
							
							//Vine
							if(GenUtils.chance(rand,1,10)){
								BlockUtils.vineUp(support.get(), 3);
							}
						}
					}
				}
			}
			
			
		}
		return true;
	}
	
	@Override
	public boolean customCarve(SimpleBlock base, BlockFace dir, int pathWidth){
		Wall core = new Wall(base.getRelative(0,1,0),dir);
		int seed = 55+core.getX()+core.getY()^2+core.getZ()^3;
		BlockUtils.carveCaveAir(seed, 
				pathWidth,pathWidth+1,pathWidth, core.get(), BlockUtils.stoneLike);
		
		return true;
	}
	
	@Override
	public int getPathWidth(){
		return 3;
	}
	
}
