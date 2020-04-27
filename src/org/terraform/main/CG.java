package org.terraform.main;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class CG extends ChunkGenerator{

	public static final int seaLevel = 62;
	
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
    	ChunkData chunk = this.createChunkData(world);
    	int height = 70;
    	for(int x = 0; x < 16; x++){
    		for(int z = 0; z < 16; z++){
            	for(int y = 255; y >= 0; y--){
            		
            		//if(chunk.getType(x,y,z) != null&&
            			//	chunk.getType(x,y,z) != Material.AIR) continue;
            		
            		if(y == 90) chunk.setBlock(x,y,z,Material.DIRT);
            		
//            		if(y > seaLevel && y > height){
//            			chunk.setBlock(x, y, z, Bukkit.createBlockData(Material.AIR));
//            		}else if(y <= seaLevel && y > height){
//            			chunk.setBlock(x, y, z, Bukkit.createBlockData(Material.WATER));
//            		}else if(y >= 65){//if(y >= height-crust.length+1){
//            			chunk.setBlock(x,y,z,Bukkit.createBlockData(Material.DIRT));
//            			//chunk.setBlock(x,y,z,crust[height-y]);
//            		}else if(y > 3){
//            			chunk.setBlock(x, y, z, Bukkit.createBlockData(Material.STONE));
//            		}else if(y > 0){
//            			chunk.setBlock(x, y, z, Bukkit.createBlockData(Material.BEDROCK));
//            		}else{
//            		    chunk.setBlock(x, y, z, Bukkit.createBlockData(Material.BEDROCK));
//            		}
            	}
    		}
    	}
    	return chunk;
    }
}
