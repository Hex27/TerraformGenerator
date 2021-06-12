package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Random;

public class SnowyMountainsHandler extends AbstractMountainHandler {

	protected double getPeakMultiplier(Random sectionRandom) {
		return GenUtils.randDouble(sectionRandom, 1.3, 1.6);
	}
	
    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.SNOWY_MOUNTAINS;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{Material.STONE};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getHighestGround(data, x, z);
				if(y < TerraformGenerator.seaLevel) continue;
				
				//Dirt Fixer 
				//Snowy wastelands and the like will spawn snow blocks, then dirt blocks.
				//Analyze 5 blocks down. Replace the block if anything next to it is stone.
				correctDirt(new SimpleBlock(data,x,y,z));
				
				//Snow on top
				data.setType(x, y+1, z, Material.SNOW);
				

                //Make patches of decorative rock on the mountain sides.
                if (GenUtils.chance(random, 1, 25)) {
                	Material stoneType = GenUtils.randMaterial(Material.ANDESITE, Material.DIORITE);
                    stoneStack(stoneType, data, random, x, y, z);
                    for (int nx = -2; nx <= 2; nx++)
                        for (int nz = -2; nz <= 2; nz++) {
                            if (GenUtils.chance(random, 1, 5)) continue;
                            y = GenUtils.getHighestGround(data, x + nx, z + nz);
                            
                            //Another check, make sure relative position isn't underwater.
                            if(y < TerraformGenerator.seaLevel)
                            	continue;
                            stoneStack(stoneType, data, random, x + nx, y, z + nz);
                        }
                }
                
				//Thick Snow on shallow areas
                //Snowy Snow on near flat areas
                double gradient = HeightMap.getTrueHeightGradient(data, x, z, 3);
				if(gradient < 1.4) {
					
					if(data.getBiome(x, z) != getBiome()) continue;
	                //Don't touch submerged blocks
					if(data.getBiome(x, z) != getBiome())
						continue;
	                if(y < TerraformGenerator.seaLevel)
	                	continue;
	                if(gradient < 1.2)
	                	data.setType(x, y, z, OneOneSevenBlockHandler.POWDER_SNOW);
	                else
	                	data.setType(x, y, z, Material.SNOW_BLOCK);
				}
			}
		}
    }
    
    private void correctDirt(SimpleBlock start) {
    	for(int depth = 0; depth < 5; depth++) {
    		for(BlockFace face:BlockUtils.directBlockFaces) {
    			if(start.getRelative(face).getType() == Material.STONE) {
    				start.setType(Material.STONE);
    				break;
    			}
    		}
    		start = start.getRelative(0,-1,0);
    	}
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.ICY_BEACH;
	}
	

    private static void stoneStack(Material stoneType, PopulatorDataAbstract data, Random rand, int x, int y, int z) {
        data.setType(x, y, z, stoneType);
        
        int depth = GenUtils.randInt(rand, 3, 7);
        for (int i = 1; i < depth; i++) {
        	if(!BlockUtils.isStoneLike(data.getType(x, y-i, z)))
        		break;
            data.setType(x, y - i, z, stoneType);
            if(BlockUtils.isExposedToNonSolid(new SimpleBlock(data, x, y-i, z))) {
            	depth++;
            }
        }
    }
}
