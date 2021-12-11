package org.terraform.coregen;

import org.terraform.carving.NoiseCaveEntranceCarver;
import org.terraform.carving.StandardCaveEntranceCarver;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

import java.util.ArrayList;
import java.util.Random;

public class CarverRegistry {
    private static final ArrayList<NoiseCaveEntranceCarver> CARVERS = new ArrayList<>()
    		{{
    			add(new StandardCaveEntranceCarver());
    		}};

    public static void doCarving(TerraformWorld tw, PopulatorDataAbstract data, Random random) {
        for(int x = data.getChunkX()*16;  x < data.getChunkX()*16+16; x++)
        	for(int z = data.getChunkZ()*16;  z < data.getChunkZ()*16+16; z++)
        	{
        		int height = HeightMap.getBlockHeight(tw, x, z);
        		for (NoiseCaveEntranceCarver carver : CARVERS) {
                	carver.carve(data, tw, random, x, z, height);
                }
        	}
    }
}
