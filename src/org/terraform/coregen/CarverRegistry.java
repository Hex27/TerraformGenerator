package org.terraform.coregen;

import java.util.ArrayList;
import java.util.Random;

import org.terraform.carving.Carver;
import org.terraform.carving.CaveWormCreator;
import org.terraform.data.TerraformWorld;

public class CarverRegistry {
	
	private static ArrayList<Carver> carvers = new ArrayList<Carver>(){{
		//add(new CaveWormCreator());
	}};
	
	public static void doCarving(TerraformWorld tw, PopulatorDataAbstract data, Random random){
		for(Carver carver:carvers){
			carver.carve(tw, data, random);
		}
	}

}
