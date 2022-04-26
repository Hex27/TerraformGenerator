package org.terraform.structure.ancientcity;

import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayoutGenerator;
import java.util.Random;

public class AncientCityCenterPlatformPopulator extends AncientCityAbstractRoomPopulator {

    public AncientCityCenterPlatformPopulator(RoomLayoutGenerator gen, Random rand, boolean forceSpawn, boolean unique) {
        super(gen, rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	super.populate(data, room);
//        int[] lowerCorner = room.getLowerCorner(0);
//        int[] upperCorner = room.getUpperCorner(0);
//
//        //Flooring - Have a stone brick platform.
//        int y = room.getY();
//        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
//            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
//                SimpleBlock b = new SimpleBlock(data, x, y, z);
//                b.lsetType(Material.BLUE_WOOL);
//            }
//        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}
