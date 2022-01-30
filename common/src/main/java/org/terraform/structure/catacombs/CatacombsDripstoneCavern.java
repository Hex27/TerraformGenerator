package org.terraform.structure.catacombs;

import java.util.Random;

import org.bukkit.Material;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.version.OneOneSevenBlockHandler;

public class CatacombsDripstoneCavern extends CatacombsStandardPopulator {

	public CatacombsDripstoneCavern(Random rand, boolean forceSpawn, boolean unique) {
		super(rand, forceSpawn, unique);
	}
	

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	super.populate(data, room);
    	SimpleBlock center = room.getCenterSimpleBlock(data).getUp();
    	new SphereBuilder(new Random(), center.getDown(), Material.CAVE_AIR)
    	.setRadius(3f)
    	.setHardReplace(true)
    	.build();

    	for(int i = 3; i <= GenUtils.randInt(3, 7); i++)
    	{
    		int[] coords = room.randomCoords(rand, 3);
    		SimpleBlock target = new SimpleBlock(data, coords[0], room.getY()+1, coords[2]);
    		target = target.findCeiling(room.getHeight());
    		if(target == null || target.getY() - room.getY() < 4) continue;
    		
    		target.setType(OneOneSevenBlockHandler.DRIPSTONE_BLOCK);
    		OneOneSevenBlockHandler.downLPointedDripstone(GenUtils.randInt(rand, 1, 3), target.getDown());
    	}
    }

}
