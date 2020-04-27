package org.terraform.structure.monument;

import java.io.FileNotFoundException;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Stairs;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;

public class TreasureRoomPopulator extends DecoratedSidesElderRoomPopulator {

	public TreasureRoomPopulator(Random rand, MonumentDesign design,
			boolean forceSpawn, boolean unique) {
		super(rand, design, forceSpawn, unique);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void populate(PopulatorDataAbstract data, CubeRoom room) {
		super.populate(data, room);
		int x = room.getX();
		int y = room.getY() + room.getHeight()/2;
		int z = room.getZ();
		TerraSchematic schema;
		
		//Spawn gold core
		try {
			World w = ((PopulatorDataPostGen) data).getWorld();
			//Add one to all to correct some weird aligning shit.
			schema = TerraSchematic.load("monument-gold", new Location(w,x+1,y-5,z+1));
			schema.parser = new MonumentSchematicParser(data);
			schema.setFace(BlockFace.NORTH);
			schema.apply();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//Spawn elder guardian
		//data.addEntity(x+5, y, z+5, EntityType.ELDER_GUARDIAN);
		
		//spawn claws
		spawnLowerClaw(data,x,room.getY(),z);
		spawnUpperClaw(data,x,room.getY()+room.getHeight(),z);
	}
	
	private void spawnLowerClaw(PopulatorDataAbstract data, int x, int y, int z){
		
		SimpleBlock block = new SimpleBlock(data,x,y,z);
		block.setType(Material.SEA_LANTERN);
		for(BlockFace face:BlockUtils.directBlockFaces){
			Stairs stair = (Stairs) Bukkit.createBlockData(Material.DARK_PRISMARINE_STAIRS);
			stair.setWaterlogged(true);
			
			stair.setFacing(face);
			block.getRelative(0,1,0).getRelative(face).setBlockData(stair);
			block.getRelative(0,2,0).getRelative(face).getRelative(face)
			.setBlockData(stair);
			
			stair.setFacing(face.getOppositeFace());
			stair.setHalf(Half.TOP);
			block.getRelative(0,1,0).getRelative(face).getRelative(face)
			.setBlockData(stair);
		}
		
		block.getRelative(-1,1,-1).setType(Material.PRISMARINE_BRICK_SLAB);
		block.getRelative(1,1,-1).setType(Material.PRISMARINE_BRICK_SLAB);
		block.getRelative(1,1,1).setType(Material.PRISMARINE_BRICK_SLAB);
		block.getRelative(-1,1,1).setType(Material.PRISMARINE_BRICK_SLAB);
	}
	
	private void spawnUpperClaw(PopulatorDataAbstract data, int x, int y, int z){
		
		SimpleBlock block = new SimpleBlock(data,x,y,z);
		block.setType(Material.SEA_LANTERN);
		for(BlockFace face:BlockUtils.directBlockFaces){
			Stairs stair = (Stairs) Bukkit.createBlockData(Material.DARK_PRISMARINE_STAIRS);
			stair.setHalf(Half.TOP);
			stair.setWaterlogged(true);
			
			stair.setFacing(face);
			block.getRelative(0,-1,0).getRelative(face).setBlockData(stair);
			block.getRelative(0,-2,0).getRelative(face).getRelative(face)
			.setBlockData(stair);
			
			stair.setFacing(face.getOppositeFace());
			stair.setHalf(Half.BOTTOM);
			block.getRelative(0,-1,0).getRelative(face).getRelative(face)
			.setBlockData(stair);
		}
		
		Waterlogged slab = (Waterlogged) Bukkit.createBlockData(Material.PRISMARINE_BRICK_SLAB);
		slab.setWaterlogged(true);
		
		block.getRelative(-1,-1,-1).setBlockData(slab);
		block.getRelative(1,-1,-1).setBlockData(slab);
		block.getRelative(1,-1,1).setBlockData(slab);
		block.getRelative(-1,-1,1).setBlockData(slab);
	}
	
	@Override
	public boolean canPopulate(CubeRoom room){
		return room.getHeight() > 10;
	}
	
}
