package org.terraform.structure.ancientcity;

import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleLocation;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayoutGenerator;

import java.util.HashSet;
import java.util.Random;

public class AncientCityRuinsPlatform extends AncientCityAbstractRoomPopulator {

    public AncientCityRuinsPlatform(HashSet<SimpleLocation> occupied, RoomLayoutGenerator gen, Random rand, boolean forceSpawn, boolean unique) {
        super(occupied, gen, rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	super.populate(data, room);
//        int[] lowerCorner = room.getLowerCorner(0);
//        int[] upperCorner = room.getUpperCorner(0);
//
//        //Flooring
//        int y = room.getY();
//        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
//            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
//                SimpleBlock b = new SimpleBlock(data, x, y, z);
//                b.lsetType(AncientCityUtils.deepslateBricks);
//            }
//        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}
