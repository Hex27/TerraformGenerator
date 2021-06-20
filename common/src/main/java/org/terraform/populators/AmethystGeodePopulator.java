package org.terraform.populators;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.version.OneOneSevenBlockHandler;
import org.terraform.utils.version.OneOneSixBlockHandler;

import java.util.ArrayList;
import java.util.Random;

public class AmethystGeodePopulator {
	
	private final int geodeRadius;
	private final double frequency;
	private final int minDepth;
	private final int minDepthBelowSurface;
	
	
    public AmethystGeodePopulator(int geodeRadius, double frequency, int minDepth, int minDepthBelowSurface) {
		this.geodeRadius = geodeRadius;
		this.frequency = frequency;
    	this.minDepth = minDepth;
    	this.minDepthBelowSurface = minDepthBelowSurface;
    }

    
    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {
    	if(GenUtils.chance(random, (int) (frequency*10000.0), 10000)) {
			int x = GenUtils.randInt(random, 0, 15) + data.getChunkX() * 16;
        	int z = GenUtils.randInt(random, 0, 15) + data.getChunkZ() * 16;
        	int upperHeightRange = GenUtils.getHighestGround(data, x, z) - minDepthBelowSurface;
        	if(upperHeightRange > minDepth)
        		upperHeightRange = minDepth;
        	
        	if(upperHeightRange < 14) return;
        	
        	//Elevate 14 units up.
        	int y = GenUtils.randInt(random, 14, upperHeightRange);
        	placeGeode(random.nextInt(9999), geodeRadius, new SimpleBlock(data,x,y,z));
    	}
    }
    
    public static void placeGeode(int seed, float r, SimpleBlock block) {
        if (r <= 1) return;
        ArrayList<SimpleBlock> amethystBlocks = new ArrayList<>();
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -r; x <= r; x++) {
            for (float y = -r; y <= r; y++) {
                for (float z = -r; z <= r; z++) {
                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    double outerCrust = 
                    		(Math.pow(x, 2) 
                				+ Math.pow(y, 2)
                				+ Math.pow(z, 2)
                    		)/Math.pow(r, 2);
                    double innerCrust = 
                    		(Math.pow(x, 2) 
                				+ Math.pow(y, 2)
                				+ Math.pow(z, 2)
                    		)/Math.pow(r-1.0, 2);
                    double amethystCrust = 
                    		(Math.pow(x, 2) 
                				+ Math.pow(y, 2)
                				+ Math.pow(z, 2)
                    		)/Math.pow(r-2.2, 2);
                    double airHollower = 
                    		(Math.pow(x, 2) 
                				+ Math.pow(y, 2)
                				+ Math.pow(z, 2)
                    		)/Math.pow(r-3.3, 2);
                    
                    double noiseVal = 1 + 0.4 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ());
                    
                    if (airHollower <= noiseVal) {
                    	if(BlockUtils.isWet(rel))
                    		rel.setType(Material.WATER);
                    	else
                    		rel.setType(Material.CAVE_AIR);
                        //Only do the other stuff if this isn't air.
                    }else if(rel.getType().isSolid())
	                    if (amethystCrust <= noiseVal) {
	                        rel.setType(OneOneSevenBlockHandler.AMETHYST_BLOCK,OneOneSevenBlockHandler.BUDDING_AMETHYST);
	                        amethystBlocks.add(rel);
	                    }else if (innerCrust <= noiseVal) {
	                        rel.setType(OneOneSevenBlockHandler.CALCITE);
	                    }else if (outerCrust <= noiseVal) {
	                        rel.setType(OneOneSixBlockHandler.SMOOTH_BASALT);
	                    }
                }
            }
        }
        
        //Place crystals
        for(SimpleBlock rel:amethystBlocks) {
        	for(BlockFace face:BlockUtils.sixBlockFaces) {
        		if(GenUtils.chance(1,6)) continue;
            	SimpleBlock target = rel.getRelative(face);
            	if(BlockUtils.isAir(target.getType()) && GenUtils.chance(1,6)) {
            		new DirectionalBuilder(OneOneSevenBlockHandler.AMETHYST_CLUSTER)
            		.setFacing(face)
            		.apply(target);
            	}
            }
        }
    }
}
