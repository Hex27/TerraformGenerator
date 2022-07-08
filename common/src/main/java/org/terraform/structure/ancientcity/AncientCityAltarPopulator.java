package org.terraform.structure.ancientcity;

import java.util.HashSet;
import java.util.Random;
import java.util.Map.Entry;

import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.version.OneOneSevenBlockHandler;

public class AncientCityAltarPopulator extends AncientCityAbstractRoomPopulator {

    public AncientCityAltarPopulator(HashSet<SimpleLocation> occupied, RoomLayoutGenerator gen, Random rand, boolean forceSpawn, boolean unique) {
        super(occupied, gen, rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	super.populate(data, room);
    	//Generates outer walls
    	for(Entry<Wall,Integer> entry:this.effectiveRoom.getFourWalls(data, 0).entrySet()) {
    		Wall w = entry.getKey();
    		Wall center = null;
    		boolean shouldPlaceAltar = true;
    		for(int i = 0; i < entry.getValue(); i++) {
    			if(i == entry.getValue()/2) center = w;
    			if(this.gen.getPathPopulators().contains(new PathPopulatorData(w.getRear().getAtY(room.getY()), 3)))
    			{
    				shouldPlaceAltar = false;
    				break;
    			}
    			w = w.getLeft();
    		}
    		if(shouldPlaceAltar) {
    			if(entry.getValue() % 2 == 0)
    				placeAltarEven(center.getUp());
    			else
    				placeAltarOdd(center.getUp());
    			return;
    		}
    	}
    }
    
    public void placeAltarOdd(Wall w) {
    	w.Pillar(3, AncientCityUtils.deepslateTiles);
    	w.getLeft().Pillar(2, AncientCityUtils.deepslateTiles);
    	w.getRight().Pillar(2, AncientCityUtils.deepslateTiles);
    	w.getFront().setType(AncientCityUtils.deepslateTiles);
    	w.getFront().getUp().setType(OneOneSevenBlockHandler.CHISELED_DEEPSLATE);
    	
    }
    public void placeAltarEven(Wall w) {
    	
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}
