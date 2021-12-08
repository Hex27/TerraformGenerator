package org.terraform.tree;

import java.io.FileNotFoundException;
import java.util.Random;

import org.bukkit.Material;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.schematic.TerraSchematic;
import org.terraform.utils.GenUtils;

public class VanillaMushroomBuilder {
	//I hate supporting another plugin
	
	public static final String RED_MUSHROOM_CAP = "redmushroomcap";
	public static final String BROWN_MUSHROOM_CAP = "brownmushroomcap";
	
	public static void buildVanillaMushroom(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z, String capSchematic) {
		Random rand = tw.getRand(16L * 16 * x + 16L * y + z);
		
		int height = GenUtils.randInt(rand, 5, 7);
		
		new Wall(new SimpleBlock(data,x,y,z)).Pillar(height, Material.MUSHROOM_STEM);
		
		try {
			TerraSchematic capSchem = TerraSchematic.load(capSchematic, new SimpleBlock(data, x, y + height - 2, z));
			capSchem.apply();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
}
