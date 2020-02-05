package org.terraform.coregen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;

public class TerraformGenerator extends ChunkGenerator{
	
	public static final int seaLevel = 62;
	private HeightMap map;
	
//	/**
//	 * 
//	 * @param tw
//	 * @param chunkx
//	 * @param chunkz
//	 * @param x relative x
//	 * @param y relative y
//	 * @param z relative z
//	 * @return whether or not the terrain block should be placed.
//	 */
//	public boolean attemptSimpleBlockUpdate(TerraformWorld tw, ChunkData data, int chunkx, int chunkz, int x, int y, int z){
//		TChunk tc = tw.getChanges().get(new SimpleChunkLocation(tw.getName(), chunkx, chunkz));
//		if(tc == null) return false;
//		
//		SimpleBlock b = tc.getBlock(x,y,z);
//		if(b != null){
//			data.setBlock(x,y,z,b.getType());
//			if(b.getBlockData() != null) 
//				data.setBlock(x,y,z,b.getBlockData());
//			
//			if(b.getType() == Material.WATER) return false;
//			return true;
//		}
//		return false;
//	}
//	
	
    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);     
        if(map == null) map = new HeightMap();
        TerraformWorld tw = TerraformWorld.get(world);
    	//Bukkit.getLogger().info("Attempting gen: " + chunkX + "," + chunkZ);
        
    	for (int x = 0; x < 16; x++){
            for (int z = 0; z < 16; z++) {
            	int rawX = chunkX*16+x;
            	int rawZ = chunkZ*16+z;

            	int height = map.getHeight(tw, rawX, rawZ);

            	BiomeBank bank = tw.getBiomeBank(rawX, height, rawZ);//BiomeBank.calculateBiome(tw,tw.getTemperature(rawX, rawZ), height);
            	Material[] crust = bank.getHandler().getSurfaceCrust(random);
            	biome.setBiome(x, z, bank.getHandler().getBiome());
            	int undergroundHeight = height;
            	int index = 0;
            	while(index < crust.length){
            		//if(!attemptSimpleBlockUpdate(tw, chunk, chunkX, chunkZ, x,undergroundHeight,z)) 
            		chunk.setBlock(x,undergroundHeight,z,crust[index]);
            		index++;
            		undergroundHeight--;
            	}
            	
            	for(int y = undergroundHeight;y > 0; y--){
            		//if(!attemptSimpleBlockUpdate(tw, chunk, chunkX, chunkZ, x,undergroundHeight,z)) 
            		chunk.setBlock(x,y,z,Material.STONE);
            	}
            	
            	//Any low elevation is sea
            	for(int y = height+1; y <= seaLevel; y++){
            		//if(!attemptSimpleBlockUpdate(tw, chunk, chunkX, chunkZ, x,undergroundHeight,z)) 
            		chunk.setBlock(x,y,z,Material.WATER);
            	}
            	
            	//Bedrock Base
                chunk.setBlock(x, 2, z, GenUtils.randMaterial(random, Material.STONE, Material.BEDROCK));
                chunk.setBlock(x, 1, z, GenUtils.randMaterial(random, Material.STONE, Material.BEDROCK));
                chunk.setBlock(x, 0, z, Material.BEDROCK);
                
            }
    	}
    	//Bukkit.getLogger().info("Finished: " + chunkX + "," + chunkZ);
    	
    	return chunk;
    }
    
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        TerraformWorld tw = TerraformWorld.get(world);
        return new ArrayList<BlockPopulator>(){{
        	add(new TerraformStructurePopulator(tw,TerraformGeneratorPlugin.injector));
        }};
    }
}
