package org.terraform.structure.mineshaft;

import java.util.Random;

import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.GenUtils;

public class MineshaftRegistry {
	
	private static final int mineshaftConst = 92935281;
	public static final MineshaftCavePopulator cavePop = new MineshaftCavePopulator();
	
	public static boolean isMineshaftMegachunk(MegaChunk c, TerraformWorld tw){
		Random rand = tw.getHashedRand(c.getX(), c.getZ(), mineshaftConst);
		 return GenUtils
				 .chance(rand,
						 TConfigOption.STRUCTURES_MINESHAFT_CHANCE.getInt(),
						 100);
	}

}
