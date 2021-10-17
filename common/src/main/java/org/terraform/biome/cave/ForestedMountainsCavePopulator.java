package org.terraform.biome.cave;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.CylinderBuilder;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Random;

public class ForestedMountainsCavePopulator extends AbstractCavePopulator {
    private static boolean genned = false;
    
    private MossyCavePopulator mossyCavePop;
    
    public ForestedMountainsCavePopulator() {
    	mossyCavePop = new MossyCavePopulator();
    }
    
    @Override
    public void populate(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor) {

        if (!genned) {
        	
            genned = true;
            TerraformGeneratorPlugin.logger.info("Spawning forested mountains cave at " + floor.toString() + ", floortype=" + floor.getType()  + ", aboveFloorType=" + floor.getRelative(0,1,0).getType());
        }
        
        //Likely to be a river cave
        if(ceil.getY() > TerraformGenerator.seaLevel && floor.getY() < TerraformGenerator.seaLevel)
        {
        	//Definitely a river cave
        	if(ceil.getAtY(TerraformGenerator.seaLevel).getType() == Material.WATER) {

                int caveHeight = ceil.getY() - TerraformGenerator.seaLevel - 1;
                
                if(caveHeight <= 2) return;

                
                //Pillars
                if(GenUtils.chance(random, 1, 100)) {
                	new CylinderBuilder(random, floor.getRelative(
                			0,(ceil.getY() - floor.getY())/2,0), 
                			Material.STONE)
                	.setRadius(1.5f)
                	.setRY((ceil.getY() - floor.getY())/2 + 3)
                	.setHardReplace(false)
                	.build();
                	return;
                }
                	
                //CEILING DECORATIONS
                
        		//Glow berries
                int glowBerryChance = 15;
                if (GenUtils.chance(random, 1, glowBerryChance)) {
                    int h = caveHeight / 2;
                    if(h > 0) {
                    	if (h > 6) h = 6;
                        OneOneSevenBlockHandler.downLCaveVines(h, ceil);
                    }
                }
                
                //Spore blossom
            	if(GenUtils.chance(random, 1, 30))
            		ceil.setType(OneOneSevenBlockHandler.SPORE_BLOSSOM);
                
                //WATER DECORATIONS
                //Lily pads
                if(GenUtils.chance(random, 1, 50)) {
                	ceil.getAtY(TerraformGenerator.seaLevel + 1).lsetType(Material.LILY_PAD);
                }

                //Don't touch slabbed floors or stalagmites
                if (Tag.SLABS.isTagged(floor.getType()) ||
                		Tag.WALLS.isTagged(floor.getType()))
                    return;
                
                //BOTTOM DECORATIONS (underwater)
                
                //sea pickles
                if(GenUtils.chance(random, 1, 20))
	                CoralGenerator.generateSeaPickles(
	                		floor.getPopData(), 
	                		floor.getX(), floor.getY()+1, floor.getZ());
                
        		return;
        	}
        }
        
    	mossyCavePop.populate(tw, random, ceil, floor);
        
    }
}
