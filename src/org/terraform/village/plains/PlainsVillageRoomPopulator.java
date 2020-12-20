package org.terraform.village.plains;

import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import java.util.Random;

public abstract class PlainsVillageRoomPopulator extends RoomPopulatorAbstract {

	TerraformWorld tw;
    public PlainsVillageRoomPopulator(TerraformWorld tw, Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
        this.tw = tw;
    }


    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	
    	//All buildings will be on the surface
    	room.setY(HeightMap.getHeight(tw, room.getX(), room.getZ()));
    }
}
