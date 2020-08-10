package org.terraform.biome.cave;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;

public class FrozenCavePopulator extends AbstractCavePopulator {

	private static boolean genned = false;
	
	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				if(!(tw.getBiomeBank(x, GenUtils.getHighestGround(data,x,z), z).getCavePop() 
						instanceof FrozenCavePopulator))
					continue;
				for(int[] pair:GenUtils.getCaveCeilFloors(data, x, z)){
					int ceil = pair[0]; //non-solid
					int floor = pair[1]; //solid
					
					if(!genned){
						genned = true;
						TerraformGeneratorPlugin.logger.info("Spawning frozen cave at " + x + "," + floor + "," + z);
					}
					
					int caveHeight = ceil-floor;
					
					//Don't touch slabbed floors or stalagmites
					if(data.getType(x, floor, z).toString().endsWith("SLAB")||
							data.getType(x, floor, z).toString().endsWith("WALL"))
						continue;

					//=========================
					//Upper decorations
					//=========================
					
					//Upper Ice
					data.setType(x, ceil, z, Material.ICE);
					
					//Stalactites
					if(GenUtils.chance(random,1,24)){
						int h = caveHeight/4;
						if(h < 1) h = 1;
						Wall w = new Wall(new SimpleBlock(data,x,ceil-1,z),BlockFace.NORTH);
						w.downLPillar(random, h, Material.ICE);
						
					}
					
					//=========================
					//Lower decorations 
					//=========================
					
					//Lower Ice
					data.setType(x, floor+1, z, Material.ICE);
					
					//Stalagmites
					if(GenUtils.chance(random,1,25)){
						int h = caveHeight/4;
						if(h < 1) h = 1;
						Wall w = new Wall(new SimpleBlock(data,x,floor+2,z),BlockFace.NORTH);
						if(w.getType() == Material.CAVE_AIR)
						w.LPillar(h, random, Material.ICE);
						
					}
				}
			}
		}
	}

}
