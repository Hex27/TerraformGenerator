package org.terraform.structure.dungeon;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.farmhouse.FarmhousePopulator;
import org.terraform.structure.farmhouse.FarmhouseSchematicParser;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.stronghold.StrongholdPathPopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Temperature;

public class SmallDungeonPopulator extends StructurePopulator{

	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
		ArrayList<BiomeBank> banks = new ArrayList<>();
		int numOceanic = 0;
		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int height = new HeightMap().getHeight(tw, x, z);//GenUtils.getTrueHighestBlock(data, x, z);
				for(BiomeBank bank:BiomeBank.values()){
					BiomeBank currentBiome = tw.getBiomeBank(x, height, z);//BiomeBank.calculateBiome(tw,tw.getTemperature(x, z), height);
					
					if(currentBiome.toString().contains("OCEAN")) numOceanic++;
					
					if(bank == currentBiome){
						if(!banks.contains(bank))
							banks.add(bank);
						break;
					}
				}
			}
		}
		
		if(numOceanic/banks.size() == 1){
			//Only spawn these in full oceans
			new DrownedDungeonPopulator().populate(tw,random,data);
		}else{
			new UndergroundDungeonPopulator().populate(tw, random, data);
		}
		
		
//		if(!banks.contains(BiomeBank.FOREST)
//				&& !banks.contains(BiomeBank.PLAINS)
//				&& !banks.contains(BiomeBank.TAIGA)
//				&& !banks.contains(BiomeBank.SAVANNA)
//				&& !banks.contains(BiomeBank.SNOWY_WASTELAND)
//				&& !banks.contains(BiomeBank.SNOWY_TAIGA)){
//			
//		}else{
//			new FarmhousePopulator().populate(tw, random, data);
//		}
	}
	
	@Override
	public boolean canSpawn(Random rand,TerraformWorld tw, int chunkX, int chunkZ,ArrayList<BiomeBank> biomes) {

		MegaChunk mc = new MegaChunk(chunkX,chunkZ);
		int[][] allCoords = getCoordsFromMegaChunk(tw,mc);
		for(int[] coords:allCoords){
			if(coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ){
				return true;
			}
		}
		return false;
	}
	
	//Each mega chunk has 10 dungeons
	protected int[][] getCoordsFromMegaChunk(TerraformWorld tw,MegaChunk mc){
		return new int[][]{
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),1317324)),
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),131732)),
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),13176)),
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),131327)),
				mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(),17328))
				};
	}

	@Override
	public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
		MegaChunk mc = new MegaChunk(rawX,0,rawZ);
		
		double minDistanceSquared = Integer.MAX_VALUE;
		int[] min = null;
		for(int nx = -1; nx <= 1; nx++){
			for(int nz = -1; nz <= 1; nz++){
				for(int[] loc:getCoordsFromMegaChunk(tw,mc.getRelative(nx, nz))){
					double distSqr = Math.pow(loc[0]-rawX,2) + Math.pow(loc[1]-rawZ,2);
					if(distSqr < minDistanceSquared){
						minDistanceSquared = distSqr;
						min = loc;
					}
				}
			}
		}
		return min;
	}


}
