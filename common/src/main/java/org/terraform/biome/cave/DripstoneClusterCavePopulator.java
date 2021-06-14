package org.terraform.biome.cave;

import org.bukkit.Tag;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Random;

public class DripstoneClusterCavePopulator extends AbstractCaveClusterPopulator {
    //private static boolean genned = false;
    
    @Override
    protected void oneUnit(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor) {
    	//if (!genned) {
        //    genned = true;
        //}
    	
    	//=========================
        //Upper decorations
        //=========================

        int caveHeight = ceil.getY() - floor.getY();

        //Don't touch slabbed floors or stalagmites
        if (Tag.SLABS.isTagged(floor.getType()) ||
        		Tag.WALLS.isTagged(floor.getType()))
            return;
        
        //All ceiling is dripstone
        ceil.setType(OneOneSevenBlockHandler.DRIPSTONE_BLOCK);
        
        //Stalactites
        if (GenUtils.chance(random, 1, 3)) {
            int h = caveHeight / 4;
            if (h < 1) h = 1;
            if (h > 4) h = 4;
            OneOneSevenBlockHandler.downLPointedDripstone(h, ceil.getRelative(0,-1,0));
        }

        //=========================
        //Lower decorations 
        //=========================

        //Floor is dripstone
        floor.setType(OneOneSevenBlockHandler.DRIPSTONE_BLOCK);
        
        //Stalagmites
        if (GenUtils.chance(random, 1, 3)) {
            int h = caveHeight / 4;
            if (h < 1) h = 1;
            if (h > 4) h = 4;
            OneOneSevenBlockHandler.upLPointedDripstone(h, floor.getRelative(0,1,0));
        }
    }
    
    
}
