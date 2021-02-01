package org.terraform.structure.village.plains;

import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;

import java.util.Map.Entry;
import java.util.Random;

public class PlainsVillageAnimalPenPopulator extends RoomPopulatorAbstract {

    

    public PlainsVillageAnimalPenPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	
    	for(Entry<Wall,Integer> entry:room.getFourWalls(data, 1).entrySet()) {
    		Wall w = entry.getKey();
    		for(int i = 0; i < entry.getValue(); i++) {
    			if(i == 0 || i == entry.getValue()-1) {
    				w.setType(Material.OAK_LOG);
    				w.getRelative(0,1,0).setType(Material.COBBLESTONE_SLAB);
    			}else {
    				w.setType(Material.OAK_FENCE);
    			}
				w.CorrectMultipleFacing(1);
    			
    			w = w.getLeft();
    		}
    	}
    	
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return (room.getWidthX() < 18 || room.getWidthZ() < 18);
    }
}
