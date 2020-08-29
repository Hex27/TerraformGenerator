package org.terraform.structure.monument;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Stairs;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.schematic.TerraSchematic;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public enum MonumentDesign {
	
	DARK_PRISMARINE_CORNERS(Material.DARK_PRISMARINE,Material.PRISMARINE_BRICKS),
	PRISMARINE_LANTERNS(Material.PRISMARINE_BRICKS,Material.PRISMARINE_BRICKS,Material.PRISMARINE),
	DARK_LIGHTLESS(Material.PRISMARINE,Material.DARK_PRISMARINE,Material.DARK_PRISMARINE,Material.DARK_PRISMARINE);

	Material[] tileSet;
	
	MonumentDesign(Material... tileSet){
		this.tileSet = tileSet;
	}
	
	public Material[] tileSet(){
		return tileSet;
	}
	
	public Material slab(){
		switch(this){
		case DARK_LIGHTLESS:
			return Material.DARK_PRISMARINE_SLAB;
		case DARK_PRISMARINE_CORNERS:
			return GenUtils.randMaterial(Material.DARK_PRISMARINE_SLAB,Material.PRISMARINE_BRICK_SLAB);
		case PRISMARINE_LANTERNS:
			return GenUtils.randMaterial(Material.PRISMARINE_SLAB,Material.PRISMARINE_BRICK_SLAB);
		}
		return null;
	}
	
	public Material stairs(){
//		switch(this){
//		case DARK_LIGHTLESS:
//			return Material.DARK_PRISMARINE_STAIRS;
//		case DARK_PRISMARINE_CORNERS:
//			return GenUtils.randMaterial(Material.DARK_PRISMARINE_STAIRS,Material.DARK_PRISMARINE_STAIRS,Material.DARK_PRISMARINE_STAIRS,Material.PRISMARINE_BRICK_STAIRS);
//		case PRISMARINE_LANTERNS:
//			return GenUtils.randMaterial(Material.PRISMARINE_STAIRS,Material.PRISMARINE_BRICK_STAIRS);
//		}
		return Material.DARK_PRISMARINE_STAIRS;
	}
	
	public Material mat(Random rand){
		return GenUtils.randMaterial(rand, tileSet);
	}
	
	public void spawnLargeLight(PopulatorDataAbstract data,int x,int y, int z){
		try{
			x++;
			z++;
			y++;
			World w = ((PopulatorDataPostGen) data).getWorld();
			TerraSchematic schema = TerraSchematic.load(this.toString().toLowerCase() + "-largelight", new Location(w,x,y,z));
			schema.parser = new MonumentSchematicParser();
			schema.setFace(BlockFace.NORTH);
			schema.apply();
		}catch(Throwable e){ e.printStackTrace();}
	}
	
	public void upSpire(SimpleBlock base, Random rand){
		while(base.getType().isSolid() || base.getRelative(0,1,0).getType().isSolid()){
			base = base.getRelative(0,1,0);
			if(base.getY() > TerraformGenerator.seaLevel)
				return;
		}
		spire(new Wall(base,BlockFace.NORTH), rand);
	}
	
	public void spire(Wall w, Random rand){
		spire(w,rand,7);
	}
	
	public void spire(Wall w, Random rand, int height){
		switch(this){
		case DARK_LIGHTLESS:
			for(int i = 0; i < height; i++){
				if(i == 0) w.setType(Material.DARK_PRISMARINE);
				else if(i > height-3) w.setType(Material.PRISMARINE_WALL);
				else{
					w.setType(GenUtils.randMaterial(Material.DARK_PRISMARINE,Material.PRISMARINE_WALL));
					if(rand.nextBoolean()){
						Stairs stairs = (Stairs) Bukkit.createBlockData(Material.DARK_PRISMARINE_STAIRS);
						stairs.setFacing(BlockUtils.getDirectBlockFace(rand));
						stairs.setHalf(rand.nextBoolean() ? Half.TOP : Half.BOTTOM);
						w.setBlockData(stairs);
					}
					
				}
				w = w.getRelative(0,1,0);
			}
			break;
		case DARK_PRISMARINE_CORNERS:
			for(int i = 0; i < height; i++){
				if(i == 0) w.setType(Material.DARK_PRISMARINE);
				else if(i == 3) w.setType(Material.SEA_LANTERN);
				else{
					w.setType(GenUtils.randMaterial(Material.DARK_PRISMARINE,Material.PRISMARINE_WALL));
					if(rand.nextBoolean()){
						Stairs stairs = (Stairs) Bukkit.createBlockData(Material.DARK_PRISMARINE_STAIRS);
						stairs.setFacing(BlockUtils.getDirectBlockFace(rand));
						stairs.setHalf(rand.nextBoolean() ? Half.TOP : Half.BOTTOM);
					}
					
				}
				w = w.getRelative(0,1,0);
			}
			break;
		case PRISMARINE_LANTERNS:
			for(int i = 0; i < height; i++){
				if(i == 0) w.setType(Material.PRISMARINE_BRICKS);
				else if(i > height-2) w.setType(Material.PRISMARINE_WALL);
				else if(i == height-2) w.setType(Material.PRISMARINE_BRICKS);
				else{
					w.setType(Material.PRISMARINE_WALL);
					if(i == 3){
						w.setType(Material.SEA_LANTERN);
					}
				}
				w = w.getRelative(0,1,0);
			}
			break;		
		}
	}
	
}
