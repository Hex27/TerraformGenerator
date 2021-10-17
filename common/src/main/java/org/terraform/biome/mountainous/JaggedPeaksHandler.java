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
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Random;

public class JaggedPeaksHandler extends AbstractMountainHandler {
	
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
    public double calculateHeight(TerraformWorld tw, int x, int z) {
    	double height = super.calculateHeight(tw, x, z);
    	FastNoise jaggedPeaksNoise = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.BIOME_JAGGED_PEAKSNOISE, 
        		world -> {
        			FastNoise n = new FastNoise((int) (world.getSeed()*2));
        	        n.SetNoiseType(NoiseType.SimplexFractal);
        	        n.SetFractalOctaves(6);
        	        n.SetFrequency(0.03f);
        	        return n;
        		});
    	
    	double noise = jaggedPeaksNoise.GetNoise(x,z); 
    	if(noise > 0) {
    		height += noise * 50;
    	}
    	return height * 1.03;
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
				

                //Make patches of decorative rock on the mountain sides.
                if (GenUtils.chance(random, 1, 25)) {
                	Material stoneType = GenUtils.randMaterial(Material.ANDESITE, Material.DIORITE);
                    stoneStack(stoneType, data, random, x, y, z);
                    for (int nx = -2; nx <= 2; nx++)
                        for (int nz = -2; nz <= 2; nz++) {
                            if (GenUtils.chance(random, 1, 5)) continue;
                            int stoneY = GenUtils.getHighestGround(data, x + nx, z + nz);
                            
                            //Another check, make sure relative position isn't underwater.
                            if(stoneY < TerraformGenerator.seaLevel)
                            	continue;
                            stoneStack(stoneType, data, random, x + nx, stoneY, z + nz);
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
	                if(gradient < 1.2) {
	                	data.setType(x, y, z, OneOneSevenBlockHandler.POWDER_SNOW);
	                	data.setType(x, y+1, z, Material.AIR); //remove snow
	                }else
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

	@Override
	public BiomeBank getRiverType() {
		return BiomeBank.FROZEN_RIVER;
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
