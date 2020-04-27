package org.terraform.structure.shipwreck;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.StructurePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class ShipwreckPopulator extends StructurePopulator{


	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {

		if(!TConfigOption.STRUCTURES_SHIPWRECK_ENABLED.getBoolean())
			return;
		int seaLevel = TerraformGenerator.seaLevel;
		MegaChunk mc = new MegaChunk(data.getChunkX(),data.getChunkZ());
		int[] coords = getCoordsFromMegaChunk(tw,mc);
		int x = coords[0];//data.getChunkX()*16 + random.nextInt(16);
		int z = coords[1];//data.getChunkZ()*16 + random.nextInt(16);
		int height = GenUtils.getHighestGround(data, x, z) - 1 - random.nextInt(5);
		spawnShipwreck(tw,tw.getHashedRand(x, height, z, 127127127),data,x,height+1,z);
	}
	
	private String[] schematics = new String[]{"upright-shipwreck-1",
			"tilted-shipwreck-1"};
	
	public void spawnShipwreck(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z){
		try {
			y += GenUtils.randInt(random,-1,1);
			TerraSchematic shipwreck = TerraSchematic.load(schematics[random.nextInt(schematics.length)], new Location(tw.getWorld(),x,y,z));
			shipwreck.parser = new ShipwreckSchematicParser(random,data);
			shipwreck.setFace(BlockUtils.getDirectBlockFace(random));
			shipwreck.apply();

			TerraformGeneratorPlugin.logger.info("Spawning shipwreck at " + x + "," + y + "," + z + " with rotation of " + shipwreck.getFace().toString());
			
			//Generate holds and damage
			for(int i = 0; i < GenUtils.randInt(random,0,3); i++){
				int nx = x + GenUtils.randInt(random,-8,8);
				int nz = z + GenUtils.randInt(random,-8,8);
				int ny = y + GenUtils.randInt(random,0,5);
				BlockUtils.replaceWaterSphere(nx*7*ny*23*nz, GenUtils.randInt(1,3), new SimpleBlock(data,nx,ny,nz));
			}
			
			//Dropdown blocks
			for(int i = 0; i < GenUtils.randInt(random,5,15); i++){
				int nx = x + GenUtils.randInt(random,-8,8);
				int nz = z + GenUtils.randInt(random,-8,8);
				int ny = y + GenUtils.randInt(random,0,5);
				dropDownBlock(new SimpleBlock(data,nx,ny,nz));
			}
			
			data.addEntity(x, y+12, z, EntityType.DROWNED); //Two Drowneds
			data.addEntity(x, y+15, z, EntityType.DROWNED);
			
			
		} catch (Throwable e) {
			TerraformGeneratorPlugin.logger.error("Something went wrong trying to place shipwreck at " + x + "," + y + "," + z + "!");
			e.printStackTrace();
		}
	}
	
	protected int[] getCoordsFromMegaChunk(TerraformWorld tw,MegaChunk mc){
		return mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),191921));
	}

	
	public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
		MegaChunk mc = new MegaChunk(rawX,0,rawZ);
		
		double minDistanceSquared = Integer.MAX_VALUE;
		int[] min = null;
		for(int nx = -1; nx <= 1; nx++){
			for(int nz = -1; nz <= 1; nz++){
				int[] loc = getCoordsFromMegaChunk(tw,mc.getRelative(nx, nz));
				double distSqr = Math.pow(loc[0]-rawX,2) + Math.pow(loc[1]-rawZ,2);
				if(distSqr < minDistanceSquared){
					minDistanceSquared = distSqr;
					min = loc;
				}
			}
		}
		return min;
	}

	@Override
	public boolean canSpawn(Random rand, TerraformWorld tw, int chunkX,
			int chunkZ, ArrayList<BiomeBank> biomes) {
		for(BiomeBank b:biomes){
			if(b.toString().contains("OCEAN")){
				MegaChunk mc = new MegaChunk(chunkX,chunkZ);
				int[] coords = getCoordsFromMegaChunk(tw,mc);
				if(coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ){
					return true;
				}
				
			}
		}
		return false;
	}
	
	private void dropDownBlock(SimpleBlock block){
		if(block.getType().isSolid()){
			Material type = block.getType();
			if(type == Material.CHEST) return;
			block.setType(Material.WATER);
			int depth = 0;
			while(!block.getType().isSolid()){
				block = block.getRelative(0,-1,0);
				depth++;
				if(depth > 50) return;
			}
			
			block.getRelative(0,1,0).setType(type);
		}
	}
	

	
}
