package org.terraform.structure.monument;

import java.util.Map.Entry;
import java.util.Random;

import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

public class CoralRoomPopulator extends LevelledRoomPopulator {

	public CoralRoomPopulator(Random rand, MonumentDesign design,
			boolean forceSpawn, boolean unique) {
		super(rand, design, forceSpawn, unique);
	}
	
	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room){
		super.populate(data, room);
		for(Entry<Wall,Integer> entry:room.getFourWalls(data, 1).entrySet()){
			Wall w = entry.getKey().getRelative(0,5,0);
			int length = entry.getValue();
			for(int i = 0; i < length; i++){
				int x = w.get().getX();
				int y = w.get().getY() + GenUtils.randInt(rand,0,room.getHeight()-6);
				int z = w.get().getZ();
				
				if(GenUtils.chance(rand,1,15))
					CoralGenerator.generateCoral(data, x, y, z);
				
				if(GenUtils.chance(rand,1,5))
					CoralGenerator.generateSponge(data, x, y, z);
				
				w = w.getLeft();
			}
		}	
	}

}
