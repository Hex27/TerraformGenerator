package org.terraform.village.plains;

import org.terraform.coregen.HeightMap;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;

public abstract class PlainsVillagePathPopulator extends PathPopulatorAbstract {
	TerraformWorld tw;

    public PlainsVillagePathPopulator(TerraformWorld tw) {
        this.tw = tw;
    }

    @Override
    public void populate(PathPopulatorData ppd) {
    	
    	//Find the ground level to place pathways
    	ppd.base = new SimpleBlock(
    			ppd.base.getPopData(),
    			ppd.base.getX(),
    			HeightMap.getHeight(tw, ppd.base.getX(), ppd.base.getZ()),
    			ppd.base.getZ());
    }

    @Override
    public int getPathWidth() {
        return 3;
    }
}
