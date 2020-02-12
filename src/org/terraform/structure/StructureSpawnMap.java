package org.terraform.structure;

import org.terraform.data.TerraformWorld;
import org.terraform.utils.FastNoise;

public class StructureSpawnMap {
	
	public static StructurePopulator getStructurePopulator(int chunkX, int chunkZ, TerraformWorld tw){
		FastNoise noise = new FastNoise(tw.getRand(7772).nextInt(9995));
		noise.SetFrequency(0.9f);
		return null;
	}

}
