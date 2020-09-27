package org.terraform.structure.dungeon;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

public class DrownedDungeonPopulator extends SmallDungeonPopulator{

	@Override
	public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
		MegaChunk mc = new MegaChunk(data.getChunkX(),data.getChunkZ());

		int[] spawnCoords = new int[]{data.getChunkX()*16,data.getChunkZ()*16};
		int[][] allCoords = getCoordsFromMegaChunk(tw,mc);
		for(int[] coords:allCoords){
			if(coords[0] >> 4 == data.getChunkX() && coords[1] >> 4 == data.getChunkZ()){
				spawnCoords = coords;
				break;
			}
		}
		
		int x = spawnCoords[0];//data.getChunkX()*16 + random.nextInt(16);
		int z = spawnCoords[1];//data.getChunkZ()*16 + random.nextInt(16);
		Random rand = this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ());
		
		//int y = GenUtils.getHighestGround(data, x, z);
		
		
		spawnDungeonRoom(x,z,tw,rand,data);
	}
	
	private static final Material[] cobbleSet = new Material[]{Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL};
	private static final Material[] graniteSet = new Material[]{Material.GRANITE, Material.GRANITE_WALL};
	private static final Material[] dioriteSet = new Material[]{Material.DIORITE, Material.DIORITE_WALL};
	private static final Material[] andesiteSet = new Material[]{Material.ANDESITE, Material.ANDESITE_WALL};
	private static final Material[] bricksSet = new Material[]{Material.STONE_BRICKS, Material.STONE_BRICK_WALL};
	
	private static final Material[][] sets = new Material[][]{cobbleSet,graniteSet,dioriteSet,andesiteSet,bricksSet};
	public void spawnDungeonRoom(int x, int z, TerraformWorld tw, Random rand,
			PopulatorDataAbstract data){
		TerraformGeneratorPlugin.logger.info("Spawning Drowned Dungeon at " + x + "," + z);
		int setIndex = rand.nextInt(sets.length);
		Material[] set = sets[setIndex];
		int radius = GenUtils.randInt(rand,5,10);
		
		//First Round
		for(int nx = -radius; nx <= radius; nx++){
			for(int nz = -radius; nz <= radius; nz++){
				if(nx*nx+nz*nz > radius*radius + GenUtils.randInt(rand,-10,10))
					continue;

				int y = GenUtils.getHighestGround(data, nx+x, nz+z);
				
				//Spawner
				if(nx == 0 && nz == 0){
					data.setSpawner(x, y+1, z, EntityType.DROWNED);
					continue;
				}
				
				//Spawn a dungeon Pillar
				if(GenUtils.chance(rand, 1, 10)){
					Wall w = new Wall(new SimpleBlock(data,nx+x,y+1,nz+z),BlockFace.NORTH);
					w.LPillar(GenUtils.randInt(1, 7), rand, set);
				}else if(GenUtils.chance(rand,1,70)){
					Directional dir = (Directional) Bukkit.createBlockData(Material.CHEST);
					dir.setFacing(BlockUtils.getDirectBlockFace(rand));
					data.setBlockData(x+nx,y+1,z+nz,dir);
					if(radius < 7)
						data.lootTableChest(x+nx,y+1,z+nz, TerraLootTable.UNDERWATER_RUIN_SMALL);
					else
						data.lootTableChest(x+nx,y+1,z+nz, TerraLootTable.UNDERWATER_RUIN_BIG);
				}else if(GenUtils.chance(rand, 1,10)){
					CoralGenerator.generateKelpGrowth(data, nx+x, y+1, nz+z);
				}else if(GenUtils.chance(rand,1,10)){
					data.setType(x+nx,y,z+nz,Material.MAGMA_BLOCK);
				}
			}
		}
		
		//Second decorative round
		for(int nx = -radius; nx <= radius; nx++){
			for(int nz = -radius; nz <= radius; nz++){
				int y = GenUtils.getHighestGround(data, nx+x, nz+z);
				if(GenUtils.chance(rand, 1,15)){
					CoralGenerator.generateSingleCoral(data, nx, y, nz);
				}else if(GenUtils.chance(rand,1,10)){
					data.setType(x+nx,y+1,z+nz,Material.SEAGRASS);
				}
			}
		}
	}
	
}
