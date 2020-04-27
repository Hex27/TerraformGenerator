package org.terraform.populators;

import java.util.Random;

import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

public class RiverWormCreator {
		
	private TerraformWorld tw;
	
	public RiverWormCreator(TerraformWorld tw){
		this.tw = tw;
	}
	
//	private static Collection<SimpleChunkLocation> processed = new ArrayList<>();

	public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
//		SimpleChunkLocation sc = new SimpleChunkLocation(chunk);
//		if(processed.contains(sc)) return;
//		processed.add(sc);
		
		if(GenUtils.chance(19, 20)) return;
		int x = GenUtils.randInt(random, 0, 15) + data.getChunkX()*16;
		int z = GenUtils.randInt(random, 0, 15) + data.getChunkZ()*16;
		
		RiverWorm worm = new RiverWorm(tw,data,x,z,(int) tw.getSeed());
		while(worm.hasNext()){
			worm.next();
		}
	}


}
