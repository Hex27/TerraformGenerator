package org.terraform.biome.cave;

import org.bukkit.Tag;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Random;

public class DripstoneClusterCavePopulator extends AbstractCaveClusterPopulator {
    
    public DripstoneClusterCavePopulator(float radius) {
		super(radius);
	}

	@Override
    protected void oneUnit(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor) {
    	
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
        if (GenUtils.chance(random, 1, 4)) {
            int h = caveHeight / 4;
            if (h < 1) h = 1;
            if (h > 4) h = 4;
            OneOneSevenBlockHandler.downLPointedDripstone(GenUtils.randInt(1, h), ceil.getRelative(0,-1,0));
        }

        //=========================
        //Lower decorations 
        //=========================

        //Floor is dripstone
        floor.setType(OneOneSevenBlockHandler.DRIPSTONE_BLOCK);
        
        //Stalagmites
        if (GenUtils.chance(random, 1, 4)) {
            int h = caveHeight / 4;
            if (h < 1) h = 1;
            if (h > 4) h = 4;
            OneOneSevenBlockHandler.upLPointedDripstone(GenUtils.randInt(1,h), floor.getRelative(0,1,0));
        }

        //=========================
        //Biome Setter 
        //=========================
        if(TerraformGeneratorPlugin.injector.getICAData(ceil.getPopData()) instanceof PopulatorDataICABiomeWriterAbstract) {
        	PopulatorDataICABiomeWriterAbstract data = (PopulatorDataICABiomeWriterAbstract) TerraformGeneratorPlugin.injector.getICAData(ceil.getPopData());
        	while(floor.getY() < ceil.getY()) {
        		data.setBiome(floor.getX(), floor.getY(), floor.getZ(), OneOneSevenBlockHandler.DRIPSTONE_CAVES);
        		floor = floor.getRelative(0,1,0);
        	}
        }
    }
    
    
}
