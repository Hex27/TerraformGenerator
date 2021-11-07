package org.terraform.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.noise.FastNoise;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class GenUtils {
    private static final Random RANDOMIZER = new Random();
    private static final String[] BLACKLIST_HIGHEST_GROUND = {
            "LEAVES", "LOG",
            "WOOD", "MUSHROOM",
            "FENCE", "WALL",
            "POTTED", "BRICK",
            "CHAIN", "CORAL",
            "POINTED_DRIPSTONE"
    };
    private static final LoadingCache<ChunkCache, ArrayList<BiomeBank>> biomeQueryCache = CacheBuilder.newBuilder()
            .maximumSize(128)
            .build(new CacheLoader<ChunkCache, ArrayList<BiomeBank>>() {
                @Override
                public ArrayList<BiomeBank> load(ChunkCache key) {
                    ArrayList<BiomeBank> banks = new ArrayList<>();
                    int gridX = key.chunkX * 16;
                    int gridZ = key.chunkZ * 16;
                    for(int x = gridX; x < gridX + 16; x++) {
                        for(int z = gridZ; z < gridZ + 16; z++) {
                            BiomeBank bank = key.tw.getBiomeBank(x, z);
                            if(!banks.contains(bank)) banks.add(bank);
                        }
                    }
                    return banks;
                }
            });

    public static SimplexOctaveGenerator getGenerator(World world) {
        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
        generator.setScale(0.005D);
        return generator;
    }

    public static int getSign(Random rand) {
        return rand.nextBoolean() ? 1 : -1;
    }

    public static Collection<int[]> getCaveCeilFloors(PopulatorDataAbstract data, int x, int z) {
        int y = getHighestGround(data, x, z);
        int[] pair = {-1, -1};
        List<int[]> list = new ArrayList<>(y);

        for(int ny = y; ny > 0; ny--) {
            Material type = data.getType(x, ny, z);
            if(type.isSolid()) {
                pair[1] = ny;
                list.add(pair);
                pair = new int[] {-1, -1};
            } else if(pair[0] == -1) pair[0] = ny;
        }

        return list;
    }

    public static int getOctaveHeightAt(World world, int x, int z, int spreadHeight, int minHeight) {
        return (int) (getGenerator(world).noise(x, z, 0.5D, 0.5D)
                * spreadHeight + minHeight);
    }

    public static int getOctaveHeightAt(Chunk c, int x, int z, int spreadHeight, int minHeight) {
        return (int) (
                getGenerator(c.getWorld()).noise(c.getX() * 16 + x, c.getZ() * 16 + z, 0.5D, 0.5D)
                        * spreadHeight + minHeight);
    }

    public static void setRandomBlock(Chunk c, int x, int y, int z, Random random, Material... types) {
        c.getBlock(x, y, z).setType(types[randInt(random, 0, types.length - 1)]);
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

    public static ArrayList<BiomeBank> getBiomesInChunk(TerraformWorld tw, int chunkX, int chunkZ) {
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
        if(tw.getBiomeBank(Math.round(center.x), Math.round(center.y)) == biome)
            return new Vector2f(center.x, center.y);
        int iter = 2;

        int x = (int) center.x;
        int z = (int) center.y;

        while(Math.abs(center.x - x) < radius
                || Math.abs(center.y - z) < radius) {
            for(int i = 0; i < iter / 2; i++) {
                switch(iter % 4) {
                    case 0:
                        x += blockSkip;
                        break;
                    case 1:
                        z -= blockSkip;
                        break;
                    case 2:
                        x -= blockSkip;
                        break;
                    case 3:
                        z += blockSkip;
                        break;
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
     * @param tw
     * @param biome
     * @param centerBlockLocation
     * @return
     */
    public static Vector2f locateHeightIndependentBiome(TerraformWorld tw, BiomeBank biome, Vector2f centerBlockLocation) {
    	BiomeSection center = BiomeBank.getBiomeSectionFromBlockCoords(tw, (int) centerBlockLocation.x, (int) centerBlockLocation.y);
    	boolean found = false;
    	int radius = 0;
    	
    	while(!found) {
    		for(BiomeSection sect:center.getRelativeSurroundingSections(radius)) {
    			if(sect.getBiomeBank() == biome) {
    				SimpleLocation sectionCenter = sect.getCenter();
    				found = true;
    				return new Vector2f(sectionCenter.getX(),sectionCenter.getZ());
    			}
    			radius++;
    		}
    	}

        return null;
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

//	public static Location randomConfinedSurfaceCoordinates(Random rand, PopulatorDataAbstract c){
//		int x = randInt(rand,5,9);
//		int z = randInt(rand,5,9);
//		int y = getTrueHighestBlock(c,x,z);
//		
//		return data.getBlock(x, y, z).getLocation();
//	}

    public static Material randMaterial(Random rand, Material... candidates) {
        return candidates[randInt(rand, 0, candidates.length - 1)];
    }

    public static Material randMaterial(Material... candidates) {
        return randMaterial(RANDOMIZER, candidates);
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


    public static int getHighestX(PopulatorDataAbstract data, int x, int z, Material X) {
        int y = 256 - 1;
        while(y > 0 && data.getType(x, y, z) != X) y--;
        return y;
    }
    
    public static Location getHighestBlock(World w, int x, int z) {
        int y = w.getMaxHeight() - 1;
        while(y > 0 && !w.getBlockAt(x, y, z).getType().isSolid()) y--;
        if(y == 0) {
            TerraformGeneratorPlugin.logger.error("getHighestBlock(w,x,z) returned 0!");
            try { throw new Exception("getHighestBlock(w,x,z) returned 0!"); }
            catch (Exception e) 
            {e.printStackTrace();}
        }
        return new Location(w, x, y, z);
    }

    public static int getHighestBlock(Chunk c, int x, int z, Collection<Material> airs) {
        int y;
        for(y = c.getWorld().getMaxHeight() - 1;
            airs.contains(c.getBlock(x, y, z).getType());
            y--)
            ;

        return y;
    }

    /**
     * @return the highest solid block
     */
    public static int getTrueHighestBlock(PopulatorDataAbstract data, int x, int z) {
        int y = 255;
        while(y > 0 && !data.getType(x, y, z).isSolid()) y--;
        return y;
    }

    
    /**
     * 
     * @param data
     * @param x
     * @param z
     * @return the highest dry ground, or the sea level
     */
    public static int getHighestGroundOrSeaLevel(PopulatorDataAbstract data, int x, int z) {
    	int y = getHighestGround(data,x,z);
    	if(y < TerraformGenerator.seaLevel)
    		return TerraformGenerator.seaLevel;
    	return y;
    }
    
    /**
     * @return the highest solid block below y
     */
    public static int getTrueHighestBlockBelow(PopulatorDataAbstract data, int x, int y, int z) {
        while(y > 0 && !data.getType(x, y, z).isSolid()) y--;
        return y;
    }

    public static SimpleBlock getTrueHighestBlockBelow(SimpleBlock block) {
        int y = block.getY();
        while(y > 0 && !block.getPopData().getType(block.getX(), y, block.getZ()).isSolid()) y--;
        return new SimpleBlock(block.getPopData(), block.getX(), y, block.getZ());
    }


    /**
     * @return the highest solid ground. Is dirt-like or stone-like, and is
     * not leaves or logs
     */
    @SuppressWarnings("incomplete-switch")
    public static int getHighestGround(PopulatorDataAbstract data, int x, int z) {
        int y = 255;
        while(y > 0) {
            Material block = data.getType(x, y, z);
            if(BlockUtils.isStoneLike(block)) break;
            if(block == Material.SAND 
            	|| block == Material.RED_SAND
            	|| block == Material.GRAVEL)
            	break;

            if(block.isSolid()) {
                switch(block) {
                    case HAY_BLOCK:
                    case ICE:
                    case PACKED_ICE:
                    case BLUE_ICE:
                    case CACTUS:
                    case BAMBOO:
                    case BAMBOO_SAPLING:
                    case IRON_BARS:
                    case LANTERN:
                        y--;
                        continue;
                }
                if(block.isInteractable()) {
                    y--;
                    continue;
                }
                if(Tag.SLABS.isTagged(block)) {
                	y--;
                	continue;
                }
                
                String name = block.toString();
                boolean continueMaster = false;
                for(String contains : BLACKLIST_HIGHEST_GROUND) {
                    if(name.contains(contains)) {
                        continueMaster = true;
                        break;
                    }
                }
                if(continueMaster) {
                    y--;
                    continue;
                }
            } else {
                y--;
                continue;
            }
            break;
        }
        if(y == 0) {
            TerraformGeneratorPlugin.logger.error("GetHighestGround returned 0!");
            try { throw new Exception("GetHighestGround returned 0!"); }
            catch (Exception e) 
            {e.printStackTrace();}
        }
        return y;
    }
    
    @SuppressWarnings("incomplete-switch")
	public static boolean isGroundLike(Material block) {
        if(BlockUtils.isStoneLike(block)) return true;
        if(block == Material.SAND 
        	|| block == Material.RED_SAND
        	|| block == Material.GRAVEL)
        	return true;;

        if(block.isSolid()) {
            switch(block) {
                case HAY_BLOCK:
                case ICE:
                case PACKED_ICE:
                case BLUE_ICE:
                case CACTUS:
                case BAMBOO:
                case BAMBOO_SAPLING:
                case IRON_BARS:
                case LANTERN:
                    return false;
            }
            if(block.isInteractable()) {
                return false;
            }
            if(Tag.SLABS.isTagged(block)) {
                return false;
            }
            
            String name = block.toString();
            for(String contains : BLACKLIST_HIGHEST_GROUND) {
                if(name.contains(contains)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
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
    public static Vector2f[] vectorRandomObjectPositions(TerraformWorld world, int chunkX, int chunkZ, int distanceBetween, float maxPerturbation) {

        FastNoise noise = new FastNoise((int) world.getSeed());
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
    	Vector2f[] vecs = vectorRandomObjectPositions(world, chunkX, chunkZ, distanceBetween, 0.35f * distanceBetween);
        SimpleLocation[] locs = new SimpleLocation[vecs.length];
    	
    	for(int i = 0; i < vecs.length; i++) {
    		locs[i] = new SimpleLocation((int) vecs[i].x, 0, (int) vecs[i].y);
    	}
        
        return locs;
    }

    /**
     * 
     * @param world
     * @param chunkX
     * @param chunkZ
     * @param distanceBetween
     * @param pertubMultiplier is normally 0.35.
     * @return
     */
    public static SimpleLocation[] randomObjectPositions(TerraformWorld world, int chunkX, int chunkZ, int distanceBetween, float pertubMultiplier) {
    	Vector2f[] vecs = vectorRandomObjectPositions(world, chunkX, chunkZ, distanceBetween, pertubMultiplier * distanceBetween);
        SimpleLocation[] locs = new SimpleLocation[vecs.length];
    	
    	for(int i = 0; i < vecs.length; i++) {
    		locs[i] = new SimpleLocation((int) vecs[i].x, 0, (int) vecs[i].y);
    	}
        
        return locs;
    }
}
