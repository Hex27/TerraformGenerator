package org.terraform.structure.monument;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

public class MonumentPathPopulator extends PathPopulatorAbstract{
	
	Random rand;
	MonumentDesign design;
	public MonumentPathPopulator(MonumentDesign design, Random rand) {
		super();
		this.rand = rand;
		this.design = design;
	}

	@Override
	public void populate(PathPopulatorData ppd) {
		Wall w = new Wall(ppd.base,ppd.dir);
		
		//Fill with water :<
		w.getLeft().getLeft().RPillar(5, rand, Material.WATER);
		w.getLeft().RPillar(5, rand, Material.WATER);
		w.RPillar(5, rand, Material.WATER);
		w.getRight().RPillar(5, rand, Material.WATER);
		w.getRight().getRight().RPillar(5, rand, Material.WATER);
		
		//Pillars
		if(GenUtils.chance(rand,1,20)){
			w.RPillar(5, rand, GenUtils.mergeArr(design.tileSet,new Material[]{Material.SEA_LANTERN}));
		}else if(GenUtils.chance(rand, 1, 50)){
			MonumentRoomPopulator.setThickPillar(rand, design, w.get().getRelative(0,3,0));
		}
		
		//Thick pillars
		if(GenUtils.chance(rand, 1, 50)){
			MonumentRoomPopulator.setThickPillar(rand, design, w.get().getRelative(0,-1,0));
		}
		
		//Small spires
		if(GenUtils.chance(rand, 1, 50)){
			if(w.getRelative(0,6,0).getType().isSolid() 
					&& !w.getRelative(0,7,0).getType().isSolid())
				design.spire(w.getRelative(0,7,0), rand);
		}
	}
	
	@Override
	public int getPathWidth(){
		return 5;
	}
	
}
