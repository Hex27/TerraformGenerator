package org.terraform.structure.pyramid;

import java.util.Random;

import org.bukkit.Material;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.utils.GenUtils;

public class PyramidDungeonPathPopulator extends PathPopulatorAbstract{
	
	Random rand;
	int height = 3;
	public PyramidDungeonPathPopulator(Random rand) {
		super();
		this.rand = rand;
	}

	public PyramidDungeonPathPopulator(Random rand, int height) {
		super();
		this.rand = rand;
		this.height = height;
	}

	@Override
	public void populate(PathPopulatorData ppd) {
		
		//Gravel tnt trap
		if(GenUtils.chance(this.rand,1,300)) {
			//TerraformGeneratorPlugin.logger.info("Pyramid trap at " + ppd.base.getX()+","+ppd.base.getY()+","+ppd.base.getZ());
			ppd.base.setType(Material.GRAVEL);
			ppd.base.getRelative(0,-1,0).setType(Material.TNT);
			ppd.base.getRelative(0,1,0).setType(Material.STONE_PRESSURE_PLATE);
			for(int i = -2; i > -8; i--) {
				ppd.base.getRelative(0,i,0).setType(Material.AIR);
			}
		}
	}
	
	@Override
	public int getPathWidth(){
		return 1;
	}
	
	@Override
	public int getPathHeight() {
		return height;
	}
	
}
