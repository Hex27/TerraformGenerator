package org.terraform.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.terraform.coregen.PopulatorDataAbstract;

public class GenUtils {
	
	
	public static SimplexOctaveGenerator getGenerator(World world){
		SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
        generator.setScale(0.005D);
		return generator;
	}
	
	public static int getOctaveHeightAt(World world, int x, int z,int spreadHeight, int minHeight){

        return (int) (getGenerator(world).noise(x, z, 0.5D, 0.5D)
    			*spreadHeight
    			+minHeight);
	}
	
	public static int getOctaveHeightAt(Chunk c, int x, int z,int spreadHeight, int minHeight){
		return (int) (
				getGenerator(c.getWorld()).noise(c.getX()*16+x, c.getZ()*16+z, 0.5D, 0.5D)
    			*spreadHeight
    			+minHeight);
	}
	
	public static void setRandomBlock(Chunk c, int x, int y, int z, Random random, Material... types){
		c.getBlock(x, y, z).setType(types[randInt(random,0,types.length-1)]);
	}
	
	public static boolean chance(Random rand, int chance,int outOf){
		return randInt(rand,1,outOf) <= chance;
	}
	
	public static boolean chance(int chance,int outOf){
		return randInt(new Random(),1,outOf) <= chance;
	}
	
	public static Material weightedRandomMaterial(Random rand, Object... candidates){
		if(candidates.length%2 != 0) throw new IllegalArgumentException();
		ArrayList<Material> types = new ArrayList<>();
		for(int i = 0; i < candidates.length; i++){
			Material type = (Material) candidates[i];
			i++;
			int freq = (Integer) candidates[i];
			for(int z = 0; z < freq; z++){
				types.add(type);
			}
		}
		
		return types.get(randInt(rand,0,types.size()-1));
	}
	
	public static Material randMaterial(Random rand, Material... candidates){
		return candidates[randInt(rand,0,candidates.length-1)];
	}
	
	public static Material randMaterial(Material... candidates){
		return randMaterial(new Random(), candidates);
	}
	
	public static int[] randomSurfaceCoordinates(Random rand, PopulatorDataAbstract data){
		int chunkX = data.getChunkX();
		int chunkZ = data.getChunkZ();
		int x = randInt(rand,chunkX*16,chunkX*16+15);
		int z = randInt(rand,chunkZ*16,chunkZ*16+15);
		int y = getTrueHighestBlock(data,x,z);
		
		return new int[]{x,y,z};
	}
	
//	public static Location randomConfinedSurfaceCoordinates(Random rand, PopulatorDataAbstract c){
//		int x = randInt(rand,5,9);
//		int z = randInt(rand,5,9);
//		int y = getTrueHighestBlock(c,x,z);
//		
//		return data.getBlock(x, y, z).getLocation();
//	}
	
	public static int randInt(int min, int max) {


	    return randInt(new Random(),min,max);
	}
	
	public static int randInt(Random rand,int min, int max) {


	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	public static double randDouble(Random rand,double min, double max) {

	    double randomNum = rand.nextDouble()*(max-min) + min;

	    return randomNum;
	}
	
	public static boolean diceRoll(int sides) {
		return diceRoll(new Random(),sides);
	}
	
	public static boolean diceRoll(Random rand,int sides) {
	    return randInt(rand,1,sides) == sides;
	}
	
	public static Location getHighestBlock(World w, int x, int z){
		int y = w.getMaxHeight()-1;
		while(!w.getBlockAt(x, y, z).getType().isSolid()){
			y--;
		}
		
		return new Location(w,x,y,z);
	}
	
	public static int getHighestBlock(Chunk c,int x, int z, Collection<Material> airs){
		int y;
		for (y = c.getWorld().getMaxHeight()-1; 
				airs.contains(c.getBlock(x,y,z).getType()); 
				y--);
		
		return y;
	}
	
//	public static int getHighestSpawnableBlock(Chunk c, int x, int z){
//		int y = getOctaveHeightAt(c,x,z);
//		if(c.getBlock(x, y, z).getType().isSolid() &&
//				c.getBlock(x, y+1, z).getType() == Material.AIR &&
//				c.getBlock(x, y+2, z).getType() == Material.AIR){
//			return y+1;
//		}
//		return getHighestBlock(c,x,z);
//	}
//	
	/**
	 * 
	 * @param c
	 * @param x
	 * @param z
	 * @return the highest solid block
	 */
	public static int getTrueHighestBlock(PopulatorDataAbstract data, int x, int z){
		int y=255;
		while(!data.getType(x, y, z).isSolid()){
			y--;
		}
		
		return y;
	}
	
	/**
	 * 
	 * @return the highest solid ground. Is dirt-like or stone-like, and is 
	 * not leaves or logs
	 */
	public static int getHighestGround(PopulatorDataAbstract data, int x, int z){
		int y=255;
//		while((!BlockUtils.isDirtLike(data.getType(x,y,z))&&
//				!BlockUtils.isStoneLike(data.getType(x,y,z))) ||
//				(data.getType(x,y,z).isSolid() &&
//						data.getType(x,y,z).toString().contains("LEAVES") &&
//						data.getType(x,y,z).toString().contains("LOG"))){
//			y--;
//		}
		while(true){
			if(BlockUtils.isStoneLike(data.getType(x,y,z))) break;
			if(data.getType(x, y, z).isSolid()){
				if(data.getType(x, y, z).toString().contains("LEAVES")){
				}else if(data.getType(x, y, z).toString().contains("LOG")){
				}else if(data.getType(x, y, z).toString().contains("WOOD")){
				}else
					break;
			}

			y--;
		}
		
		return y;
	}

}
