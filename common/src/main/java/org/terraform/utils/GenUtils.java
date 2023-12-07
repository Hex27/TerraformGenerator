package org.terraform.utils;

import com.google.common.cache.LoadingCache;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataSpigotAPI;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.noise.FastNoise;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class GenUtils {
    public static final Random RANDOMIZER = new Random();
    private static final EnumSet<Material> BLACKLIST_HIGHEST_GROUND = EnumSet.noneOf(Material.class);
    public static void initGenUtils() {

    	//Initialize highest ground blacklist
    	for(Material mat:Material.values()) {
    		if(mat.toString().contains("LEAVES")
    				|| mat.toString().contains("LOG")
    				|| mat.toString().contains("WOOD")
    				|| mat.toString().contains("MUSHROOM")
    				|| mat.toString().contains("FENCE")
    				|| mat.toString().contains("WALL")
    				|| mat.toString().contains("POTTED")
    				|| mat.toString().contains("BRICK")
    				|| mat.toString().contains("CHAIN")
    				|| mat.toString().contains("CORAL")
    				|| mat.toString().contains("POINTED_DRIPSTONE")
    				|| mat.toString().contains("NETHERRACK")
    				|| mat.toString().contains("MANGROVE")
    				|| mat == Material.HAY_BLOCK
    				|| mat == Material.ICE
    				|| mat == Material.CACTUS
    				|| mat == Material.BAMBOO
    				|| mat == Material.BAMBOO_SAPLING
    				|| mat == Material.IRON_BARS
    				|| mat == Material.LANTERN
    				//|| mat == Material.SNOW_BLOCK
    				//|| mat == Material.PACKED_ICE
    				//|| mat == Material.BLUE_ICE
    				) {
//    	    	{
//              "LEAVES", "LOG",
//              "WOOD", "MUSHROOM",
//              "FENCE", "WALL",
//              "POTTED", "BRICK",
//              "CHAIN", "CORAL",
//              "POINTED_DRIPSTONE",
//              "NETHERRACK"
//      };
    			BLACKLIST_HIGHEST_GROUND.add(mat);
    		}
    	}
    }
    public static LoadingCache<ChunkCache, EnumSet<BiomeBank>> biomeQueryCache;

    public static int getSign(Random rand) {
        return rand.nextBoolean() ? 1 : -1;
    }

    /**
     * This will now use StoneLike, and not just any solid block.
     */
    public static Collection<int[]> getCaveCeilFloors(PopulatorDataAbstract data, int x, int z) {
        int y = getHighestGround(data, x, z);
        int[] pair = {TerraformGeneratorPlugin.injector.getMinY() - 1, TerraformGeneratorPlugin.injector.getMinY() - 1};
        List<int[]> list = new ArrayList<>();

        for(int ny = y; ny > TerraformGeneratorPlugin.injector.getMinY(); ny--) {
            Material type = data.getType(x, ny, z);
            if(BlockUtils.isStoneLike(type)) {
                pair[1] = ny;
                list.add(pair);
                pair = new int[] {TerraformGeneratorPlugin.injector.getMinY() - 1,TerraformGeneratorPlugin.injector.getMinY() - 1};
            } else if(pair[0] == TerraformGeneratorPlugin.injector.getMinY() - 1) pair[0] = ny;
        }

        return list;
    }

    public static int[] randomCoords(Random rand, int[] lowBound, int[] highBound) {
        return new int[] {randInt(rand, lowBound[0], highBound[0]),
                randInt(rand, lowBound[1], highBound[1]),
                randInt(rand, lowBound[2], highBound[2])};
    }

    public static boolean chance(Random rand, int chance, int outOf) {
        return randInt(rand, 1, outOf) <= chance;
    }

    public static boolean chance(int chance, int outOf) {
        return randInt(new Random(), 1, outOf) <= chance;
    }

    public static EnumSet<BiomeBank> getBiomesInChunk(TerraformWorld tw, int chunkX, int chunkZ) {
        ChunkCache key = new ChunkCache(tw, chunkX, chunkZ);
        return biomeQueryCache.getUnchecked(key);
    }

    /**
     * Locates the target biome in given area using brute force.
     * Note that the function is blocking.
     * <br><br>
     * This should not be used for first phase biomes (i.e. height-independent biomes like oceans, mountains etc)
     * This should be used for stuff like beaches and rivers, which cannot be broadly searched for with biome sections.
     * <br><br>
     * If for whatever reason you still use this for height-independent biomes, it will still work. It's just slower.
     *
     * @return Position for the biome or null if no biomes found.
     */
    public static Vector2f locateHeightDependentBiome(TerraformWorld tw, BiomeBank biome, Vector2f center, int radius, int blockSkip) {
        if(!BiomeBank.isBiomeEnabled(biome)) return null;
    	if(tw.getBiomeBank(Math.round(center.x), Math.round(center.y)) == biome)
            return new Vector2f(center.x, center.y);
        int iter = 2;

        int x = (int) center.x;
        int z = (int) center.y;

        while(Math.abs(center.x - x) < radius
                || Math.abs(center.y - z) < radius) {
            for(int i = 0; i < iter / 2; i++) {
                switch(iter % 4) {
                    case 0 -> x += blockSkip;
                    case 1 -> z -= blockSkip;
                    case 2 -> x -= blockSkip;
                    case 3 -> z += blockSkip;
                }
            }

            if(tw.getBiomeBank(x, z) == biome) return new Vector2f(x, z);
            iter++;
        }

        return null;
    }
    
    /**
     * Locates a biome with BiomeSection.
     * WILL NOT FIND RIVERS AND BEACHES.
     * Does not stop until biome is found, much like structure locate, because it should work.
     */
    public static Vector2f locateHeightIndependentBiome(TerraformWorld tw, BiomeBank biome, Vector2f centerBlockLocation) {
        if(!BiomeBank.isBiomeEnabled(biome)) return null;
        
    	BiomeSection center = BiomeBank.getBiomeSectionFromBlockCoords(tw, (int) centerBlockLocation.x, (int) centerBlockLocation.y);
    	int radius = 0;
    	
    	while(true) {
    		for(BiomeSection sect:center.getRelativeSurroundingSections(radius)) {
    			if(sect.getBiomeBank() == biome) {
    				SimpleLocation sectionCenter = sect.getCenter();
    				return new Vector2f(sectionCenter.getX(),sectionCenter.getZ());
    			}
    			radius++;
    		}
    	}
    }

    public static Material weightedRandomMaterial(Random rand, Object... candidates) {
        if(candidates.length % 2 != 0) throw new IllegalArgumentException();
        ArrayList<Material> types = new ArrayList<>(50);
        for(int i = 0; i < candidates.length; i++) {
            Material type = (Material) candidates[i++];
            int freq = (int) candidates[i];
            for(int z = 0; z < freq; z++) types.add(type);
        }

        return types.get(randInt(rand, 0, types.size() - 1));
    }

    public static Material randMaterial(Random rand, Material... candidates) {
        if(candidates.length == 1) return candidates[0]; //avoid invocation to randInt
        return candidates[randInt(rand, 0, candidates.length - 1)];
    }

    public static Material randMaterial(Material... candidates) {
        return randMaterial(RANDOMIZER, candidates);
    }
    public static Material randMaterial(EnumSet<Material> candidates) {
    	Material[] temp = new Material[candidates.size()];
    	int pointer = 0;
    	for(Material candidate:candidates) {
    		temp[pointer] = candidate;
    		pointer++;
    	}
        return randMaterial(RANDOMIZER, temp);
    }

    public static int[] randomSurfaceCoordinates(Random rand, PopulatorDataAbstract data) {
        int chunkX = data.getChunkX();
        int chunkZ = data.getChunkZ();
        int x = randInt(rand, chunkX * 16, chunkX * 16 + 15);
        int z = randInt(rand, chunkZ * 16, chunkZ * 16 + 15);
        int y = getTrueHighestBlock(data, x, z);

        return new int[] {x, y, z};
    }

    public static int randInt(int min, int max) {
        if(min == max) return min;
        return randInt(RANDOMIZER, min, max);
    }

    public static int randInt(Random rand, int d, int max) {
        if(d == max) return d;
        boolean negative = false;
        if(d < 0 && max < 0) {
            negative = true;
            d = -d;
            max = -max;
        }

        if(max < d) {
            int temp = d;
            d = max;
            max = temp;
        }

        int randomNum = rand.nextInt((max - d) + 1) + d;
        return negative ? -randomNum : randomNum;
    }

    /**
     * Try to have a max-min of more than 2.
     */
    public static int randOddInt(Random rand, int min, int max) {
        int randomNum = rand.nextInt((max - min) + 1) + min;
        if(randomNum % 2 == 0) {
            if(randomNum++ > max) randomNum -= 2;
        }
        return randomNum;
    }

    public static double randDouble(Random rand, double min, double max) {
        return rand.nextDouble() * (max - min) + min;
    }
    public static int getHighestX(PopulatorDataAbstract data, int x, int z, Material X) {
        int y = TerraformGeneratorPlugin.injector.getMaxY() - 1;
        while(y > TerraformGeneratorPlugin.injector.getMinY() && data.getType(x, y, z) != X) y--;
        return y;
    }


    /**
     * @return the highest solid block
     */
    public static int getTrueHighestBlock(PopulatorDataAbstract data, int x, int z) {
        int y = TerraformGeneratorPlugin.injector.getMaxY()-1;
        while(y > TerraformGeneratorPlugin.injector.getMinY() && !data.getType(x, y, z).isSolid()) y--;
        return y;
    }

    
    /**
     *
     * @return the highest dry ground, or the sea level
     */
    public static int getHighestGroundOrSeaLevel(PopulatorDataAbstract data, int x, int z) {
    	int y = getHighestGround(data,x,z);
        return Math.max(y, TerraformGenerator.seaLevel);
    }
    
    /**
     * @return the highest solid block below y
     */
    public static int getTrueHighestBlockBelow(PopulatorDataAbstract data, int x, int y, int z) {
        while(y > TerraformGeneratorPlugin.injector.getMinY() && !data.getType(x, y, z).isSolid()) y--;
        return y;
    }

    public static SimpleBlock getTrueHighestBlockBelow(SimpleBlock block) {
        int y = block.getY();
        while(y > TerraformGeneratorPlugin.injector.getMinY() && !block.getPopData().getType(block.getX(), y, block.getZ()).isSolid()) y--;
        return new SimpleBlock(block.getPopData(), block.getX(), y, block.getZ());
    }

	public static boolean isGroundLike(Material mat) {
    	//Ice is considered stone-like, but not in a million years is it ground.
        if(BlockUtils.isStoneLike(mat) 
        		&& mat != Material.PACKED_ICE 
        		&& mat != Material.BLUE_ICE) 
        	return true;
        if(mat == Material.SAND 
        	|| mat == Material.RED_SAND
        	|| mat == Material.GRAVEL)
        	return true;

        if(mat.isSolid()) {
            if(mat.isInteractable()) {
                return false;
            }
            if(Tag.SLABS.isTagged(mat)) {
            	return false;
            }
            return !BLACKLIST_HIGHEST_GROUND.contains(mat);
        } else {
            return false;
        }
    }

    /**
     * Meant as a new way to calculate noise-cached heights.
     * <br>
     * Stop fucking iterating from the sky, you look like an idiot.
     * Please for the love of god, use this where you can
     */
    public static int getTransformedHeight(PopulatorDataAbstract data, int rawX, int rawZ)
    {
        ChunkCache cache = TerraformGenerator.getCache(data.getTerraformWorld(), rawX, rawZ);
        int cachedY = cache.getTransformedHeight(rawX&0xF, rawZ&0xF);
        if(cachedY == Float.MIN_VALUE){
            TerraformGenerator.buildFilledCache(data.getTerraformWorld(), rawX>>4,rawZ>>4, cache);
        }
        //TODO: There is a problem with this that makes it return 0. Check it.
        return cache.getTransformedHeight(rawX&0xF, rawZ&0xF);
    }

    /**
     * @return the highest solid ground. Is dirt-like or stone-like, and is
     * not leaves or logs
     * <br>
     * The damn issue with this stupid stupid method is that caching it is
     * inherently unsafe. If I call this, then place stone on the floor, it
     * technically changes.
     * <br>
     * I hate this method so much.
     * <br>
     * But you know what? I'm gonna cache it anyway.
     */
    public static int getHighestGround(PopulatorDataAbstract data, int x, int z) {
        //If you're too lazy to bother then just do this
        if(data instanceof PopulatorDataSpigotAPI) return getTransformedHeight(data,x,z);

    	int y = TerraformGeneratorPlugin.injector.getMaxY()-1;
    	ChunkCache cache = TerraformGenerator.getCache(data.getTerraformWorld(), x, z);
    	int cachedY = cache.getHighestGround(x, z);
    	if(cachedY != Float.MIN_VALUE) {
    		//Basic check to ensure block above is not ground
    		//and current block is ground.
    		//Will fail if the new ground is an overhang of some kind.
    		if(isGroundLike(data.getType(x, cachedY, z))
    				&& !isGroundLike(data.getType(x, cachedY+1, z))) {
                return cache.getHighestGround(x, z);
            }
    	}

        while(y > TerraformGeneratorPlugin.injector.getMinY()) {
            Material block = data.getType(x, y, z);
            if(!isGroundLike(block)) {
            	y--;
            	continue;
            }
            break;
        }
        if(y <= TerraformGeneratorPlugin.injector.getMinY()) {
            TerraformGeneratorPlugin.logger.error("GetHighestGround returned less than " + TerraformGeneratorPlugin.injector.getMinY() + "! (" + y + ")");
            try { throw new Exception("GetHighestGround returned less than " + TerraformGeneratorPlugin.injector.getMinY() + "! (" + y + ")"); }
            catch (Exception e) 
            {e.printStackTrace();}
        }
        
        //Y can be stored as a short, as there's no way world height will be 32k.
        cache.cacheHighestGround(x, z, Integer.valueOf(y).shortValue());
        return y;
    }

    public static Material[] mergeArr(Material[]... arrs) {
        int totalLength = 0, index = 0;
        for(Material[] arr : arrs) totalLength += arr.length;
        Material[] res = new Material[totalLength];

        for(Material[] arr : arrs) {
            for(Material mat : arr) {
                res[index++] = mat;
            }
        }

        return res;
    }

    /**
     * Function that returns random positions inside chunk.
     * An algorithm makes sure that objects are at least user-defined
     * distance away from each others. Object density can also be
     * configured precisely.
     *
     * @param distanceBetween Initial distance between objects in a grid.
     *                        (aka. density)
     * @param maxPerturbation Max amount a point can move in each axis
     * @return List of points
     */
    public static Vector2f[] vectorRandomObjectPositions(int seed, int chunkX, int chunkZ, int distanceBetween, float maxPerturbation) {

        FastNoise noise = new FastNoise(seed);
        noise.SetFrequency(1);
        noise.SetGradientPerturbAmp(maxPerturbation);

        ArrayList<Vector2f> positions = new ArrayList<>();

        // Calculate first grid element position in chunk
        // Fixme come up with better algorithm for calculating next grid item inside chunk
        int i;
        int startX = (chunkX << 4) - 5;
        i = (distanceBetween - (startX % distanceBetween));
        startX += i != distanceBetween ? i : 0;
        int startZ = (chunkZ << 4) - 5;
        i = (distanceBetween - (startZ % distanceBetween));
        startZ += i != distanceBetween ? i : 0;

        // Also checks if points from chunks close by are perturbed to this chunk
        for (int x = startX - distanceBetween; x < startX + 16 + distanceBetween; x += distanceBetween) {
            for(int z = startZ - distanceBetween; z < startZ + 16 + distanceBetween; z += distanceBetween) {
                Vector2f v = new Vector2f(x, z);
                noise.GradientPerturb(v);
                v.x = Math.round(v.x);
                v.y = Math.round(v.y);

                // If perturbed vector is inside chunk
                if (v.x >= (chunkX << 4) && v.x < (chunkX << 4) + 16 &&
                        v.y >= (chunkZ << 4) && v.y < (chunkZ << 4) + 16 )
                    positions.add(v);
            }
        }

        return positions.toArray(new Vector2f[0]);
    }

    public static SimpleLocation[] randomObjectPositions(TerraformWorld world, int chunkX, int chunkZ, int distanceBetween) {
    	Vector2f[] vecs = vectorRandomObjectPositions((int)world.getSeed(), chunkX, chunkZ, distanceBetween, 0.35f * distanceBetween);
        SimpleLocation[] locs = new SimpleLocation[vecs.length];
    	
    	for(int i = 0; i < vecs.length; i++) {
    		locs[i] = new SimpleLocation((int) vecs[i].x, 0, (int) vecs[i].y);
    	}
        
        return locs;
    }

    /**
     *
     * @param pertubMultiplier is normally 0.35.
     */
    public static SimpleLocation[] randomObjectPositions(TerraformWorld world, int chunkX, int chunkZ, int distanceBetween, float pertubMultiplier) {
    	Vector2f[] vecs = vectorRandomObjectPositions((int)world.getSeed(), chunkX, chunkZ, distanceBetween, pertubMultiplier * distanceBetween);
        SimpleLocation[] locs = new SimpleLocation[vecs.length];
    	
    	for(int i = 0; i < vecs.length; i++) {
    		locs[i] = new SimpleLocation((int) vecs[i].x, 0, (int) vecs[i].y);
    	}
        
        return locs;
    }
    
    /**
     * 
     * @param seed for tighter control between points
     * @param pertubMultiplier is normally 0.35.
     */
    public static SimpleLocation[] randomObjectPositions(int seed, int chunkX, int chunkZ, int distanceBetween, float pertubMultiplier) {
    	Vector2f[] vecs = vectorRandomObjectPositions(seed, chunkX, chunkZ, distanceBetween, pertubMultiplier * distanceBetween);
        SimpleLocation[] locs = new SimpleLocation[vecs.length];
    	
    	for(int i = 0; i < vecs.length; i++) {
    		locs[i] = new SimpleLocation((int) vecs[i].x, 0, (int) vecs[i].y);
    	}
        
        return locs;
    }

    /**
     * Random-angle
     * @return An angle between lowerBound*base to upperBound*base degrees in radians
     */
    public static double randAngle(double base, double lowerBound, double upperBound) {
        return GenUtils.randDouble(new Random(), lowerBound * base, upperBound * base);
    }

    public static <T> T choice(Random rand, T[] array)
    {
        if(array.length == 0) return null;
        if(array.length == 1) return array[0];
        return array[rand.nextInt(array.length)];
    }
}
